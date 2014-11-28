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
package com.codenvy.ide.ext.java.client;

import com.codenvy.api.builder.BuildStatus;
import com.codenvy.api.builder.dto.BuildTaskDescriptor;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.collections.StringMap;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.extension.builder.client.build.BuildController;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.Pair;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.LinkedList;
import java.util.Queue;

import static com.codenvy.ide.api.notification.Notification.Status.FINISHED;
import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.ERROR;

/**
 * Updates dependencies for Maven project.
 *
 * @author Artem Zatsarynnyy
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class DependenciesUpdater {
    private NotificationManager      notificationManager;
    private BuildContext             buildContext;
    private AsyncRequestFactory      asyncRequestFactory;
    private JavaParserWorker         parserWorker;
    private EditorAgent              editorAgent;
    private BuildController          buildController;
    private DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private JavaLocalizationConstant javaLocalizationConstant;
    private String                   workspaceId;
    private Queue<Pair<ProjectDescriptor, Boolean>> projects = new LinkedList<>();
    private boolean                                 updating = false;

    @Inject
    public DependenciesUpdater(JavaLocalizationConstant javaLocalizationConstant,
                               NotificationManager notificationManager,
                               BuildContext buildContext,
                               AsyncRequestFactory asyncRequestFactory,
                               JavaParserWorker parserWorker,
                               EditorAgent editorAgent,
                               BuildController buildController,
                               @Named("workspaceId") String workspaceId,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.javaLocalizationConstant = javaLocalizationConstant;
        this.notificationManager = notificationManager;
        this.buildContext = buildContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.parserWorker = parserWorker;
        this.editorAgent = editorAgent;
        this.buildController = buildController;
        this.workspaceId = workspaceId;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    public void updateDependencies(final ProjectDescriptor project, final boolean force) {
        if (updating) {
            projects.add(new Pair<>(project, force));
            return;
        }

        final Notification notification = new Notification(javaLocalizationConstant.updatingDependencies(), PROGRESS);
        notificationManager.showNotification(notification);

        buildContext.setBuilding(true);
        updating = true;

        // send a first request to launch build process and return build task descriptor
        String urlLaunch =
                JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId + "/update-dependencies-launch-task?projectpath=" +
                project.getPath() + "&force=" + force;
        asyncRequestFactory.createGetRequest(urlLaunch, false).send(new AsyncRequestCallback<BuildTaskDescriptor>(
                dtoUnmarshallerFactory.newUnmarshaller(BuildTaskDescriptor.class)) {
            @Override
            protected void onSuccess(BuildTaskDescriptor descriptor) {
                if (descriptor.getStatus() == BuildStatus.SUCCESSFUL) {
                    notification.setMessage(javaLocalizationConstant.dependenciesSuccessfullyUpdated());
                    notification.setStatus(FINISHED);
                    buildContext.setBuilding(false);
                    updating = false;
                    return;
                }
                buildController.showRunningBuild(descriptor, "[INFO] Updating dependencies...");

                String urlWaitEnd = JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId +
                                    "/update-dependencies-wait-build-end?projectpath=" + project.getPath();
                // send a second request to be notified when dependencies update is finished
                asyncRequestFactory.createPostRequest(urlWaitEnd, descriptor, true)
                                   .send(new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                       @Override
                                       protected void onSuccess(String result) {
                                           updating = false;
                                           notification.setMessage(javaLocalizationConstant.dependenciesSuccessfullyUpdated());
                                           notification.setStatus(FINISHED);
                                           buildContext.setBuilding(false);
                                           parserWorker.dependenciesUpdated();
                                           editorAgent.getOpenedEditors()
                                                      .iterate(new StringMap.IterationCallback<EditorPartPresenter>() {
                                                          @Override
                                                          public void onIteration(String s, EditorPartPresenter editorPartPresenter) {
                                                              if (editorPartPresenter instanceof EmbeddedTextEditorPresenter) {
                                                                  final EmbeddedTextEditorPresenter<?> editor = (EmbeddedTextEditorPresenter<?>)editorPartPresenter;
                                                                  editor.refreshEditor();
                                                              }
                                                          }
                                                      });
                                           if (!projects.isEmpty()) {
                                               Pair<ProjectDescriptor, Boolean> pair = projects.poll();
                                               updateDependencies(pair.first, pair.second);
                                           }
                                       }

                                       @Override
                                       protected void onFailure(Throwable exception) {
                                           updating = false;
                                           if (!projects.isEmpty()) {
                                               Pair<ProjectDescriptor, Boolean> pair = projects.poll();
                                               updateDependencies(pair.first, pair.second);
                                           }
                                           updateFinishedWithError(exception, notification);
                                       }
                                   });
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.warn(DependenciesUpdater.class, "Failed to launch build process and get build task descriptor for " + project);
                updating = false;
                updateFinishedWithError(exception, notification);
            }
        });
    }

    private void updateFinishedWithError(Throwable exception, Notification notification) {
        buildContext.setBuilding(false);
        notification.setMessage(exception.getMessage());
        notification.setType(ERROR);
        notification.setStatus(FINISHED);
    }
}
