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
package com.codenvy.ide.extension.maven.client;

import com.codenvy.api.builder.BuildStatus;
import com.codenvy.api.builder.dto.BuildTaskDescriptor;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.editor.CodenvyTextEditor;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.text.Document;
import com.codenvy.ide.api.texteditor.reconciler.Reconciler;
import com.codenvy.ide.api.texteditor.reconciler.ReconcilingStrategy;
import com.codenvy.ide.collections.StringMap;
import com.codenvy.ide.ext.java.client.JavaExtension;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.ext.java.client.editor.JavaReconcilerStrategy;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.builder.client.build.BuildController;
import com.codenvy.ide.extension.maven.client.event.BeforeModuleOpenEvent;
import com.codenvy.ide.extension.maven.client.event.BeforeModuleOpenHandler;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.notification.Notification.Status.FINISHED;
import static com.codenvy.ide.api.notification.Notification.Status.PROGRESS;
import static com.codenvy.ide.api.notification.Notification.Type.ERROR;

/**
 * Updates dependencies for Maven project.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class DependenciesUpdater {
    private NotificationManager       notificationManager;
    private BuildContext              buildContext;
    private AsyncRequestFactory       asyncRequestFactory;
    private JavaParserWorker          parserWorker;
    private EditorAgent               editorAgent;
    private BuildController           buildController;
    private DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    private MavenLocalizationConstant mavenLocalizationConstant;
    private String                    workspaceId;
    private boolean updating      = false;
    private boolean needForUpdate = false;

    @Inject
    public DependenciesUpdater(MavenLocalizationConstant mavenLocalizationConstants,
                               NotificationManager notificationManager,
                               BuildContext buildContext,
                               AsyncRequestFactory asyncRequestFactory,
                               JavaParserWorker parserWorker,
                               final EditorAgent editorAgent,
                               BuildController buildController,
                               @Named("workspaceId") String workspaceId,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               EventBus eventBus) {
        this.mavenLocalizationConstant = mavenLocalizationConstants;
        this.notificationManager = notificationManager;
        this.buildContext = buildContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.parserWorker = parserWorker;
        this.editorAgent = editorAgent;
        this.buildController = buildController;
        this.workspaceId = workspaceId;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                ProjectDescriptor project = event.getProject();
                Map<String, List<String>> attributes = project.getAttributes();
                if (!attributes.isEmpty() && attributes.containsKey(Constants.LANGUAGE) &&
                    "java".equals(attributes.get(Constants.LANGUAGE).get(0))) {
                    updateDependencies(project, false);
                }
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
            }
        });

        eventBus.addHandler(FileEvent.TYPE, new FileEventHandler() {
            @Override
            public void onFileOperation(FileEvent event) {
                String name = event.getFile().getName();
                if (event.getOperationType() == FileEvent.FileOperation.SAVE && "pom.xml".equals(name)) {
                    updateDependencies(event.getFile().getProject().getData(), true);
                }
            }
        });

        eventBus.addHandler(BeforeModuleOpenEvent.TYPE, new BeforeModuleOpenHandler() {
            @Override
            public void onBeforeModuleOpen(BeforeModuleOpenEvent event) {
                updateDependencies(event.getModule().getData(), false);
            }
        });
    }

    public void updateDependencies(final ProjectDescriptor project, final boolean force) {
        if (updating) {
            needForUpdate = true;
            return;
        }

        Map<String, List<String>> attributes = project.getAttributes();
        if (attributes.containsKey(MavenAttributes.MAVEN_PACKAGING)) {
            if ("pom".equals(attributes.get(MavenAttributes.MAVEN_PACKAGING).get(0))) {
                needForUpdate = false;
                return;
            }
        }

        final Notification notification = new Notification(mavenLocalizationConstant.updatingDependencies(), PROGRESS);
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
                    notification.setMessage(mavenLocalizationConstant.dependenciesSuccessfullyUpdated());
                    notification.setStatus(FINISHED);
                    needForUpdate = false;
                    return;
                }
                buildController.showRunningBuild(descriptor, "[INFO] Update Dependencies started...");

                String urlWaitEnd = JavaExtension.getJavaCAPath() + "/java-name-environment/" + workspaceId +
                                    "/update-dependencies-wait-build-end?projectpath=" + project.getPath();
                // send a second request to be notified when dependencies update is finished
                asyncRequestFactory.createPostRequest(urlWaitEnd, descriptor, true)
                                   .send(new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                       @Override
                                       protected void onSuccess(String result) {
                                           updating = false;
                                           notification.setMessage(mavenLocalizationConstant.dependenciesSuccessfullyUpdated());
                                           notification.setStatus(FINISHED);
                                           buildContext.setBuilding(false);
                                           parserWorker.dependenciesUpdated();
                                           editorAgent.getOpenedEditors().iterate(new StringMap.IterationCallback<EditorPartPresenter>() {
                                               @Override
                                               public void onIteration(String s, EditorPartPresenter editorPartPresenter) {
                                                   if (editorPartPresenter instanceof CodenvyTextEditor) {
                                                       CodenvyTextEditor editor = (CodenvyTextEditor)editorPartPresenter;
                                                       Reconciler reconciler = editor.getConfiguration().getReconciler(editor.getView());
                                                       if (reconciler != null) {
                                                           ReconcilingStrategy strategy =
                                                                   reconciler.getReconcilingStrategy(Document.DEFAULT_CONTENT_TYPE);
                                                           if (strategy != null && strategy instanceof JavaReconcilerStrategy) {
                                                               ((JavaReconcilerStrategy)strategy).parse();
                                                           }
                                                       }
                                                   }
                                                   if (needForUpdate) {
                                                       needForUpdate = false;
                                                       updateDependencies(project, force);
                                                   }
                                               }
                                           });
                                       }

                                       @Override
                                       protected void onFailure(Throwable exception) {
                                           updating = false;
                                           needForUpdate = false;
                                           notification.setMessage(exception.getMessage());
                                           notification.setType(ERROR);
                                           notification.setStatus(FINISHED);
                                           buildContext.setBuilding(false);
                                       }
                                   });
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.warn(MavenExtension.class, "failed to launch build process and get build task descriptor for " + project);
            }
        });
    }
}
