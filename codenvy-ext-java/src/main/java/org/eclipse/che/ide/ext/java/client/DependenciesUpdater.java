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
import org.eclipse.che.api.builder.dto.BuildTaskDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
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
import org.eclipse.che.ide.extension.builder.client.build.BuildController;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import java.util.LinkedList;
import java.util.Queue;

import static org.eclipse.che.ide.api.notification.Notification.Status.FINISHED;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

/**
 * Updates dependencies for Maven project.
 *
 * @author Artem Zatsarynnyy
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class DependenciesUpdater {
    private NotificationManager    notificationManager;
    private BuildContext           buildContext;
    private AsyncRequestFactory    asyncRequestFactory;
    private JavaParserWorker       parserWorker;
    private EditorAgent            editorAgent;
    private BuildController        buildController;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private EventBus               eventBus;
    private AppContext context;
    private JavaLocalizationConstant javaLocalizationConstant;
    private String                   workspaceId;
    private Queue<Pair<ProjectDescriptor, Boolean>> projects = new LinkedList<>();
    private boolean                                 updating = false;
    private JavaTreeStructure javaTreeStructure;

    @Inject
    public DependenciesUpdater(JavaLocalizationConstant javaLocalizationConstant,
                               NotificationManager notificationManager,
                               BuildContext buildContext,
                               AsyncRequestFactory asyncRequestFactory,
                               JavaParserWorker parserWorker,
                               EditorAgent editorAgent,
                               BuildController buildController,
                               @Named("workspaceId") String workspaceId,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               EventBus eventBus,
                               AppContext context) {
        this.javaLocalizationConstant = javaLocalizationConstant;
        this.notificationManager = notificationManager;
        this.buildContext = buildContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.parserWorker = parserWorker;
        this.editorAgent = editorAgent;
        this.buildController = buildController;
        this.workspaceId = workspaceId;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.eventBus = eventBus;
        this.context = context;
    }

    public void updateDependencies(final ProjectDescriptor project, final boolean force) {
        if (updating) {
            projects.add(new Pair<>(project, force));
            return;
        }
        javaTreeStructure = null;
        if (context.getCurrentProject().getCurrentTree() instanceof JavaTreeStructure) {
            javaTreeStructure = (JavaTreeStructure)context.getCurrentProject().getCurrentTree();
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
                                            if(javaTreeStructure != null){
                                                ExternalLibrariesNode librariesNode =
                                                        javaTreeStructure.getExternalLibrariesNode(project.getPath());
                                                if(librariesNode != null && librariesNode.isOpened()){
                                                    eventBus.fireEvent(new RefreshProjectTreeEvent(librariesNode));
                                                }
                                            }
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
