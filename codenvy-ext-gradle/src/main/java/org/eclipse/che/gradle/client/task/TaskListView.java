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


import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.collections.Array;

import javax.annotation.Nonnull;

/**
 * The view of {@link com.codenvy.ide.extension.gradle.client.task.TaskListPresenter}.
 *
 * @author Vladyslav Zhukovskii
 */
public interface TaskListView extends View<TaskListView.ActionDelegate> {
    /** Delegate actions which called from View into Presenter. */
    public interface ActionDelegate extends BaseActionDelegate {
        /** Perform action when node double-clicked or enter key pressed. */
        void onNodeAction(@Nonnull TreeNode<?> node);

        /** Process action on node when enter key pressed. */
        void onEnterKey();

        /** Update child nodes when parent node expanded. */
        void onNodeExpanded(@Nonnull TreeNode<?> node);
    }

    /** Set new root nodes to show task tree structure. */
    void setRootNodes(@Nonnull Array<TreeNode<?>> rootNodes);

    /** Get selected node in task tree. */
    @Nonnull
    TreeNode<?> getSelectedNode();

    /** Update node content. */
    void updateNode(@Nonnull TreeNode<?> oldNode, @Nonnull TreeNode<?> newNode);

    /** Show no task available panel if tasks aren't available at this moment. */
    void disableTaskList(@Nonnull String cause);
}
