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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import static com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask.Status.FAILED;
import static com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask.Status.SUCCESSFUL;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GENERATOR_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Generates sample Maven project using maven-archetype-quickstart.
 *
 * @author Artem Zatsarynnyy
 */
public class ArchetypeProjectGenerator implements ProjectGenerator {
    private static final Logger     LOG            = LoggerFactory.getLogger(ArchetypeProjectGenerator.class);
    private static final AtomicLong taskIdSequence = new AtomicLong(1);
    private final ConcurrentMap<Long, GenerateTaskCallable> tasks;
    private final String                                    generatorServiceUrl;
    private final VirtualFileSystemRegistry                 vfsRegistry;
    private final DownloadPlugin downloadPlugin = new HttpDownloadPlugin();
    private ExecutorService          executor;
    private ScheduledExecutorService scheduler;

    @Inject
    public ArchetypeProjectGenerator(@Named("archetype_generator_service.url") String generatorServiceUrl,
                                     VirtualFileSystemRegistry vfsRegistry) {
        this.generatorServiceUrl = generatorServiceUrl;
        this.vfsRegistry = vfsRegistry;
        tasks = new ConcurrentHashMap<>();
    }

    @Override
    public String getId() {
        return ARCHETYPE_GENERATOR_ID;
    }

    @Override
    public String getProjectTypeId() {
        return MAVEN_ID;
    }

    @PostConstruct
    void start() {
        executor = Executors.newCachedThreadPool(new NamedThreadFactory("-ProjectGenerator-" + getId() + "-", true));
        scheduler =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("-ProjectGeneratorSchedulerPool-" + getId() + "-", true));
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                int num = 0;
                for (Iterator<GenerateTaskCallable> i = tasks.values().iterator(); i.hasNext(); ) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    final GenerateTaskCallable task = i.next();
                    if (task.isDone()) {
                        i.remove();
                        try {
                            cleanup(task);
                        } catch (RuntimeException e) {
                            LOG.error(e.getMessage(), e);
                        }
                        num++;
                    }
                }
                if (num > 0) {
                    LOG.debug("Remove {} expired tasks", num);
                }
            }
        }, 1, 1, MINUTES);
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
        scheduler.shutdownNow();
        tasks.clear();
    }

    @Override
    public void generateProject(final FolderEntry baseFolder, NewProject newProjectDescriptor)
            throws ForbiddenException, ConflictException, ServerException {
        Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();
        List<String> artifactId = attributes.get(ARTIFACT_ID);
        List<String> groupId = attributes.get(GROUP_ID);
        List<String> version = attributes.get(VERSION);
        if (groupId == null || groupId.isEmpty() || artifactId == null || artifactId.isEmpty() || version == null || version.isEmpty()) {
            throw new ServerException("Missed some required attribute (groupId, artifactId or version)");
        }

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;
        Map<String, String> options = new HashMap<>();

        for (Map.Entry<String, String> entry : newProjectDescriptor.getGeneratorDescription().getOptions().entrySet()) {
            switch (entry.getKey()) {
                case "archetypeGroupId":
                    archetypeGroupId = entry.getValue();
                    break;
                case "archetypeArtifactId":
                    archetypeArtifactId = entry.getValue();
                    break;
                case "archetypeVersion":
                    archetypeVersion = entry.getValue();
                    break;
                default:
                    options.put(entry.getKey(), entry.getValue());
            }
        }

        if (archetypeGroupId == null || archetypeGroupId.isEmpty() ||
            archetypeArtifactId == null || archetypeArtifactId.isEmpty() ||
            archetypeVersion == null || archetypeVersion.isEmpty()) {
            throw new ServerException("Missed some required option (archetypeGroupId, archetypeArtifactId or archetypeVersion)");
        }

        final Long internalId = taskIdSequence.getAndIncrement();
        try {
            final File downloadFolder = Files.createTempDirectory("generated-project-").toFile();
            final GenerateTaskCallable callable = new GenerateTaskCallable(generatorServiceUrl, internalId,
                                                                           archetypeGroupId, archetypeArtifactId, archetypeVersion,
                                                                           groupId.get(0), artifactId.get(0), version.get(0), options,
                                                                           downloadFolder);
            tasks.put(internalId, callable);
            Future<GenerateTask> futureTask = executor.submit(callable);
            GenerateTask task = futureTask.get();
            if (task.getStatus() == SUCCESSFUL) {
                final File result = downloadTaskResult(task, downloadFolder);
                copyFileToRemoteFolder(result, baseFolder);
            } else if (task.getStatus() == FAILED) {
                throw new ServerException(task.getReport().isEmpty() ? "Failed to generate project: " : task.getReport());
            }
            callable.done();
        } catch (NotFoundException | InterruptedException | ExecutionException | IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    private File downloadTaskResult(GenerateTask generateTask, File downloadFolder) throws ServerException, IOException {
        final ValueHolder<File> resultHolder = new ValueHolder<>();
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
        downloadPlugin.download(generateTask.getDownloadUrl(), downloadFolder, callback);
        final IOException ioError = errorHolder.get();
        if (ioError != null) {
            throw new ServerException(ioError);
        }
        return resultHolder.get();
    }

    private void copyFileToRemoteFolder(File file, FolderEntry baseFolder)
            throws ForbiddenException, ServerException, NotFoundException, ConflictException, IOException {
        final VirtualFileSystem vfs = vfsRegistry.getProvider(baseFolder.getWorkspace()).newInstance(null);
        vfs.importZip(baseFolder.getVirtualFile().getId(), Files.newInputStream(file.toPath()), true, false);
    }

    private void cleanup(GenerateTaskCallable task) {
        final File downloadFolder = task.getDownloadFolder();
        if (downloadFolder != null && downloadFolder.exists()) {
            if (!downloadFolder.delete()) {
                LOG.warn("Unable to delete file {}", downloadFolder);
            }
        }
    }
}
