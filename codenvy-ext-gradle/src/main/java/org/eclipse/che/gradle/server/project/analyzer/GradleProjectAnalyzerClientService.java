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
package org.eclipse.che.gradle.server.project.analyzer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectService;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.gradle.analyzer.ModelRequest;
import org.eclipse.che.gradle.analyzer.events.AnalysisFinishedEvent;
import org.eclipse.che.gradle.analyzer.events.AnalysisStartEvent;
import org.eclipse.che.gradle.analyzer.events.AnalyzerResponse;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.gradle.dto.GrdProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gradle analyzer client service.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleProjectAnalyzerClientService implements EventSubscriber<AnalysisFinishedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(GradleProjectAnalyzerClientService.class);

    private ExecutorService executor;

    final ValueHolder<AnalyzerResponse> responseValueHolder = new ValueHolder<>();

    private String       apiEndPoint;
    private EventService eventService;

    @Inject
    public GradleProjectAnalyzerClientService(@Named("api.endpoint") String apiEndPoint,
                                              EventService eventService) {
        this.apiEndPoint = apiEndPoint;
        this.eventService = eventService;
    }

    @PostConstruct
    void start() {
        executor =
                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("-Gradle-model-resolver-").setDaemon(true).build());
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    /**
     * Get Gradle project configuration in synchronous mode.
     *
     * @param projectFolder
     *         project folder
     * @param request
     *         configuration type
     * @return Gradle configuration build from project
     */
    public GrdConfiguration getGrdConfiguration(FolderEntry projectFolder, ModelRequest request) {
        AnalyzerResponse analyzerResponse = doWork(projectFolder, request);

        if (!analyzerResponse.isSuccessful()) {
            return DtoFactory.getInstance().createDto(GrdConfiguration.class)
                             .withConfigurationState(GrdConfiguration.State.OUTDATED);
        }

        GrdProject grdProject = DtoFactory.getInstance().createDtoFromJson(analyzerResponse.getModel(), GrdProject.class);

        return DtoFactory.getInstance().createDto(GrdConfiguration.class)
                         .withProject(grdProject)
                         .withConfigurationState(GrdConfiguration.State.ACTUAL);
    }

    /**
     * Get Gradle project configuration in asynchronous mode.
     *
     * @param projectFolder
     *         project folder
     * @param request
     *         configuration type
     * @param receivedHandler
     *         callback
     */
    public void getAsyncProjectModel(final FolderEntry projectFolder,
                                     final ModelRequest request,
                                     final GradleProjectAnalyzerCallback receivedHandler) {
        Runnable wrapped = ThreadLocalPropagateContext.wrap(new Runnable() {
            @Override
            public void run() {
                AnalyzerResponse analyzerResponse = doWork(projectFolder, request);

                if (analyzerResponse.isSuccessful()) {
                    GrdProject grdProject = DtoFactory.getInstance().createDtoFromJson(analyzerResponse.getModel(), GrdProject.class);
                    GrdConfiguration grdConfiguration = DtoFactory.getInstance().createDto(GrdConfiguration.class)
                                                                  .withProject(grdProject)
                                                                  .withConfigurationState(GrdConfiguration.State.ACTUAL);
                    receivedHandler.onSuccess(grdConfiguration);
                } else {
                    receivedHandler.onFailed(analyzerResponse.getErrorOutput());
                }
            }
        });
        executor.submit(wrapped);
    }

    /** Perform analyze and return analyze response. */
    private synchronized AnalyzerResponse doWork(FolderEntry projectFolder, ModelRequest request) {
        String sourceLink;

        try {
            sourceLink = getZipBallLink(projectFolder.getWorkspace(), projectFolder.getPath());
        } catch (ApiException e) {
            return new AnalyzerResponse().withErrorOutput(e.getMessage()).withSuccessful(false);
        }

        eventService.subscribe(this);
        eventService.publish(new AnalysisStartEvent(projectFolder.getWorkspace(), projectFolder.getPath(), sourceLink, request));

        synchronized (this) {
            while (responseValueHolder.get() == null) {
                try {
                    LOG.info("Waiting for model constructing.");
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        AnalyzerResponse analyzerResponse = responseValueHolder.get();
        responseValueHolder.set(null);

        return analyzerResponse;
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(AnalysisFinishedEvent event) {
        synchronized (this) {
            responseValueHolder.set(event.getResponse());
            LOG.info("Model constructed.");
            notifyAll();
        }
    }

    /** Fetch export link from project path. */
    private String getZipBallLink(String workspace, String projectPath)
            throws UnauthorizedException, ForbiddenException, ConflictException, NotFoundException, ServerException {
        final UriBuilder baseProjectUriBuilder = UriBuilder.fromUri(apiEndPoint);
        final String projectUrl = baseProjectUriBuilder.path(ProjectService.class)
                                                       .path(ProjectService.class, "getProject")
                                                       .build(workspace,
                                                              projectPath.startsWith("/") ? projectPath.substring(1) : projectPath)
                                                       .toString();

        try {
            ProjectDescriptor descriptor = HttpJsonHelper.get(ProjectDescriptor.class, projectUrl);
            final Link zipBallLink = descriptor.getLink(org.eclipse.che.api.project.server.Constants.LINK_REL_EXPORT_ZIP);
            if (zipBallLink != null) {
                final String zipBallLinkHref = zipBallLink.getHref();
                final String token = getAuthenticationToken();

                return token != null ? String.format("%s?token=%s", zipBallLinkHref, token) : zipBallLinkHref;
            }
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }

        return null;
    }

    /** Fetch authentication token from current environment context. */
    private String getAuthenticationToken() {
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null) {
            return user.getToken();
        }
        return null;
    }
}
