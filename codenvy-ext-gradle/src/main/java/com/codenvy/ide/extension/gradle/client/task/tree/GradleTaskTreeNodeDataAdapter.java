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

import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ui.tree.NodeDataAdapter;
import com.codenvy.ide.ui.tree.TreeNodeElement;

/**
 * Adapter that allows the Gradle Tree to traverse (get the children of) some NodeData.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleTaskTreeNodeDataAdapter implements NodeDataAdapter<TreeNode<?>> {
    /** {@inheritDoc} */
    @Override
    public int compare(TreeNode<?> a, TreeNode<?> b) {
        return a.getId().compareTo(b.getId());
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(TreeNode<?> data) {
        return !data.isLeaf();
    }

    /** {@inheritDoc} */
    @Override
    public Array<TreeNode<?>> getChildren(TreeNode<?> data) {
        return data.getChildren();
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeId(TreeNode<?> data) {
        return data.getId();
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeName(TreeNode<?> data) {
        return data.getDisplayName();
    }

    /** {@inheritDoc} */
    @Override
    public TreeNode<?> getParent(TreeNode<?> data) {
        return data.getParent();
    }

    /** {@inheritDoc} */
    @Override
    public TreeNodeElement<TreeNode<?>> getRenderedTreeNode(TreeNode<?> data) {
        return data.getTreeNodeElement();
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeName(TreeNode<?> data, String name) {
    }

    /** {@inheritDoc} */
    @Override
    public void setRenderedTreeNode(TreeNode<?> data, TreeNodeElement<TreeNode<?>> renderedNode) {
        data.setTreeNodeElement(renderedNode);
    }

    /** {@inheritDoc} */
    @Override
    public TreeNode<?> getDragDropTarget(TreeNode<?> data) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Array<String> getNodePath(TreeNode<?> data) {
        return PathUtils.getNodePath(this, data);
    }

    /** {@inheritDoc} */
    @Override
    public TreeNode<?> getNodeByPath(TreeNode<?> root, Array<String> relativeNodePath) {
        return null;
    }
}
