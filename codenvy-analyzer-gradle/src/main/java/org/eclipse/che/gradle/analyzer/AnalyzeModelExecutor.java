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
package org.eclipse.che.gradle.analyzer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

import org.eclipse.che.api.builder.internal.BuildLogger;
import org.eclipse.che.api.builder.internal.SourceManagerEvent;
import org.eclipse.che.api.builder.internal.SourceManagerListener;
import org.eclipse.che.api.builder.internal.SourcesManagerImpl;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.gradle.analyzer.events.AnalysisFinishedEvent;
import org.eclipse.che.gradle.analyzer.events.AnalysisStartEvent;
import org.eclipse.che.gradle.analyzer.events.AnalyzerResponse;
import org.eclipse.che.gradle.analyzer.model.TransportableJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Provide mechanism to run project analysis. Result of work is the build project model which produced by {@link .GradleModelBuilder}.
 * Project model represent simple json string which can be de-serialized into complex DTO object.
 *
 * @author Vladyslav Zhukovskii
 */
public class AnalyzeModelExecutor implements EventSubscriber<AnalysisStartEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyzeModelExecutor.class);

    private ExecutorService    executor;
    private File               rootDirectory;
    private EventService       eventService;
    private SourcesManagerImpl sourcesManager;

    /** New instance of {@link AnalyzeModelExecutor}. */
    @Inject
    public AnalyzeModelExecutor(@Named(Constants.BASE_DIRECTORY) File rootDirectory,
                                EventService eventService) {
        this.rootDirectory = rootDirectory;
        this.eventService = eventService;
        LOG.info("AnalyzeModelExecutor constructor executed");
    }

    /** Initializing instructions. */
    @PostConstruct
    void start() {
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("-GradleAnalyzer-").setDaemon(true).build());
        final File repository = new File(rootDirectory, "gradle");
        if (!(repository.exists() || repository.mkdirs())) {
            throw new IllegalStateException(String.format("Unable create directory %s", repository.getAbsolutePath()));
        }
        final File sources = new File(repository, "sources");
        if (!(sources.exists() || sources.mkdirs())) {
            throw new IllegalStateException(String.format("Unable create directory %s", sources.getAbsolutePath()));
        }
        sourcesManager = new SourcesManagerImpl(sources);
        sourcesManager.start();
        eventService.subscribe(this);
        LOG.info("Post construct executed");
    }

    /** Shutdown instructions. */
    @PreDestroy
    void stop() {
        executor.shutdownNow();
        sourcesManager.stop();
        eventService.unsubscribe(this);
        LOG.info("pre destroy executed");
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(AnalysisStartEvent event) {
        try {
            LOG.info("AnalysisStartEvent event fired");
            Path futureWorkDir = Paths.get(sourcesManager.getDirectory().getAbsolutePath(), event.getWorkspace(), event.getProject());
            Path workDir = Files.createDirectories(futureWorkDir);

            sourcesManager.addListener(new GradleSourcesManager(workDir.toFile(), event.getRequest()));
            sourcesManager.getSources(BuildLogger.DUMMY,
                                      event.getWorkspace(),
                                      event.getProject(),
                                      event.getSourcesZipBallLink(),
                                      workDir.toFile());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            sendFiledAnalyzedEvent(e.getMessage());
        }
    }

    /**
     * Implementation of {@link SourceManagerListener} which perform downloading project sources and run
     * analysis.
     */
    private class GradleSourcesManager implements SourceManagerListener {
        private File         baseWorkDir;
        private ModelRequest request;

        /** New instance of {@link GradleSourcesManager} */
        private GradleSourcesManager(File baseWorkDir, ModelRequest request) {
            this.baseWorkDir = baseWorkDir;
            this.request = request;
        }

        /** {@inheritDoc} */
        @Override
        public void afterDownload(SourceManagerEvent event) {
            if (!baseWorkDir.equals(event.getWorkDir())) {
                return;
            }

            sourcesManager.removeListener(this);

            FutureTask<TransportableJSON> task = scheduleAnalysis(event.getWorkDir(), request);

            try {
                while (!task.isDone()) {
                    Thread.sleep(500); //sleep for 0.5 sec
                }

                TransportableJSON model = task.get();

                AnalyzerResponse response = new AnalyzerResponse().withSuccessful(true)
                                                                  .withModel(model.getJSON());
                eventService.publish(new AnalysisFinishedEvent(response));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof AnalyzeModelException) {
                    String errStdOutput = ((AnalyzeModelException)e.getCause()).getErrStdOutput();
                    sendFiledAnalyzedEvent(errStdOutput);
                } else {
                    sendFiledAnalyzedEvent(e.getMessage());
                }

                LOG.error("Failed to get project model.", e);
            }
        }
    }

    /** Run new analysis task and return {@link java.util.concurrent.Future} object. */
    public FutureTask<TransportableJSON> scheduleAnalysis(File workDir, ModelRequest request) {
        AnalyzeModelTask analyzeModelTask = new AnalyzeModelTask(workDir, request);
        FutureTask<TransportableJSON> task = new FutureTask<>(analyzeModelTask);
        executor.execute(task);

        return task;
    }

    /** Send websocket message to server (API) that something went wrong. */
    private void sendFiledAnalyzedEvent(String message) {
        AnalyzerResponse response = new AnalyzerResponse().withSuccessful(false)
                                                          .withErrorOutput(message);

        eventService.publish(new AnalysisFinishedEvent(response));
    }

}
