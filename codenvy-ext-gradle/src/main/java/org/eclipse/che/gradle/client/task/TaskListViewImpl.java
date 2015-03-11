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

import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.gradle.client.GradleLocalizationConstant;
import org.eclipse.che.gradle.client.task.tree.GradleTaskTreeNodeDataAdapter;
import org.eclipse.che.gradle.client.task.tree.GradleTaskTreeNodeRenderer;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.js.JsoArray;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;

/**
 * The implementation of {@link TaskListView}.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class TaskListViewImpl extends BaseView<TaskListView.ActionDelegate> implements TaskListView {

    interface TaskListViewImplUiBinder extends UiBinder<Widget, TaskListViewImpl> {
    }

    private static TaskListViewImplUiBinder uiBinder = GWT.create(TaskListViewImplUiBinder.class);

    @UiField
    SimplePanel container;

    @UiField
    DockLayoutPanel noTasks;

    @UiField
    Label noTasksCause;

    @UiField(provided = true)
    TaskListResources resources;

    private Tree<TreeNode<?>>   tree;
    private AbstractTreeNode<?> rootNode;

    @Inject
    public TaskListViewImpl(PartStackUIResources partStackUIResources,
                            Tree.Resources coreRes,
                            GradleTaskTreeNodeRenderer gradleTaskTreeNodeRenderer,
                            GradleLocalizationConstant localization,
                            TaskListResources resources) {
        super(partStackUIResources);
        this.resources = resources;


        setTitle(localization.showTasksViewTitle());
        uiBinder.createAndBindUi(this);
        setContentWidget(container);

        // create special 'invisible' root node that will contain 'visible' root nodes
        rootNode = new AbstractTreeNode<Void>(null, null, null, null) {
            /** {@inheritDoc} */
            @Nonnull
            @Override
            public String getId() {
                return "ROOT";
            }

            /** {@inheritDoc} */
            @Nonnull
            @Override
            public String getDisplayName() {
                return "ROOT";
            }

            /** {@inheritDoc} */
            @Override
            public boolean isLeaf() {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {
            }
        };

        tree = Tree.create(coreRes, new GradleTaskTreeNodeDataAdapter(), gradleTaskTreeNodeRenderer);
        tree.getModel().setRoot(rootNode);
        tree.setTreeEventHandler(new Tree.Listener<TreeNode<?>>() {
            /** {@inheritDoc} */
            @Override
            public void onNodeAction(TreeNodeElement<TreeNode<?>> node) {
                delegate.onNodeAction(node.getData());
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeClosed(TreeNodeElement<TreeNode<?>> node) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeContextMenu(final int mouseX, final int mouseY, TreeNodeElement<TreeNode<?>> node) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeDragStart(TreeNodeElement<TreeNode<?>> node, MouseEvent event) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeDragDrop(TreeNodeElement<TreeNode<?>> node, MouseEvent event) {
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeExpanded(TreeNodeElement<TreeNode<?>> node) {
                delegate.onNodeExpanded(node.getData());
            }

            /** {@inheritDoc} */
            @Override
            public void onNodeSelected(TreeNodeElement<TreeNode<?>> node, SignalEvent event) {
            }

            /** {@inheritDoc} */
            @Override
            public void onRootContextMenu(final int mouseX, final int mouseY) {
            }

            /** {@inheritDoc} */
            @Override
            public void onRootDragDrop(MouseEvent event) {
            }

            /** {@inheritDoc} */
            @Override
            public void onKeyboard(KeyboardEvent event) {
                if (event.getKeyCode() == KeyboardEvent.KeyCode.ENTER) {
                    delegate.onEnterKey();
                }
            }
        });

        container.ensureDebugId("container");
        noTasks.ensureDebugId("noTasks");
        noTasksCause.ensureDebugId("noTasksCause");
    }

    /** {@inheritDoc} */
    @Override
    public void disableTaskList(@Nonnull String cause) {
        container.clear();
        noTasksCause.setText(cause);
        container.setWidget(noTasks);
    }

    /** {@inheritDoc} */
    @Override
    public void setRootNodes(@Nonnull Array<TreeNode<?>> rootNodes) {
        // provided rootNodes should be set as child nodes for rootNode
        com.google.gwt.dom.client.Element el = container.getElement().getFirstChildElement().cast();
        el.getStyle().setProperty("position", "relative");
        el.getStyle().setProperty("width", "100%");
        el.getStyle().setProperty("height", "100%");

        DockLayoutPanel treeContainer = new DockLayoutPanel(Style.Unit.PX);
        treeContainer.add(tree);
        treeContainer.setHeight("100%");
        treeContainer.setWidth("100%");
        container.setWidget(treeContainer);
        rootNode.setChildren(rootNodes);
        for (TreeNode<?> treeNode : rootNodes.asIterable()) {
            treeNode.setParent(rootNode);
        }

        tree.getSelectionModel().clearSelections();
        tree.getModel().setRoot(rootNode);
        tree.renderTree(1);

        if (!rootNodes.isEmpty()) {
            final TreeNode<?> firstNode = rootNodes.get(0);
            if (!firstNode.isLeaf()) {
                // expand first node that usually represents project itself
                tree.autoExpandAndSelectNode(firstNode, false);
                delegate.onNodeExpanded(firstNode);
            }
        }
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public TreeNode<?> getSelectedNode() {
        // Tree always must to have one selected node at least.
        // Return the first one until we don't support multi-selection.
        return tree.getSelectionModel().getSelectedNodes().get(0);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNode(@Nonnull TreeNode<?> oldNode, @Nonnull TreeNode<?> newNode) {
        // get currently selected node
        final JsoArray<TreeNode<?>> selectedNodes = tree.getSelectionModel().getSelectedNodes();
        TreeNode<?> selectedNode = null;
        if (!selectedNodes.isEmpty()) {
            selectedNode = selectedNodes.get(0);
        }

        Array<Array<String>> pathsToExpand = tree.replaceSubtree(oldNode, newNode, false);
        tree.expandPaths(pathsToExpand, false);

        // restore selected node
        if (selectedNode != null) {
            tree.getSelectionModel().selectSingleNode(selectedNode);
        }
    }

    public interface TaskListResources extends ClientBundle {
        public interface Css extends CssResource {
        }

        @Source("tree.svg")
        SVGResource tree();
    }
}
