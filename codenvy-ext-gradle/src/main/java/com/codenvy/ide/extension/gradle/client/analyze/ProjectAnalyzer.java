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
package com.codenvy.ide.extension.gradle.client.analyze;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.ProjectDescriptorChangedEvent;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.extension.builder.client.console.BuilderConsolePresenter;
import com.codenvy.ide.extension.gradle.client.GradleClientService;
import com.codenvy.ide.extension.gradle.client.GradleExtension;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.event.ProjectConfigurationReceivedEvent;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.util.loging.Log;
import com.codenvy.ide.websocket.MessageBus;
import com.codenvy.ide.websocket.WebSocketException;
import com.codenvy.ide.websocket.rest.SubscriptionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Project analysis action service. Perform start execution and handle analysis status.
 *
 * @author Vladyslav Zhukovskii
 * @see com.codenvy.ide.gradle.dto.GrdConfiguration
 */
@Singleton
public class ProjectAnalyzer {

    private GradleClientService        clientService;
    private DtoUnmarshallerFactory     dtoUnmarshallerFactory;
    private NotificationManager        notificationManager;
    private MessageBus                 messageBus;
    private EventBus                   eventBus;
    private GradleLocalizationConstant localization;
    private ProjectServiceClient       projectServiceClient;
    private BuilderConsolePresenter    builderConsolePresenter;
    private DependenciesUpdater        dependenciesUpdater;

    private Notification analysisStatus;

    @Inject
    public ProjectAnalyzer(GradleClientService clientService,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           NotificationManager notificationManager,
                           MessageBus messageBus,
                           EventBus eventBus,
                           GradleLocalizationConstant localization,
                           ProjectServiceClient projectServiceClient,
                           BuilderConsolePresenter builderConsolePresenter,
                           DependenciesUpdater dependenciesUpdater) {
        this.clientService = clientService;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.messageBus = messageBus;
        this.eventBus = eventBus;
        this.localization = localization;
        this.projectServiceClient = projectServiceClient;
        this.builderConsolePresenter = builderConsolePresenter;
        this.dependenciesUpdater = dependenciesUpdater;
    }

    /**
     * Perform project analysis. Gradle analyzer on Builder instance parse Gradle project with tooling api, build raw json and store
     * project
     * configuration in root project directory.
     *
     * @param project
     *         project on which analysis should be performed
     */
    public void analyzeProject(@Nonnull final ProjectDescriptor project) {
        AsyncRequestCallback<Void> callback = new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                analysisStatus = new Notification(localization.analyzeProjectExecuted(),
                                                  Notification.Type.INFO,
                                                  Notification.Status.PROGRESS);
                analysisStatus.setImportant(true);

                notificationManager.showNotification(analysisStatus);

                try {
                    messageBus.subscribe(getAnalysisChannel(project), new AnalysisHandler(project));
                } catch (WebSocketException e) {
                    Log.error(ProjectAnalyzer.class, e);
                }
            }

            @Override
            protected void onFailure(Throwable e) {
                notificationManager.showWarning(localization.analyzeProjectExecutionFailed());
                Log.error(ProjectAnalyzer.class, e);
            }
        };

        clientService.analyzeProject(project.getPath(), callback);
    }

    /** Check whether analyzer perform any analysis task or not. */
    public boolean isBusy() {
        return !(analysisStatus == null || analysisStatus.isFinished());
    }

    /**
     * Get current stored project configuration of project. If no configuration was found default {@link
     * com.codenvy.ide.gradle.dto.GrdConfiguration} will be constructed with outdated status.
     */
    public void getGrdConfiguration(@Nonnull ProjectDescriptor project, @Nonnull final AsyncCallback<GrdConfiguration> callback) {
        Unmarshallable<GrdConfiguration> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(GrdConfiguration.class);

        AsyncRequestCallback<GrdConfiguration> asyncCallback = new AsyncRequestCallback<GrdConfiguration>(unmarshaller) {
            @Override
            protected void onSuccess(GrdConfiguration grdConfiguration) {
                callback.onSuccess(grdConfiguration);
            }

            @Override
            protected void onFailure(Throwable e) {
                callback.onFailure(e);
            }
        };

        clientService.getProjectModel(project.getPath(), asyncCallback);
    }

    /** Handles websocket messages to fetch Gradle project configuration. */
    private class AnalysisHandler extends SubscriptionHandler<GrdConfiguration> {
        private ProjectDescriptor project;

        private AnalysisHandler(@Nonnull ProjectDescriptor project) {
            super(dtoUnmarshallerFactory.newWSUnmarshaller(GrdConfiguration.class));
            this.project = project;
        }

        /** {@inheritDoc} */
        @Override
        protected void onMessageReceived(GrdConfiguration grdConfiguration) {
            analysisStatus.setMessage(localization.analyzeProjectExecutionSuccess());

            synchronizeProjectWithModel(project, grdConfiguration);

            try {
                messageBus.unsubscribe(getAnalysisChannel(project), this);
            } catch (WebSocketException e) {
                analysisStatus.setStatus(Notification.Status.FINISHED);
                Log.error(ProjectAnalyzer.class, e);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void onErrorReceived(Throwable e) {
            analysisStatus.setMessage(localization.analyzeProjectFailed());
            analysisStatus.setType(Notification.Type.ERROR);
            analysisStatus.setStatus(Notification.Status.FINISHED);

            builderConsolePresenter.print(e.getMessage());
            builderConsolePresenter.setActive();

            if (messageBus.isHandlerSubscribed(this, getAnalysisChannel(project))) {
                try {
                    messageBus.unsubscribe(getAnalysisChannel(project), this);
                } catch (WebSocketException e1) {
                    Log.error(ProjectAnalyzer.class, e1);
                }
            }
        }
    }

    /**
     * Perform resolving project from fetched Gradle project configuration. We need to do this due to our websocket event service which
     * doesn't support providing current Environment Context, so Codenvy received configuration, sent through events to rest presenters and
     * run process to resolving project. For example if user provide new module in build script, project resolving will create it.
     */
    private void synchronizeProjectWithModel(@Nonnull final ProjectDescriptor project, @Nonnull final GrdConfiguration grdConfiguration) {
        analysisStatus.setMessage(localization.analyzeProjectRegenerationExecuted());

        AsyncRequestCallback<Void> callback = new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                analysisStatus.setMessage(localization.analyzeProjectRegenerationExecutionSuccessful());
                analysisStatus.setStatus(Notification.Status.FINISHED);
                getUpdatedProjectDescriptor(grdConfiguration, project);
            }

            @Override
            protected void onFailure(Throwable e) {
                analysisStatus.setMessage(localization.analyzeProjectRegenerationExecutionFailed());
                analysisStatus.setType(Notification.Type.ERROR);
                analysisStatus.setStatus(Notification.Status.FINISHED);
                Log.error(ProjectAnalyzer.class, e);
            }
        };

        clientService.synchronizeProject(project.getPath(), callback);
    }

    /** Get websocket listening channel. */
    private String getAnalysisChannel(@Nonnull ProjectDescriptor project) {
        return "gradle:analyzer:" + project.getWorkspaceId() + ":" + project.getPath();
    }

    /**
     * Get updated {@link com.codenvy.api.project.shared.dto.ProjectDescriptor} to allow Project Explorer to refresh current project tree
     * to
     * show actual state of current project.
     */
    private void getUpdatedProjectDescriptor(@Nonnull final GrdConfiguration grdConfiguration, @Nonnull ProjectDescriptor project) {
        Unmarshallable<ProjectDescriptor> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class);
        projectServiceClient.getProject(project.getPath(), new AsyncRequestCallback<ProjectDescriptor>(unmarshaller) {
            @Override
            protected void onSuccess(ProjectDescriptor project) {
                eventBus.fireEvent(new ProjectDescriptorChangedEvent(project));
                //if synchronization was successful then fire event that we have correct grdConfiguration
                eventBus.fireEvent(new ProjectConfigurationReceivedEvent(grdConfiguration, project));
            }

            @Override
            protected void onFailure(Throwable exception) {

            }
        });
    }
}
