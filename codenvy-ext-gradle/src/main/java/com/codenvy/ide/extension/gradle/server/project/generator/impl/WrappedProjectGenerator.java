/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.gradle.server.project.generator.impl;

import com.codenvy.api.builder.BuildStatus;
import com.codenvy.api.builder.dto.BuildOptions;
import com.codenvy.api.builder.dto.BuildTaskDescriptor;
import com.codenvy.api.core.*;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.api.core.util.*;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.api.vfs.server.VirtualFileSystem;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.commons.lang.NamedThreadFactory;
import com.codenvy.commons.lang.Pair;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.extension.gradle.server.GradleTemplateHelper;
import com.codenvy.ide.extension.gradle.server.project.generator.ProjectGenerator;
import com.codenvy.ide.gradle.DistributionVersion;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.codenvy.api.builder.BuildStatus.FAILED;
import static com.codenvy.api.builder.BuildStatus.SUCCESSFUL;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.WRAPPED_GENERATION_STRATEGY;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.WRAPPER_VERSION_OPTION;

/**
 * Wrapped project generator.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class WrappedProjectGenerator implements ProjectGenerator {

    private       String                    apiUrl;
    private final VirtualFileSystemRegistry vfsRegistry;
    private final DownloadPlugin downloadPlugin = new HttpDownloadPlugin();
    private ExecutorService executor;

    @Inject
    public WrappedProjectGenerator(@Named("api.endpoint") String apiUrl,
                                   VirtualFileSystemRegistry vfsRegistry) {
        this.apiUrl = apiUrl;
        this.vfsRegistry = vfsRegistry;
    }

    @PostConstruct
    void start() {
        executor = Executors
                .newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("-ProjectGenerator-gradle-wrapper-").setDaemon(true).build());
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getId() {
        return WRAPPED_GENERATION_STRATEGY;
    }

    /** {@inheritDoc} */
    @Override
    public void generateProject(@Nonnull FolderEntry baseFolder, @Nonnull Map<String, AttributeValue> attributes,
                                Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        if ((options == null || !options.containsKey(WRAPPER_VERSION_OPTION) || options.get(WRAPPER_VERSION_OPTION) == null)) {
            return;
        }

        DistributionVersion version = DistributionVersion.fromVersion(options.get(WRAPPER_VERSION_OPTION));

        try {
            //generate build script file
            GradleTemplateHelper.getInstance().createWrappedGradleBuildFile(baseFolder, version);

            //then fetch from builder rest necessary project files, e.g. wrapper.jar, *.properties
            Callable<BuildTaskDescriptor> callable = createTaskFor(baseFolder);
            final BuildTaskDescriptor task = executor.submit(callable).get();
            if (task.getStatus() == SUCCESSFUL) {
                final File downloadFolder = Files.createTempDirectory("generated-project-").toFile();
                final File generatedProject = new File(downloadFolder, "project.zip");
                downloadGeneratedProject(task, generatedProject);
                importZipToFolder(generatedProject, baseFolder);
                FileCleaner.addFile(downloadFolder);
            } else if (task.getStatus() == FAILED) {
                throw new ServerException("Failed to generate project.");
            }
        } catch (NotFoundException | InterruptedException | ExecutionException | IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    private Callable<BuildTaskDescriptor> createTaskFor(final FolderEntry baseFolder) {
        return new Callable<BuildTaskDescriptor>() {
            @Override
            public BuildTaskDescriptor call() throws Exception {
                final ValueHolder<Link> statusUrlHolder = new ValueHolder<>();

                BuildOptions buildOptions = DtoFactory.getInstance().createDto(BuildOptions.class)
                                                      .withBuilderName(GRADLE_ID)
                                                      .withTargets(Collections.singletonList("wrapper"));

                try {
                    BuildTaskDescriptor task =
                            HttpJsonHelper.post(BuildTaskDescriptor.class,
                                                apiUrl + "/builder/" + baseFolder.getWorkspace() + "/build",
                                                buildOptions,
                                                Pair.of("project", baseFolder.getPath()));
                    Link statusLink = LinksHelper.findLink("get status", task.getLinks());
                    statusUrlHolder.set(statusLink);
                } catch (IOException | UnauthorizedException | NotFoundException e) {
                    throw new ServerException(e);
                }

                final Link statusUrl = statusUrlHolder.get();
                try {
                    for (; ; ) {
                        if (Thread.currentThread().isInterrupted()) {
                            return null;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            return null;
                        }
                        BuildTaskDescriptor generateTask = HttpJsonHelper.get(BuildTaskDescriptor.class, statusUrl.getHref());
                        if (BuildStatus.IN_PROGRESS != generateTask.getStatus()) {
                            return generateTask;
                        }
                    }
                } catch (IOException | ApiException e) {
                    throw new ServerException(e);
                }
            }
        };
    }

    private void downloadGeneratedProject(BuildTaskDescriptor task, File file) throws IOException, NotFoundException {
        Link link = LinksHelper.findLink("download result", task.getLinks());

        if (link == null) {
            throw new NotFoundException("Failed to locate project download link.");
        }

        downloadPlugin.download(link.getHref(), file.getParentFile(), file.getName(), true);
    }

    private void importZipToFolder(File file, FolderEntry baseFolder)
            throws ForbiddenException, ServerException, NotFoundException, ConflictException, IOException {
        final VirtualFileSystem vfs = vfsRegistry.getProvider(baseFolder.getWorkspace()).newInstance(null);
        vfs.importZip(baseFolder.getVirtualFile().getId(), Files.newInputStream(file.toPath()), true, true);
    }
}
