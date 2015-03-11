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
package org.eclipse.che.gradle.client.task;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.gradle.client.GradleLocalizationConstant;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedEvent;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedHandler;
import org.eclipse.che.gradle.client.task.tree.GradleTaskTreeStructure;
import org.eclipse.che.gradle.client.task.tree.GradleTaskTreeStructureProvider;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.util.loging.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Show Gradle project tasks presenter.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class TaskListPresenter extends BasePresenter implements TaskListView.ActionDelegate,
                                                                ProjectConfigurationReceivedHandler {

    private WorkspaceAgent                  workspaceAgent;
    private TaskListView                    view;
    private GradleLocalizationConstant      localization;
    private GradleTaskTreeStructureProvider taskTreeStructureProvider;
    private GrdConfiguration                grdConfiguration;

    @Inject
    public TaskListPresenter(WorkspaceAgent workspaceAgent,
                             TaskListView view,
                             GradleLocalizationConstant localization,
                             GradleTaskTreeStructureProvider taskTreeStructureProvider,
                             EventBus eventBus) {
        this.view = view;
        this.workspaceAgent = workspaceAgent;
        this.localization = localization;
        this.taskTreeStructureProvider = taskTreeStructureProvider;

        eventBus.addHandler(ProjectConfigurationReceivedEvent.TYPE, this);

        this.view.setDelegate(this);
    }

    /** Shows current dialog window. */
    public void showPanel() {
        PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }
    }

    /** Get new instance of {@link GradleTaskTreeStructure} and construct task tree then show it on the view. */
    private void updateTreeOnView() {
        if (grdConfiguration != null) {
            GradleTaskTreeStructure gradleTaskTreeStructure = (GradleTaskTreeStructure)taskTreeStructureProvider.get();
            gradleTaskTreeStructure.getRootNodes(grdConfiguration, new AsyncCallback<Array<TreeNode<?>>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Log.error(TaskListPresenter.class, caught.getMessage());
                }

                @Override
                public void onSuccess(Array<TreeNode<?>> result) {
                    view.setRootNodes(result);
                }
            });
        } else {
            view.disableTaskList("Tasks aren't available at this moment");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationReceived(ProjectConfigurationReceivedEvent event) {
        this.grdConfiguration = event.getGrdConfiguration();
        updateTreeOnView();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeAction(@Nonnull TreeNode<?> node) {
        node.processNodeAction();
    }

    /** {@inheritDoc} */
    @Override
    public void onEnterKey() {
        view.getSelectedNode().processNodeAction();
    }

    /** Add view to right-side part stack. */
    public void addToPartStack() {
        updateTreeOnView();
        workspaceAgent.openPart(this, PartStackType.TOOLING);
    }

    /** Remove view from the right-side part stack. */
    public void removeFromPartStack() {
        workspaceAgent.removePart(this);
        grdConfiguration = null;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getTitle() {
        return localization.showTasksTitle();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getTitleToolTip() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return 280;
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeExpanded(@Nonnull final TreeNode<?> node) {
        if (node.getChildren().isEmpty()) {
            // If children is empty then node may be not refreshed yet?
            node.refreshChildren(new AsyncCallback<TreeNode<?>>() {
                @Override
                public void onSuccess(TreeNode<?> result) {
                    if (!result.getChildren().isEmpty()) {
                        view.updateNode(result, result);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    Log.error(TaskListPresenter.class, caught);
                }
            });
        }
    }
}
