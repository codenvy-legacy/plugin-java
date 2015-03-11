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
package org.eclipse.che.gradle.client.task.tree;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.gradle.dto.GrdProject;
import org.eclipse.che.gradle.dto.GrdTask;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.Collections;

import javax.annotation.Nonnull;

/**
 * Node that represent Gradle module.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleModuleNode extends AbstractTreeNode<GrdProject> {

    @AssistedInject
    public GradleModuleNode(@Assisted TreeNode<?> parent,
                            @Assisted GrdProject data,
                            @Assisted GradleTaskTreeStructure treeStructure,
                            EventBus eventBus) {
        super(parent, data, treeStructure, eventBus);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public GradleTaskTreeStructure getTreeStructure() {
        return (GradleTaskTreeStructure)super.getTreeStructure();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getId() {
        return getData().getName();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getDisplayName() {
        return getData().getPath();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {
        setChildren(getChildNodesForItems(getData()));
        callback.onSuccess(this);
    }

    /** Get child nodes according to their type. */
    private Array<TreeNode<?>> getChildNodesForItems(GrdProject grdProject) {
        Array<TreeNode<?>> childs = Collections.createArray();
        for (GrdTask task : grdProject.getTasks()) {
            childs.add(getTreeStructure().newGradleTaskNode(this, task));
        }

        for (GrdProject grdChildProject : grdProject.getChild()) {
            childs.add(getTreeStructure().newGradleModuleNode(this, grdChildProject));
        }
        return childs;
    }
}
