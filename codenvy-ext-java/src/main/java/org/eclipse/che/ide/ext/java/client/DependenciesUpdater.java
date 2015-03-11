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
package org.eclipse.che.ide.ext.java.client;

import org.eclipse.che.api.builder.BuildStatus;
import org.eclipse.che.api.builder.dto.BuildOptions;
import org.eclipse.che.api.builder.dto.BuildTaskDescriptor;
import org.eclipse.che.api.project.shared.dto.BuildersDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.RefreshProjectTreeEvent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.collections.StringMap;
import org.eclipse.che.ide.ext.java.client.editor.JavaParserWorker;
import org.eclipse.che.ide.ext.java.client.projecttree.nodes.ExternalLibrariesNode;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaTreeStructure;
import org.eclipse.che.ide.extension.builder.client.BuilderExtension;
import org.eclipse.che.ide.extension.builder.client.build.BuildController;
import org.eclipse.che.ide.extension.builder.client.console.BuilderConsolePresenter;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.rest.*;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.Notification.Status.FINISHED;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;
import static org.eclipse.che.ide.api.notification.Notification.Type.INFO;
import static org.eclipse.che.ide.api.notification.Notification.Type.WARNING;

/**
 * Updates dependencies for Maven project.
 *
 * @author Artem Zatsarynnyy
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class DependenciesUpdater implements Notification.OpenNotificationHandler {
    private NotificationManager                notificationManager;
    private BuildContext                       buildContext;
    private BuildController                    buildController;
    private BuilderConsolePresenter            buildConsole;
    private AsyncRequestFactory                asyncRequestFactory;
    private JavaParserWorker                   parserWorker;
    private EditorAgent                        editorAgent;
    private DtoUnmarshallerFactory             dtoUnmarshallerFactory;
    private EventBus                           eventBus;
    private Set<DependencyBuildOptionProvider> optionProviders;
    private MessageBus                         messageBus;
    private AppContext                         appContext;
    private JavaLocalizationConstant           javaLocalizationConstant;
    private String                             workspaceId;

    private ProjectDescriptor                        rootProject;
    private ProjectDescriptor                        moduleProject;
    private BuildTaskDescriptor                      lastBuildTaskDescriptor;
    private UpdateStep                               currentUpdateStep;
    private Notification                             updateNotification;
    private SubscriptionHandler<BuildTaskDescriptor> buildStatusHandler;

    private Queue<QueueItem> queue    = new LinkedList<>();
    private boolean          updating = false;

    @Inject
    public DependenciesUpdater(JavaLocalizationConstant javaLocalizationConstant,
                               NotificationManager notificationManager,
                               BuildContext buildContext,
                               AsyncRequestFactory asyncRequestFactory,
                               JavaParserWorker parserWorker,
                               EditorAgent editorAgent,
                               BuildController buildController,
                               BuilderConsolePresenter buildConsole, @Named("workspaceId") String workspaceId,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               EventBus eventBus,
                               Set<DependencyBuildOptionProvider> optionProviders,
                               MessageBus messageBus,
                               AppContext appContext) {
        this.javaLocalizationConstant = javaLocalizationConstant;
        this.notificationManager = notificationManager;
        this.buildContext = buildContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.parserWorker = parserWorker;
        this.editorAgent = editorAgent;
        this.buildController = buildController;
        this.buildConsole = buildConsole;
        this.workspaceId = workspaceId;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.eventBus = eventBus;
        this.optionProviders = optionProviders;
        this.messageBus = messageBus;
        this.appContext = appContext;
    }

    @Override
    public void onOpenClicked() {
        buildConsole.setActive();
    }

    private enum UpdateStep {
        BINARY,
        SOURCE
    }

    private class QueueItem {
        ProjectDescriptor project;
        ProjectDescriptor module;
        boolean           force;

        private QueueItem(ProjectDescriptor project, ProjectDescriptor module, boolean force) {
            this.project = project;
            this.module = module;
            this.force = force;
        }
    }

    public void updateDependencies(ProjectDescriptor rootProject, ProjectDescriptor moduleProject, boolean force) {
        if (updating) {
            queue.add(new QueueItem(rootProject, moduleProject, force));
            return;
        }

        this.rootProject = rootProject;
        this.moduleProject = moduleProject;
        this.currentUpdateStep = UpdateStep.BINARY;

        updating = true;

        updateNotification = new Notification("Preparing environment", INFO, PROGRESS, this, null);
        notificationManager.showNotification(updateNotification);

        DependencyBuildOptionProvider optionProvider = getBuildOptionProvider(rootProject.getBuilders());
        BuildOptions binaryBuildOptions = optionProvider != null ? optionProvider.getOptionsForBinJars(moduleProject) : null;

        final String url = JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId + "/dependency/binaries";
        final String param = "?project=" + rootProject.getPath() + "&module=" + moduleProject.getPath() + "&force=" + force;

        Unmarshallable<BuildTaskDescriptor> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BuildTaskDescriptor.class);

        AsyncRequestCallback<BuildTaskDescriptor> callback = new AsyncRequestCallback<BuildTaskDescriptor>(unmarshaller) {
            @Override
            protected void onSuccess(BuildTaskDescriptor descriptor) {
                if (descriptor.getStatus() == BuildStatus.SUCCESSFUL) {
                    updateNotification.update("Dependency resolving has been finished successfully.", INFO, FINISHED, null, false);
                    buildContext.setBuilding(false);

                    processNextQueuedItem();
                    return;
                }

                buildContext.setBuilding(true);
                buildContext.setBuildTaskDescriptor(descriptor);

                updateNotification.setMessage("Binary dependency resolving has been started");
                buildController.showRunningBuild(descriptor, "[INFO] Updating binary dependencies...");

                startCheckingStatus(descriptor);
            }

            @Override
            protected void onFailure(Throwable exception) {
                updateNotification.update("Failed to resolve binary dependencies", WARNING, FINISHED, null, true);
                processNextQueuedItem();
            }
        };

        asyncRequestFactory.createPostRequest(url + param, binaryBuildOptions, false).send(callback);
    }

    private void updateSourceDependencies(ProjectDescriptor project, ProjectDescriptor module) {
        currentUpdateStep = UpdateStep.SOURCE;

        DependencyBuildOptionProvider optionProvider = getBuildOptionProvider(rootProject.getBuilders());
        BuildOptions binaryBuildOptions = optionProvider != null ? optionProvider.getOptionsForSrcJars(module) : null;

        final String url = JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId + "/dependency/sources";
        final String param = "?project=" + project.getPath() + "&module=" + module.getPath();

        Unmarshallable<BuildTaskDescriptor> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(BuildTaskDescriptor.class);

        AsyncRequestCallback<BuildTaskDescriptor> callback = new AsyncRequestCallback<BuildTaskDescriptor>(unmarshaller) {
            @Override
            protected void onSuccess(BuildTaskDescriptor descriptor) {
                buildContext.setBuilding(true);
                buildContext.setBuildTaskDescriptor(descriptor);

                updateNotification.setMessage("Sources dependency resolving has been started");
                buildController.showRunningBuild(descriptor, "[INFO] Updating sources dependencies...");

                startCheckingStatus(descriptor);
            }

            @Override
            protected void onFailure(Throwable exception) {
                updateNotification.update("Failed to resolve source dependencies", WARNING, FINISHED, null, true);
                processNextQueuedItem();
            }
        };

        asyncRequestFactory.createPostRequest(url + param, binaryBuildOptions, false).send(callback);
    }


    private void startCheckingStatus(final BuildTaskDescriptor buildTaskDescriptor) {
        buildStatusHandler =
                new SubscriptionHandler<BuildTaskDescriptor>(dtoUnmarshallerFactory.newWSUnmarshaller(BuildTaskDescriptor.class)) {
                    @Override
                    protected void onMessageReceived(BuildTaskDescriptor result) {
                        lastBuildTaskDescriptor = result;
                        buildContext.setBuildTaskDescriptor(result);
                        onBuildStatusUpdated(result);
                    }

                    @Override
                    protected void onErrorReceived(Throwable exception) {
                        try {
                            messageBus.unsubscribe(BuilderExtension.BUILD_STATUS_CHANNEL + buildTaskDescriptor.getTaskId(), this);
                            Log.error(BuildController.class, exception);
                        } catch (WebSocketException e) {
                            Log.error(BuildController.class, e);
                        }
                        buildContext.setBuilding(false);
                    }
                };

        try {
            messageBus.subscribe(BuilderExtension.BUILD_STATUS_CHANNEL + buildTaskDescriptor.getTaskId(), buildStatusHandler);
        } catch (WebSocketException e) {
            Log.error(BuildController.class, e);
        }
    }

    private void stopCheckingStatus() {
        try {
            messageBus.unsubscribe(BuilderExtension.BUILD_STATUS_CHANNEL + lastBuildTaskDescriptor.getTaskId(), buildStatusHandler);
        } catch (WebSocketException e) {
            Log.error(BuildController.class, e);
        }
    }

    private void onBuildStatusUpdated(BuildTaskDescriptor descriptor) {
        switch (descriptor.getStatus()) {
            case SUCCESSFUL:
                stopCheckingStatus();

                if (currentUpdateStep == UpdateStep.BINARY) {
                    updateSourceDependencies(rootProject, moduleProject);
                } else {
                    CurrentProject currentProject = appContext.getCurrentProject();
                    if (currentProject != null && currentProject.getCurrentTree() instanceof JavaTreeStructure) {
                        JavaTreeStructure currentTree = (JavaTreeStructure)currentProject.getCurrentTree();
                        ExternalLibrariesNode librariesNode = currentTree.getExternalLibrariesNode(moduleProject.getPath());
                        if (librariesNode != null && librariesNode.isOpened()) {
                            eventBus.fireEvent(new RefreshProjectTreeEvent(librariesNode));
                        }
                    }
                    parserWorker.dependenciesUpdated();
                    editorAgent.getOpenedEditors().iterate(new StringMap.IterationCallback<EditorPartPresenter>() {
                        @Override
                        public void onIteration(String s, EditorPartPresenter editorPartPresenter) {
                            if (editorPartPresenter instanceof EmbeddedTextEditorPresenter) {
                                ((EmbeddedTextEditorPresenter<?>)editorPartPresenter).refreshEditor();
                            }
                        }
                    });
                    updateNotification.update("Dependencies has been successfully resolved", INFO, FINISHED, null, true);

                    processNextQueuedItem();
                }

                break;
            case FAILED:
                stopCheckingStatus();
                processNextQueuedItem();
                updateNotification.update("Dependencies update has been failed", INFO, FINISHED, null, true);

                break;
            case CANCELLED:
                stopCheckingStatus();
                processNextQueuedItem();
                updateNotification.update("Dependencies update has been stopped", INFO, FINISHED, null, true);

                break;
        }
    }


    private DependencyBuildOptionProvider getBuildOptionProvider(BuildersDescriptor buildersDescriptor) {
        for (DependencyBuildOptionProvider provider : optionProviders) {
            if (provider.getBuilder().equals(buildersDescriptor.getDefault())) {
                return provider;
            }
        }

        return null;
    }

    private void processNextQueuedItem() {
        updating = false;
        buildContext.setBuilding(false);

        if (!queue.isEmpty()) {
            QueueItem item = queue.poll();
            updateDependencies(item.project, item.module, item.force);
        }
    }


//    public void _updateDependencies(final ProjectDescriptor project, final boolean force) {
//        if (updating) {
//            queue.add(new Pair<>(project, force));
//            return;
//        }
//        javaTreeStructure = null;
//        if (context.getCurrentProject().getCurrentTree() instanceof JavaTreeStructure) {
//            javaTreeStructure = (JavaTreeStructure)context.getCurrentProject().getCurrentTree();
//        }
//        final Notification notification = new Notification(javaLocalizationConstant.updatingDependencies(), PROGRESS);
//        notification.setImportant(true);
//        notificationManager.showNotification(notification);
//
//        buildContext.setBuilding(true);
//        updating = true;
//
//        // send a first request to launch build process and return build task descriptor
//        String urlLaunch =
//                JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId +
// "/update-dependencies-launch-task?projectpath=" +
//                project.getPath() + "&force=" + force;
//        asyncRequestFactory.createGetRequest(urlLaunch, false).send(new AsyncRequestCallback<BuildTaskDescriptor>(
//                dtoUnmarshallerFactory.newUnmarshaller(BuildTaskDescriptor.class)) {
//            @Override
//            protected void onSuccess(BuildTaskDescriptor descriptor) {
//                if (descriptor.getStatus() == BuildStatus.SUCCESSFUL) {
//                    notification.setMessage(javaLocalizationConstant.dependenciesSuccessfullyUpdated());
//                    notification.setStatus(FINISHED);
//                    buildContext.setBuilding(false);
//                    updating = false;
//                    return;
//                }
//                buildController.showRunningBuild(descriptor, "[INFO] Updating dependencies...");
//
//                String urlWaitEnd = JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId +
//                                    "/update-dependencies-wait-build-end?projectpath=" + project.getPath();
//                // send a second request to be notified when dependencies update is finished
//                asyncRequestFactory.createPostRequest(urlWaitEnd, descriptor, true)
//                                   .send(new AsyncRequestCallback<String>(new StringUnmarshaller()) {
//                                       @Override
//                                       protected void onSuccess(String result) {
//                                           updating = false;
//                                           notification.setMessage(javaLocalizationConstant.dependenciesSuccessfullyUpdated());
//                                           notification.setStatus(FINISHED);
//                                           buildContext.setBuilding(false);
//                                           parserWorker.dependenciesUpdated();
//                                           editorAgent.getOpenedEditors()
//                                                      .iterate(new StringMap.IterationCallback<EditorPartPresenter>() {
//                                                          @Override
//                                                          public void onIteration(String s, EditorPartPresenter editorPartPresenter) {
//                                                              if (editorPartPresenter instanceof EmbeddedTextEditorPresenter) {
//                                                                  final EmbeddedTextEditorPresenter<?> editor =
//                                                                          (EmbeddedTextEditorPresenter<?>)editorPartPresenter;
//                                                                  editor.refreshEditor();
//                                                              }
//                                                          }
//                                                      });
//                                           if (javaTreeStructure != null) {
//                                               ExternalLibrariesNode librariesNode =
//                                                       javaTreeStructure.getExternalLibrariesNode(project.getPath());
//                                               if (librariesNode != null && librariesNode.isOpened()) {
//                                                   eventBus.fireEvent(new RefreshProjectTreeEvent(librariesNode));
//                                               }
//                                           }
//                                           if (!queue.isEmpty()) {
//                                               Pair<ProjectDescriptor, Boolean> pair = queue.poll();
//                                               updateDependencies(pair.first, pair.second);
//                                           }
//                                       }
//
//                                       @Override
//                                       protected void onFailure(Throwable exception) {
//                                           updating = false;
//                                           if (!queue.isEmpty()) {
//                                               Pair<ProjectDescriptor, Boolean> pair = queue.poll();
//                                               updateDependencies(pair.first, pair.second);
//                                           }
//                                           updateFinishedWithError(exception, notification);
//                                       }
//                                   });
//            }
//
//            @Override
//            protected void onFailure(Throwable exception) {
//                Log.warn(DependenciesUpdater.class, "Failed to launch build process and get build task descriptor for " + project);
//                updating = false;
//                updateFinishedWithError(exception, notification);
//            }
//        });
//    }

//    private void updateFinishedWithError(Throwable exception, Notification notification) {
//        buildContext.setBuilding(false);
//        notification.setMessage(exception.getMessage());
//        notification.setType(ERROR);
//        notification.setStatus(FINISHED);
//    }
}
