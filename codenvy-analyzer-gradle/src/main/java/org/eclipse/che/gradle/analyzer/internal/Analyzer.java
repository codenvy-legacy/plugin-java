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
package org.eclipse.che.gradle.analyzer.internal;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.builder.internal.SourcesManagerImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.Cancellable;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Vladyslav Zhukovskyi
 */
public class Analyzer {
    private static final Logger LOG = LoggerFactory.getLogger(Analyzer.class);

    private static final AtomicLong processIdSequence = new AtomicLong(1);

    private final Map<Long, AnalysisProcessImpl> processes;
    private final EventService                   eventService;

    private final AtomicBoolean started;

    private ExecutorService    executor;
    private SourcesManagerImpl sourcesManager;

    public Analyzer(EventService eventService) {
        this.eventService = eventService;
        processes = new ConcurrentHashMap<>();
        started = new AtomicBoolean(false);
    }

    protected ExecutorService getExecutor() {
        return executor;
    }

    public AnalysisProcess execute(final AnalyzeRequest request) throws AnalyzerException {
        checkStarted();
        final AnalysisProcess.Callback callback = new AnalysisProcess.Callback() {
            @Override
            public void started(AnalysisProcess process) {
                final AnalyzeRequest analyzeRequest = process.getRequest();
                notify(AnalyzerEvent.startedEvent(analyzeRequest.getId(), analyzeRequest.getWorkspace(), analyzeRequest.getProject()));
            }

            @Override
            public void stopped(AnalysisProcess process) {
                final AnalyzeRequest analyzeRequest = process.getRequest();
                notify(AnalyzerEvent.stoppedEvent(analyzeRequest.getId(), analyzeRequest.getWorkspace(), analyzeRequest.getProject()));
            }

            @Override
            public void error(AnalysisProcess process, Throwable t) {
                final AnalyzeRequest analyzeRequest = process.getRequest();
                notify(AnalyzerEvent.errorEvent(analyzeRequest.getId(), analyzeRequest.getWorkspace(), analyzeRequest.getProject(),
                                                t.getMessage()));
            }

            private void notify(AnalyzerEvent re) {
                try {
                    eventService.publish(re);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        };

        return doExecute(request, callback);
    }

    protected AnalysisProcess doExecute(final AnalyzeRequest request, final AnalysisProcess.Callback callback) throws AnalyzerException {
        final Long internalId = processIdSequence.getAndIncrement();
        final Watchdog watcher = new Watchdog("WATCHDOG", request.getLifetime(), TimeUnit.SECONDS);
        final AnalysisProcessImpl process = new AnalysisProcessImpl(internalId, request, callback);
        final Runnable r = ThreadLocalPropagateContext.wrap(new Runnable() {
            @Override
            public void run() {
                try {
                    process.start();
                    watcher.start(new Cancellable() {
                        @Override
                        public void cancel() throws Exception {
                            process.getLogger().writeLine("[ERROR] Your analyze has been shutdown due to timeout.");
                            process.stop(true);
                        }
                    });

                    createGradleConnector(process, request).execute();

//                    new GradleConnector(process, request, eventService).execute();
                } catch (Throwable t) {
                    process.setError(t);
                } finally {
                    watcher.stop();
                }
            }
        });

        processes.put(internalId, process);
        final FutureTask<Void> future = new FutureTask<>(r, null);
        process.setTask(future);
        executor.execute(future);
        return process;
    }

    public AnalysisProcess getProcess(Long id) throws NotFoundException {
        AnalysisProcessImpl process = processes.get(id);
        if (process == null) {
            throw new NotFoundException(String.format("Invalid run task id: %d", id));
        }
        return process;
    }

    private GradleConnector createGradleConnector(AnalysisProcess process, AnalyzeRequest request) {
        return
    }

    protected class AnalysisProcessImpl implements AnalysisProcess {
        private final Long                     id;
        private final AnalyzeRequest           request;
        private final AnalysisProcess.Callback callback;

        private Throwable    error;
        private Future<Void> task;
        private boolean      cancelled;

        public AnalysisProcessImpl(Long id, AnalyzeRequest request, AnalysisProcess.Callback callback) {
            this.id = id;
            this.request = request;
            this.callback = callback;
        }

        synchronized void setTask(Future<Void> task) {
            this.task = task;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public AnalysisProcess getAnalysisProcess() {
            return this;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void start() throws AnalyzerException {
            if (callback != null) {
                // NOTE: important to do it in separate thread!
                getExecutor().execute(ThreadLocalPropagateContext.wrap(new Runnable() {
                    @Override
                    public void run() {
                        callback.started(AnalysisProcessImpl.this);
                    }
                }));
            }
        }

        @Override
        public void stop(boolean cancelled) throws AnalyzerException {
            this.cancelled = cancelled;
            if (task != null && !task.isDone()) {
                task.cancel(true);
            }

            if (callback != null) {
                // NOTE: important to do it in separate thread!
                getExecutor().execute(ThreadLocalPropagateContext.wrap(new Runnable() {
                    @Override
                    public void run() {
                        callback.stopped(AnalysisProcessImpl.this);
                    }
                }));
            }
        }

        @Override
        public void setError(final Throwable error) {
            this.error = error;
            if (callback != null) {
                // NOTE: important to do it in separate thread!
                getExecutor().execute(ThreadLocalPropagateContext.wrap(new Runnable() {
                    @Override
                    public void run() {
                        callback.error(AnalysisProcessImpl.this, error);
                    }
                }));
            }
        }

        @Override
        public Throwable getError() {
            return error;
        }

        @Override
        public AnalysisLogger getLogger() throws AnalyzerException {
            return AnalysisLogger.DUMMY;
        }

        @Override
        public AnalyzeRequest getRequest() {
            return request;
        }
    }

    @PostConstruct
    public void start() {
        if (started.compareAndSet(false, true)) {
            executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("-Gradle-analyzer-").setDaemon(true).build());
        } else {
            throw new IllegalStateException("Already started");
        }
        //TODO root directory is a directory where sources stored
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
    }

    @PreDestroy
    public void stop() {
        if (started.compareAndSet(true, false)) {
            boolean interrupted = false;
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOG.warn("Unable terminate main pool");
                    }
                }
            } catch (InterruptedException e) {
                interrupted = true;
                executor.shutdownNow();
            }

            processes.clear();
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        } else {
            throw new IllegalStateException("Is not started yet.");
        }
    }

    protected void checkStarted() {
        if (!started.get()) {
            throw new IllegalStateException("Is not started yet.");
        }
    }
}
