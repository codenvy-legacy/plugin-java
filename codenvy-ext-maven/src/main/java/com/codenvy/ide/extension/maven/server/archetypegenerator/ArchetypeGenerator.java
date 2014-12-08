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
package com.codenvy.ide.extension.maven.server.archetypegenerator;

import com.codenvy.api.core.util.CommandLine;
import com.codenvy.api.core.util.ProcessUtil;
import com.codenvy.api.core.util.StreamPump;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.NamedThreadFactory;
import com.codenvy.commons.lang.ZipUtils;
import com.codenvy.ide.maven.tools.MavenUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates projects from Maven-archetype.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class ArchetypeGenerator {

    private static final Logger     LOG            = LoggerFactory.getLogger(ArchetypeGenerator.class);
    private static final AtomicLong taskIdSequence = new AtomicLong(1);
    private final ConcurrentMap<Long, GenerateTask> tasks;
    /**
     * Time of keeping the results (generated projects and logs) of generating.
     * After this time the results of generating may be removed.
     */
    private final long                              keepResultTimeMillis;
    private       ExecutorService                   executor;
    private       ScheduledExecutorService          scheduler;
    private       java.io.File                      projectsFolder;

    @Inject
    public ArchetypeGenerator() {
        this.keepResultTimeMillis = TimeUnit.SECONDS.toMillis(60);
        tasks = new ConcurrentHashMap<>();
    }

    /** Initialize generator. */
    @PostConstruct
    private void start() {
        projectsFolder = new java.io.File(System.getProperty("java.io.tmpdir"), "archetype-generator");
        if (!(projectsFolder.exists() || projectsFolder.mkdirs())) {
            throw new IllegalStateException(String.format("Unable to create directory %s", projectsFolder.getAbsolutePath()));
        }
        executor = Executors.newCachedThreadPool(new NamedThreadFactory("ArchetypeGenerator-", true));
        scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ArchetypeGeneratorSchedulerPool-", true));
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                int num = 0;
                for (Iterator<GenerateTask> i = tasks.values().iterator(); i.hasNext(); ) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    final GenerateTask task = i.next();
                    if (task.isExpired()) {
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
        }, 1, 1, TimeUnit.MINUTES);
    }

    /** Stops generator and releases any associated resources. */
    @PreDestroy
    private void stop() {
        boolean interrupted = false;
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warn("Unable terminate scheduler");
            }
        } catch (InterruptedException e) {
            interrupted = true;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable terminate main pool");
                }
            }
        } catch (InterruptedException e) {
            interrupted |= true;
            executor.shutdownNow();
        }
        final java.io.File[] files = projectsFolder.listFiles();
        if (files != null && files.length > 0) {
            for (java.io.File f : files) {
                boolean deleted;
                if (f.isDirectory()) {
                    deleted = IoUtil.deleteRecursive(f);
                } else {
                    deleted = f.delete();
                }
                if (!deleted) {
                    LOG.warn("Failed delete {}", f);
                }
            }
        }
        tasks.clear();
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    java.io.File getProjectsDirectory() {
        return projectsFolder;
    }

    private GeneratorConfigurationFactory getGeneratorConfigurationFactory() {
        return new GeneratorConfigurationFactory(this);
    }

    /**
     * Generates a new project from the specified archetype.
     *
     * @param archetype
     *         archetype from which need to generate new project
     * @param groupId
     *         groupId of new project
     * @param artifactId
     *         artifactId of new project
     * @param version
     *         version of new project
     * @param options
     *         additional properties for archetype. May be {@code null}
     * @return generating result
     * @throws GeneratorException
     */
    public GenerateResult generateFromArchetype(@Nonnull MavenArchetype archetype, @Nonnull String groupId, @Nonnull String artifactId,
                                                @Nonnull String version, @Nullable Map<String, String> options) throws GeneratorException {
        Map<String, String> myOptions = new HashMap<>();
        myOptions.put("-DinteractiveMode", "false"); // get rid of the interactivity of the archetype plugin
        myOptions.put("-DarchetypeGroupId", archetype.getGroupId());
        myOptions.put("-DarchetypeArtifactId", archetype.getArtifactId());
        myOptions.put("-DarchetypeVersion", archetype.getVersion());
        if (archetype.getRepository() != null) {
            myOptions.put("-DarchetypeRepository", archetype.getRepository());
        }
        myOptions.put("-DgroupId", groupId);
        myOptions.put("-DartifactId", artifactId);
        myOptions.put("-Dversion", version);
        if (options != null) {
            myOptions.putAll(options);
        }

        GenerateTask task = generate(artifactId, myOptions);
        while (!task.isDone()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return task.getResult();
    }

    /** Starts new project generating process. */
    private GenerateTask generate(String artifactId, Map<String, String> options) throws GeneratorException {
        final GeneratorConfiguration configuration = getGeneratorConfigurationFactory().createConfiguration(artifactId, options);
        final java.io.File workDir = configuration.getWorkDir();
        final java.io.File logFile = new java.io.File(workDir.getParentFile(), workDir.getName() + ".log");
        final GeneratorLogger logger = createLogger(logFile);
        final CommandLine commandLine = createCommandLine(configuration);
        final Callable<Boolean> callable = createTaskFor(commandLine, logger, configuration);
        final Long internalId = taskIdSequence.getAndIncrement();
        final GenerateTask task = new GenerateTask(callable, internalId, configuration, logger);
        tasks.put(internalId, task);
        executor.execute(task);
        return task;
    }

    private GeneratorLogger createLogger(File logFile) throws GeneratorException {
        try {
            return new GeneratorLogger(logFile);
        } catch (IOException e) {
            throw new GeneratorException(e);
        }
    }

    private CommandLine createCommandLine(GeneratorConfiguration config) throws GeneratorException {
        final CommandLine commandLine = new CommandLine(MavenUtils.getMavenExecCommand());
        commandLine.add("--batch-mode");
        commandLine.add("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate");
        commandLine.add(config.getOptions());
        return commandLine;
    }

    private Callable<Boolean> createTaskFor(final CommandLine commandLine,
                                            final GeneratorLogger logger,
                                            final GeneratorConfiguration configuration) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                StreamPump output = null;
                int result = -1;
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder().command(commandLine.toShellCommand()).directory(
                            configuration.getWorkDir()).redirectErrorStream(true);
                    Process process = processBuilder.start();

                    output = new StreamPump();
                    output.start(process, logger);
                    try {
                        result = process.waitFor();
                    } catch (InterruptedException e) {
                        Thread.interrupted(); // we interrupt thread when cancel task
                        ProcessUtil.kill(process);
                    }
                    try {
                        output.await(); // wait for logger
                    } catch (InterruptedException e) {
                        Thread.interrupted(); // we interrupt thread when cancel task, NOTE: logs may be incomplete
                    }
                } finally {
                    if (output != null) {
                        output.stop();
                    }
                }
                LOG.debug("Done: {}, exit code: {}", commandLine, result);
                return result == 0;
            }
        };
    }

    /**
     * Gets result of GenerateTask.
     *
     * @param task
     *         task
     * @param successful
     *         reports whether generate process terminated normally or not.
     *         Note: {@code true} is not indicated successful generating but only normal process termination.
     * @return GenerateResult
     * @throws GeneratorException
     *         if an error occurs when try to get result
     * @see com.codenvy.ide.extension.maven.server.archetypegenerator.ArchetypeGenerator.GenerateTask#getResult()
     */
    private GenerateResult getTaskResult(GenerateTask task, boolean successful) throws GeneratorException {
        if (!successful) {
            return new GenerateResult(false, getReport(task));
        }

        boolean mavenSuccess = false;
        BufferedReader logReader = null;
        try {
            logReader = new BufferedReader(task.getLogger().getReader());
            String line;
            while ((line = logReader.readLine()) != null) {
                line = MavenUtils.removeLoggerPrefix(line);
                if ("BUILD SUCCESS".equals(line)) {
                    mavenSuccess = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new GeneratorException(e);
        } finally {
            if (logReader != null) {
                try {
                    logReader.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (!mavenSuccess) {
            return new GenerateResult(false, getReport(task));
        }

        final GeneratorConfiguration config = task.getConfiguration();
        final java.io.File workDir = config.getWorkDir();
        final GenerateResult result = new GenerateResult(true, getReport(task));

        final java.io.File projectFolder = new java.io.File(workDir, config.getArtifactId());
        if (projectFolder.isDirectory() && projectFolder.list().length > 0) {
            final java.io.File zip = new java.io.File(workDir, "project.zip");
            try {
                ZipUtils.zipDir(projectFolder.getAbsolutePath(), projectFolder, zip, IoUtil.ANY_FILTER);
            } catch (IOException e) {
                throw new GeneratorException(e);
            }
            result.setResult(zip);
        }

        return result;
    }

    /** Get generating task report or {@code null} if report is not available. */
    private java.io.File getReport(GenerateTask task) {
        final java.io.File workDir = task.getConfiguration().getWorkDir();
        final java.io.File logFile = new java.io.File(workDir.getParentFile(), workDir.getName() + ".log");
        return logFile.exists() ? logFile : null;
    }

    /** Cleanup task - remove all local files which were created during project generating process, e.g logs, generated project, etc. */
    private void cleanup(GenerateTask task) {
        final GeneratorConfiguration configuration = task.getConfiguration();
        final java.io.File workDir = configuration.getWorkDir();
        if (workDir != null && workDir.exists()) {
            if (!IoUtil.deleteRecursive(workDir)) {
                LOG.warn("Unable delete directory {}", workDir);
            }
        }
        final java.io.File log = task.getLogger().getFile();
        if (log != null && log.exists()) {
            if (!log.delete()) {
                LOG.warn("Unable delete file {}", log);
            }
        }
        GenerateResult result = null;
        try {
            result = task.getResult();
        } catch (GeneratorException e) {
            LOG.error("Skip cleanup of the task {}. Unable get task result.", task);
        }
        if (result != null) {
            java.io.File artifact = result.getResult();
            if (artifact.exists()) {
                if (!artifact.delete()) {
                    LOG.warn("Unable delete file {}", artifact);
                }
            }
            if (result.hasGenerateReport()) {
                java.io.File report = result.getGenerateReport();
                if (report != null && report.exists()) {
                    if (!report.delete()) {
                        LOG.warn("Unable delete file {}", report);
                    }
                }
            }
        }
        final java.io.File projectDir = configuration.getProjectDir();
        if (projectDir != null && projectDir.exists()) {
            if (!IoUtil.deleteRecursive(projectDir)) {
                LOG.warn("Unable delete directory {}", projectDir);
            }
        }
    }

    private class GenerateTask extends FutureTask<Boolean> {
        private final Long                   id;
        private final GeneratorConfiguration configuration;
        private final GeneratorLogger        logger;

        private GenerateResult result;
        /** Time when task was done (successfully ends, fails, cancelled) or -1 if task is not done yet. */
        private long           endTime;

        GenerateTask(Callable<Boolean> callable,
                     Long id,
                     GeneratorConfiguration configuration,
                     GeneratorLogger logger) {
            super(callable);
            this.id = id;
            this.configuration = configuration;
            this.logger = logger;
            endTime = -1L;
        }

        final Long getId() {
            return id;
        }

        @Override
        protected void done() {
            super.done();
            endTime = System.currentTimeMillis();
            try {
                logger.close();
                LOG.debug("Close logger {}", logger);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        GeneratorLogger getLogger() {
            return logger;
        }

        /**
         * Get result of project generating.
         *
         * @return result of project generating or {@code null} if task is not done yet
         * @throws GeneratorException
         *         if an error occurs when try to start project generating process or get its result.
         */
        final GenerateResult getResult() throws GeneratorException {
            if (!isDone()) {
                return null;
            }
            if (result == null) {
                boolean successful;
                try {
                    successful = super.get();
                } catch (InterruptedException e) {
                    // Should not happen since we checked is task done or not.
                    Thread.currentThread().interrupt();
                    successful = false;
                } catch (ExecutionException e) {
                    final Throwable cause = e.getCause();
                    if (cause instanceof Error) {
                        throw (Error)cause;
                    } else if (cause instanceof GeneratorException) {
                        throw (GeneratorException)cause;
                    } else {
                        throw new GeneratorException(cause.getMessage(), cause);
                    }
                } catch (CancellationException ce) {
                    successful = false;
                }
                result = ArchetypeGenerator.this.getTaskResult(this, successful);
            }
            return result;
        }

        GeneratorConfiguration getConfiguration() {
            return configuration;
        }

        synchronized boolean isExpired() {
            return endTime > 0 && (endTime + keepResultTimeMillis) < System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "GenerateTask{" +
                   "id=" + id +
                   ", workDir=" + configuration.getWorkDir() +
                   '}';
        }
    }
}
