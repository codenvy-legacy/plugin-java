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
import com.codenvy.ide.api.projecttree.TreeStructure;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.codenvy.ide.gradle.dto.GrdProject;
import com.codenvy.ide.gradle.dto.GrdTask;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nonnull;

/** @author Vladyslav Zhukovskii */
public class GradleTaskTreeStructure implements TreeStructure {

    private GradleTaskNodeFactory nodeFactory;
    private GradleProjectNode     gradleProjectNode;
    private GrdConfiguration      grdConfiguration;

    public GradleTaskTreeStructure(GradleTaskNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public void getRootNodes(@Nonnull GrdConfiguration grdConfiguration, @Nonnull AsyncCallback<Array<TreeNode<?>>> callback) {
        this.grdConfiguration = grdConfiguration;
        getRootNodes(callback);
    }

    @Override
    public void getRootNodes(@Nonnull AsyncCallback<Array<TreeNode<?>>> callback) {
        if (gradleProjectNode == null) {
            if (grdConfiguration != null) {
                gradleProjectNode = newGradleProjectNode(grdConfiguration);
            } else {
                callback.onFailure(new IllegalStateException("No model is specified."));
                return;
            }
        }

        callback.onSuccess(Collections.<TreeNode<?>>createArray(gradleProjectNode));
    }

    @Nonnull
    @Override
    public GradleTaskTreeSettings getSettings() {
        return GradleTaskTreeSettings.DEFAULT;
    }

    @Override
    public void getNodeByPath(@Nonnull String path, @Nonnull AsyncCallback<TreeNode<?>> callback) {

    }

    @Nonnull
    public GradleTaskNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    @Nonnull
    public GradleProjectNode newGradleProjectNode(GrdConfiguration data) {
        return getNodeFactory().newGradleProjectNode(null, data, this);
    }

    @Nonnull
    public GradleModuleNode newGradleModuleNode(AbstractTreeNode parent, GrdProject data) {
        return getNodeFactory().newGradleModuleNode(parent, data, this);
    }

    @Nonnull
    public GradleTaskNode newGradleTaskNode(AbstractTreeNode parent, GrdTask data) {
        return getNodeFactory().newGradleTaskNode(parent, data, this);
    }
}
