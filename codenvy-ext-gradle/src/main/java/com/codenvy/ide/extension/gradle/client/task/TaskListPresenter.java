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
package com.codenvy.ide.extension.gradle.client.task;

import com.codenvy.api.builder.dto.BuildOptions;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.parts.PartPresenter;
import com.codenvy.ide.api.parts.PartStackType;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.builder.client.build.BuildController;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.codenvy.ide.extension.gradle.shared.dto.Task;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;

/** @author Vladyslav Zhukovskii */
@Singleton
public class TaskListPresenter extends BasePresenter implements TaskListView.ActionDelegate {

    private EventBus               eventBus;
    private WorkspaceAgent         workspaceAgent;
    private TaskListView           view;
    private DtoFactory             dtoFactory;
    private AsyncRequestFactory    asyncRequestFactory;
    private String                 workspaceId;
    private String                 baseUrl;
    private AppContext             appContext;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private BuildController        buildController;
    private BuildContext           buildContext;
    private NotificationManager    notificationManager;
    private LinkedList<Task>       recentTasks;

    @Inject
    public TaskListPresenter(EventBus eventBus,
                             WorkspaceAgent workspaceAgent,
                             TaskListView view,
                             DtoFactory dtoFactory,
                             AsyncRequestFactory asyncRequestFactory,
                             @Named("workspaceId") String workspaceId,
                             @Named("restContext") String baseUrl,
                             AppContext appContext,
                             DtoUnmarshallerFactory dtoUnmarshallerFactory,
                             BuildController buildController,
                             BuildContext buildContext,
                             NotificationManager notificationManager) {
        this.eventBus = eventBus;
        this.workspaceAgent = workspaceAgent;
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.workspaceId = workspaceId;
        this.baseUrl = baseUrl;
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.buildController = buildController;
        this.buildContext = buildContext;
        this.notificationManager = notificationManager;


        this.view.setDelegate(this);

        this.recentTasks = new LinkedList<>();

        this.eventBus.addHandler(ProjectActionEvent.TYPE, openProjectHandler);
        this.eventBus.addHandler(FileEvent.TYPE, saveBuildScriptHandler);
    }

    public void showPanel() {
        PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }
    }

    ProjectActionHandler openProjectHandler = new ProjectActionHandler() {
        @Override
        public void onProjectOpened(ProjectActionEvent event) {
            ProjectDescriptor project = event.getProject();
            if (project != null && GradleAttributes.GRADLE_ID.equals(project.getType())) {
                recentTasks.clear();
                addToPartStack();
                loadTasks();
            }
        }

        @Override
        public void onProjectClosed(ProjectActionEvent event) {
            recentTasks.clear();
            removeFromPartStack();
        }
    };

    FileEventHandler saveBuildScriptHandler = new FileEventHandler() {
        @Override
        public void onFileOperation(FileEvent event) {
            ProjectDescriptor project = event.getFile().getProject().getData();

            if (GradleAttributes.GRADLE_ID.equals(project.getType()) &&
                FileEvent.FileOperation.SAVE == event.getOperationType() &&
                "build.gradle".equals(event.getFile().getName())) {
                loadTasks();
            }
        }
    };

    private void loadTasks() {
        CurrentProject project = appContext.getCurrentProject();

        if (project == null) {
            return;
        }

        final String requestUrl = baseUrl + "/gradle/" + workspaceId + "/tasks";
        final String params = "projectpath=" + project.getProjectDescription().getPath();

        asyncRequestFactory.createGetRequest(requestUrl + "?" + params, false)
                           .send(new AsyncRequestCallback<Array<Task>>(dtoUnmarshallerFactory.newArrayUnmarshaller(Task.class)) {
                               @Override
                               protected void onSuccess(Array<Task> taskList) {
                                   view.showTaskList(taskList);
                                   log("DEBUG Class TaskListPresenter#onSuccess at line 137: " + taskList.toString());
                               }

                               @Override
                               protected void onFailure(Throwable exception) {

                               }
                           });

//        Array<Task> fooTasks = Collections.createArray();
//        fooTasks.add(dtoFactory.createDto(Task.class).withName("foo1"));
//        fooTasks.add(dtoFactory.createDto(Task.class).withName("foo2"));
//        fooTasks.add(dtoFactory.createDto(Task.class).withName("foo3"));
//        fooTasks.add(dtoFactory.createDto(Task.class).withName("foo4"));
//        fooTasks.add(dtoFactory.createDto(Task.class).withName("foo5"));
//
//        view.showRecentTaskList(fooTasks);
    }

    public native void log(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    private void addToPartStack() {
        workspaceAgent.openPart(this, PartStackType.TOOLING);
    }

    private void removeFromPartStack() {
        workspaceAgent.removePart(this);
    }

    @Nonnull
    @Override
    public String getTitle() {
        return "Gradle";
    }

    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    @Nullable
    @Override
    public String getTitleToolTip() {
        return null;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public int getSize() {
        return 280;
    }

    @Override
    public void executeTask(Task task) {
        log("DEBUG Class TaskListPresenter#executeTask at line 187: fired event");

        if (buildContext.isBuilding()) {
            notificationManager.showInfo("You cannot run any task during building.");
            return;
        }

        addTaskToRecentCallList(task);
        view.showRecentTaskList(recentTasks);

        buildController.buildActiveProject(getBuildOptions(task), true);
    }

    private BuildOptions getBuildOptions(Task task) {
        BuildOptions buildOptions = dtoFactory.createDto(BuildOptions.class);
        buildOptions.setTargets(Arrays.asList(task.getName()));

        return buildOptions;
    }

    private void addTaskToRecentCallList(Task task) {
        Task peek = recentTasks.peek();
        if (peek == null) {
            recentTasks.add(task);
            return;
        }

        if (peek.equals(task)) {
            return;
        }

        if (recentTasks.size() == 5) {
            recentTasks.removeLast();
        }

        recentTasks.addFirst(task);
    }
}
