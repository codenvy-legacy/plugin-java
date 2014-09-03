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
package com.codenvy.ide.extension.maven.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.projecttree.JavaProjectNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nullable;

/**
 * Node that represents Maven project.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenProjectNode extends JavaProjectNode {

    public MavenProjectNode(AbstractTreeNode<?> parent, ProjectDescriptor data, MavenProjectTreeStructure treeStructure,
                            TreeSettings settings, EventBus eventBus, ProjectServiceClient projectServiceClient,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, settings, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** Tests if the specified item is a source folder. */
    protected static boolean isSourceFolder(ItemReference item) {
        // TODO: read source folders from project/module attributes
        return isFolder(item) && item.getPath().endsWith("src/main/java") || item.getPath().endsWith("src/test/java");
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<AbstractTreeNode<?>> callback) {
        getModules(data, new AsyncCallback<Array<ProjectDescriptor>>() {
            @Override
            public void onSuccess(final Array<ProjectDescriptor> modules) {
                getChildren(data.getPath(), new AsyncCallback<Array<ItemReference>>() {
                    @Override
                    public void onSuccess(Array<ItemReference> children) {
                        final boolean isShowHiddenItems = settings.isShowHiddenItems();
                        Array<AbstractTreeNode<?>> newChildren = Collections.createArray();
                        setChildren(newChildren);
                        for (ItemReference item : children.asIterable()) {
                            if (isShowHiddenItems || !item.getName().startsWith(".")) {
                                AbstractTreeNode node = createNode(item, modules);
                                if (node != null) {
                                    newChildren.add(node);
                                }
                            }
                        }
                        callback.onSuccess(MavenProjectNode.this);
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

    /**
     * Method helps to retrieve modules of the specified project using Codenvy Project API.
     *
     * @param project
     *         project to retrieve its modules
     * @param callback
     *         callback to return retrieved modules
     */
    protected void getModules(ProjectDescriptor project, final AsyncCallback<Array<ProjectDescriptor>> callback) {
        final Unmarshallable<Array<ProjectDescriptor>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ProjectDescriptor.class);
        projectServiceClient.getModules(project.getPath(), new AsyncRequestCallback<Array<ProjectDescriptor>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<ProjectDescriptor> result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /**
     * Creates node for the specified item.
     *
     * @param item
     *         {@link com.codenvy.api.project.shared.dto.ItemReference} for which need to create node
     * @param modules
     *         modules list to identify specified item as project's module
     * @return new node instance or <code>null</code> if the specified item is not supported
     */
    @Nullable
    protected AbstractTreeNode<?> createNode(ItemReference item, Array<ProjectDescriptor> modules) {
        ProjectDescriptor module = getModule(item, modules);
        if (module != null) {
            return ((MavenProjectTreeStructure)treeStructure).newModuleNode(this, module);
        } else if (isSourceFolder(item)) {
            return ((MavenProjectTreeStructure)treeStructure).newSourceFolderNode(MavenProjectNode.this, item);
        } else if (isFolder(item)) {
            return ((MavenProjectTreeStructure)treeStructure).newJavaFolderNode(MavenProjectNode.this, item);
        } else {
            return super.createNode(item);
        }
    }

    /**
     * Returns the module descriptor that corresponds to the specified folderItem
     * or null if the folderItem does not correspond to any module from the specified list.
     */
    @Nullable
    private ProjectDescriptor getModule(ItemReference folderItem, Array<ProjectDescriptor> modules) {
        if (isFolder(folderItem)) {
            for (ProjectDescriptor module : modules.asIterable()) {
                if (folderItem.getName().equals(module.getName())) {
                    return module;
                }
            }
        }
        return null;
    }
}
