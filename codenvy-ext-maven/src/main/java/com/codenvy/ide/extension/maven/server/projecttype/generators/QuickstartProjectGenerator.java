/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.maven.server.projecttype.generators;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.util.DownloadPlugin;
import com.codenvy.api.core.util.HttpDownloadPlugin;
import com.codenvy.api.core.util.ValueHolder;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectGenerator;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystem;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.commons.lang.NamedThreadFactory;
import com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask;
import com.google.inject.name.Named;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask.Status.FAILED;
import static com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask.Status.SUCCESSFUL;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.QUICKSTART_GENERATOR_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Generates sample Maven project using maven-archetype-quickstart.
 *
 * @author Artem Zatsarynnyy
 */
public class QuickstartProjectGenerator implements ProjectGenerator {
    private static final String ARCHETYPE_GROUP_ID    = "org.apache.maven.archetypes";
    private static final String ARCHETYPE_ARTIFACT_ID = "maven-archetype-quickstart";
    private static final String ARCHETYPE_VERSION     = "RELEASE";
    private final String                    generatorServiceUrl;
    private final VirtualFileSystemRegistry vfsRegistry;
    private final DownloadPlugin downloadPlugin = new HttpDownloadPlugin();
    private ExecutorService executor;

    @Inject
    public QuickstartProjectGenerator(@Named("archetype_generator_service.url") String generatorServiceUrl,
                                      VirtualFileSystemRegistry vfsRegistry) {
        this.generatorServiceUrl = generatorServiceUrl;
        this.vfsRegistry = vfsRegistry;
    }

    @Override
    public String getId() {
        return QUICKSTART_GENERATOR_ID;
    }

    @Override
    public String getProjectTypeId() {
        return MAVEN_ID;
    }

    @PostConstruct
    void start() {
        executor = Executors.newCachedThreadPool(new NamedThreadFactory("-ProjectGenerator-" + getId() + "-", true));
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    @Override
    public void generateProject(final FolderEntry baseFolder, NewProject newProjectDescriptor)
            throws ForbiddenException, ConflictException, ServerException {
        Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();
        List<String> artifactId = attributes.get(ARTIFACT_ID);
        List<String> groupId = attributes.get(GROUP_ID);
        List<String> version = attributes.get(VERSION);
        if (artifactId == null || artifactId.isEmpty() || groupId == null || groupId.isEmpty() || version == null || version.isEmpty()) {
            throw new ServerException("Missed some required attribute (artifactId, groupId or version)");
        }

        Map<String, String> options = new HashMap<>();
        options.put("-Dpackage", "app");
        GenerateTaskCallable callable = new GenerateTaskCallable(generatorServiceUrl,
                                                                 ARCHETYPE_GROUP_ID, ARCHETYPE_ARTIFACT_ID, ARCHETYPE_VERSION,
                                                                 groupId.get(0), artifactId.get(0), version.get(0), options);
        Future<GenerateTask> futureTask = executor.submit(callable);
        try {
            GenerateTask task = futureTask.get();
            if (task.getStatus() == SUCCESSFUL) {
                download(task, baseFolder);
            } else if (task.getStatus() == FAILED) {
                throw new ServerException(task.getReport().isEmpty() ? "Failed to generate project" : task.getReport());
            }
        } catch (NotFoundException | InterruptedException | ExecutionException | IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    private void download(GenerateTask generateTask, FolderEntry baseFolder)
            throws ServerException, ConflictException, ForbiddenException, IOException, NotFoundException {
        final ValueHolder<java.io.File> resultHolder = new ValueHolder<>();
        final ValueHolder<IOException> errorHolder = new ValueHolder<>();
        DownloadPlugin.Callback callback = new DownloadPlugin.Callback() {
            @Override
            public void done(File downloaded) {
                resultHolder.set(downloaded);
            }

            @Override
            public void error(IOException e) {
                errorHolder.set(e);
            }
        };
        downloadPlugin.download(generateTask.getDownloadUrl(), Files.createTempDirectory("generated-project-").toFile(), callback);
        final IOException ioError = errorHolder.get();
        if (ioError != null) {
            throw new ServerException(ioError);
        }
        copyFileToRemoteFolder(resultHolder.get(), baseFolder);
    }

    private void copyFileToRemoteFolder(File file, FolderEntry baseFolder)
            throws ForbiddenException, ServerException, NotFoundException, ConflictException, IOException {
        final VirtualFileSystem vfs = vfsRegistry.getProvider(baseFolder.getWorkspace()).newInstance(null);
        vfs.importZip(baseFolder.getVirtualFile().getId(), Files.newInputStream(file.toPath()), true, false);
    }
}
