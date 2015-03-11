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
package org.eclipse.che.gradle.client.projecttree;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.Collections;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.client.projecttree.nodes.JavaProjectNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;

/**
 * Node that represents Gradle project.
 *
 * @author Vladyslav Zhukovskii
 * @see JavaProjectNode
 */
public class GradleProjectNode extends JavaProjectNode {

    @Inject
    public GradleProjectNode(@Assisted TreeNode<?> parent,
                             @Assisted ProjectDescriptor data,
                             @Assisted GradleProjectTreeStructure treeStructure,
                             EventBus eventBus,
                             ProjectServiceClient projectServiceClient,
                             DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public GradleProjectTreeStructure getTreeStructure() {
        return (GradleProjectTreeStructure)super.getTreeStructure();
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        getModules(getData(), new AsyncCallback<Array<ProjectDescriptor>>() {
            @Override
            public void onSuccess(final Array<ProjectDescriptor> modules) {
                getChildren(getData().getPath(), new AsyncCallback<Array<ItemReference>>() {
                    @Override
                    public void onSuccess(Array<ItemReference> childItems) {
                        setChildren(getChildNodesForItems(childItems, modules));
                        callback.onSuccess(GradleProjectNode.this);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.error(GradleProjectNode.class, e);
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.error(GradleProjectNode.class, e);
                callback.onFailure(e);
            }
        });
    }

    /** Get project modules. */
    protected void getModules(ProjectDescriptor project, final AsyncCallback<Array<ProjectDescriptor>> callback) {
        final Unmarshallable<Array<ProjectDescriptor>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ProjectDescriptor.class);
        projectServiceClient.getModules(project.getPath(), new AsyncRequestCallback<Array<ProjectDescriptor>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<ProjectDescriptor> result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable e) {
                Log.error(GradleProjectNode.class, e);
                callback.onFailure(e);
            }
        });
    }

    /** Get child node from current Project node. */
    private Array<TreeNode<?>> getChildNodesForItems(Array<ItemReference> childItems, Array<ProjectDescriptor> modules) {
        Array<TreeNode<?>> oldChildren = Collections.createArray(getChildren().asIterable());
        Array<TreeNode<?>> newChildren = Collections.createArray();
        for (ItemReference item : childItems.asIterable()) {
            AbstractTreeNode node = createChildNode(item, modules);
            if (node != null) {
                if (oldChildren.contains(node)) {
                    final int i = oldChildren.indexOf(node);
                    newChildren.add(oldChildren.get(i));
                } else {
                    newChildren.add(node);
                }
            }
        }
        return newChildren;
    }

    /** Create child node according to node type. */
    @Nullable
    protected AbstractTreeNode<?> createChildNode(ItemReference item, Array<ProjectDescriptor> modules) {
        if ("project".equals(item.getType())) {
            ProjectDescriptor module = getModule(item, modules);
            if (module != null) {
                return getTreeStructure().newModuleNode(this, module);
            }
            // if project isn't a module - show it as folder
            return getTreeStructure().newJavaFolderNode(GradleProjectNode.this, item);
        } else if (JavaSourceFolderUtil.isSourceFolder(item, getProject())) {
            return getTreeStructure().newSourceFolderNode(GradleProjectNode.this, item);
        } else if ("folder".equals(item.getType())) {
            return getTreeStructure().newJavaFolderNode(GradleProjectNode.this, item);
        } else {
            return super.createChildNode(item);
        }
    }

    /** Get project descriptor from child module. */
    @Nullable
    private ProjectDescriptor getModule(ItemReference folderItem, Array<ProjectDescriptor> modules) {
        if ("project".equals(folderItem.getType())) {
            for (ProjectDescriptor module : modules.asIterable()) {
                if (folderItem.getName().equals(module.getName())) {
                    return module;
                }
            }
        }
        return null;
    }
}
