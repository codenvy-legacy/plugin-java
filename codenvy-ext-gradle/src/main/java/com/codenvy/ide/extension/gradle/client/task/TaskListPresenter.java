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
package com.codenvy.ide.extension.gradle.client.task;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.parts.PartPresenter;
import com.codenvy.ide.api.parts.PartStackType;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.api.parts.base.BasePresenter;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Vladyslav Zhukovskii */
@Singleton
public class TaskListPresenter extends BasePresenter implements TaskListView.ActionDelegate {

    private EventBus       eventBus;
    private WorkspaceAgent workspaceAgent;
    private TaskListView view;

    @Inject
    public TaskListPresenter(EventBus eventBus,
                             WorkspaceAgent workspaceAgent,
                             TaskListView view) {
        this.eventBus = eventBus;
        this.workspaceAgent = workspaceAgent;
        this.view = view;


        this.view.setDelegate(this);

        this.eventBus.addHandler(ProjectActionEvent.TYPE, openProjectHandler);

        this.view.showPanel();
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
                addToPartStack();
            }
        }

        @Override
        public void onProjectClosed(ProjectActionEvent event) {
            removeFromPartStack();
        }
    };

    private void addToPartStack() {
        workspaceAgent.openPart(this, PartStackType.TOOLING);
    }

    private void removeFromPartStack() {
        workspaceAgent.removePart(this);
    }

    @Nonnull
    @Override
    public String getTitle() {
        return "Gradle tasks";
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
}
