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
package com.codenvy.ide.extension.gradle.client.task.tree;

import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.codenvy.ide.gradle.dto.GrdProject;
import com.codenvy.ide.gradle.dto.GrdTask;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Node that represent Gradle project.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleProjectNode extends AbstractTreeNode<GrdConfiguration> {

    @AssistedInject
    public GradleProjectNode(@Assisted TreeNode<?> parent,
                             @Assisted GrdConfiguration data,
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
        return getData().getProject().getPath();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getDisplayName() {
        return getData().getProject().getName();
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
    private Array<TreeNode<?>> getChildNodesForItems(GrdConfiguration grdConfiguration) {
        Array<TreeNode<?>> childs = Collections.createArray();
        GrdProject root = grdConfiguration.getProject();

        for (GrdTask grdTask : root.getTasks()) {
            childs.add(getTreeStructure().newGradleTaskNode(this, grdTask));
        }

        for (GrdProject grdChild : root.getChild()) {
            childs.add(getTreeStructure().newGradleModuleNode(this, grdChild));
        }

        return childs;
    }
}
