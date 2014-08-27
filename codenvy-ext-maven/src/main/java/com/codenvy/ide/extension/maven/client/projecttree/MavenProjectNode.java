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
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.projecttree.JavaFolderNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaProjectNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFolderNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Node that represents Maven project.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenProjectNode extends JavaProjectNode {
    private IconRegistry iconRegistry;

    public MavenProjectNode(ProjectDescriptor data, TreeSettings settings, EventBus eventBus, ProjectServiceClient projectServiceClient,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(data, settings, eventBus, projectServiceClient, dtoUnmarshallerFactory);
        this.iconRegistry = iconRegistry;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<AbstractTreeNode<?>> callback) {
        getModules(data.getPath(), new AsyncCallback<Array<ProjectDescriptor>>() {
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
                                // some folders may represents modules
                                ProjectDescriptor module = getModule(item);
                                if (module != null) {
                                    newChildren.add(new ModuleNode(MavenProjectNode.this, item, module, settings, eventBus,
                                                                   projectServiceClient, dtoUnmarshallerFactory, iconRegistry));
                                } else if (isFile(item)) {
                                    newChildren.add(new FileNode(MavenProjectNode.this, item, eventBus, projectServiceClient));
                                } else if (isFolder(item)) {
                                    if (isSourceFolder(item)) {
                                        newChildren.add(new SourceFolderNode(MavenProjectNode.this, item, settings, eventBus,
                                                                             projectServiceClient, dtoUnmarshallerFactory, iconRegistry));
                                    } else {
                                        newChildren.add(new JavaFolderNode(MavenProjectNode.this, item, settings, eventBus,
                                                                           projectServiceClient, dtoUnmarshallerFactory, iconRegistry));
                                    }
                                }
                            }
                        }
                        callback.onSuccess(MavenProjectNode.this);
                    }

                    /** Returns the module corresponding to the specified item
                     * or null if the directory does not correspond to any package. */
                    private ProjectDescriptor getModule(ItemReference item) {
                        if (isFolder(item)) {
                            for (ProjectDescriptor module : modules.asIterable()) {
                                if (item.getName().equals(module.getName())) {
                                    return module;
                                }
                            }
                        }
                        return null;
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

    private void getModules(String path, final AsyncCallback<Array<ProjectDescriptor>> callback) {
        final Unmarshallable<Array<ProjectDescriptor>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ProjectDescriptor.class);
        projectServiceClient.getModules(path, new AsyncRequestCallback<Array<ProjectDescriptor>>(unmarshaller) {
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

    private void getChildren(String path, final AsyncCallback<Array<ItemReference>> callback) {
        final Unmarshallable<Array<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ItemReference.class);
        projectServiceClient.getChildren(path, new AsyncRequestCallback<Array<ItemReference>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<ItemReference> result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /** Tests if the specified item is a source folder. */
    protected static boolean isSourceFolder(ItemReference item) {
        // TODO: read source folders from project/module attributes
        if ("folder".equals(item.getType())) {
            return item.getPath().endsWith("src/main/java") || item.getPath().endsWith("src/test/java");
        }
        return false;
    }
}
