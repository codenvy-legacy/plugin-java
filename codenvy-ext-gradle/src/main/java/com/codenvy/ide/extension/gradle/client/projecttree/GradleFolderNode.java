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
package com.codenvy.ide.extension.gradle.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JavaFolderNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Vladyslav Zhukovskii */
public class GradleFolderNode extends JavaFolderNode {

    @AssistedInject
    public GradleFolderNode(@Assisted TreeNode<?> parent,
                            @Assisted ItemReference data,
                            @Assisted GradleProjectTreeStructure treeStructure,
                            EventBus eventBus,
                            ProjectServiceClient projectServiceClient,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
        log("DEBUG Class GradleFolderNode#GradleFolderNode at line 41: ");
    }

    @Nonnull
    @Override
    public GradleProjectTreeStructure getTreeStructure() {
        return (GradleProjectTreeStructure)super.getTreeStructure();
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        log("DEBUG Class GradleFolderNode#refreshChildren at line 52: ");
        getModules(getData(), new AsyncCallback<Array<ProjectDescriptor>>() {
            @Override
            public void onSuccess(final Array<ProjectDescriptor> modules) {
                getChildren(getData().getPath(), new AsyncCallback<Array<ItemReference>>() {
                    @Override
                    public void onSuccess(Array<ItemReference> childItems) {
                        setChildren(getChildNodesForItems(childItems, modules));
                        callback.onSuccess(GradleFolderNode.this);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    protected void getModules(ItemReference folder, final AsyncCallback<Array<ProjectDescriptor>> callback) {
        log("DEBUG Class GradleFolderNode#getModules at line 78: " + folder.toString());
        final Unmarshallable<Array<ProjectDescriptor>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ProjectDescriptor.class);
        projectServiceClient.getModules(folder.getPath(), new AsyncRequestCallback<Array<ProjectDescriptor>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<ProjectDescriptor> result) {
                log("DEBUG Class GradleFolderNode#onSuccess at line 83: " + result.toString());
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private Array<TreeNode<?>> getChildNodesForItems(Array<ItemReference> childItems, Array<ProjectDescriptor> modules) {
        final boolean isShowHiddenItems = getTreeStructure().getSettings().isShowHiddenItems();
        Array<TreeNode<?>> oldChildren = Collections.createArray(getChildren().asIterable());
        Array<TreeNode<?>> newChildren = Collections.createArray();
        for (ItemReference item : childItems.asIterable()) {
            if (!isShowHiddenItems && item.getName().startsWith(".")) {
                continue;
            }
            AbstractTreeNode node = createChildNode(item, modules);
            log("DEBUG Class GradleFolderNode#getChildNodesForItems at line 103: " + node);
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

    @Nullable
    protected AbstractTreeNode<?> createChildNode(ItemReference item, Array<ProjectDescriptor> modules) {
        log("DEBUG Class GradleFolderNode#createChildNode at line 113: " + item.toString() + ":-------:" + modules.toString());
        if ("project".equals(item.getType())) {
            ProjectDescriptor module = getModule(item, modules);
            if (module != null) {
                return getTreeStructure().newModuleNode(this, module);
            }
            return null;
        } else if (JavaSourceFolderUtil.isSourceFolder(item, getProject())) {
            return getTreeStructure().newSourceFolderNode(this, item);
        } else if ("folder".equals(item.getType())) {
            return getTreeStructure().newJavaFolderNode(this, item);
        } else {
            return super.createChildNode(item);
        }
    }


    public native void log(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    @Nullable
    private ProjectDescriptor getModule(ItemReference folderItem, Array<ProjectDescriptor> modules) {
        if ("project".equals(folderItem.getType())) {
            for (ProjectDescriptor module : modules.asIterable()) {
                log("DEBUG Class GradleFolderNode#getModule at line 138: " + module.toString() + ":" + folderItem.toString());
                if (folderItem.getName().equals(module.getName())) {
                    return module;
                }
            }
        }
        return null;
    }
}
