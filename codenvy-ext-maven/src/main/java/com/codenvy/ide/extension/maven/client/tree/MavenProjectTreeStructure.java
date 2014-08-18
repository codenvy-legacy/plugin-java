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
package com.codenvy.ide.extension.maven.client.tree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.api.projecttree.generic.ProjectRootNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.tree.AbstractJavaTreeStructure;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Tree structure for Maven project.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenProjectTreeStructure extends AbstractJavaTreeStructure {

    protected MavenProjectTreeStructure(EventBus eventBus, AppContext appContext, ProjectServiceClient projectServiceClient,
                                        DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(eventBus, appContext, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AbstractTreeNode<?> node, final AsyncCallback<AbstractTreeNode<?>> callback) {
        if (node instanceof ProjectRootNode) {
            final String path = ((ProjectRootNode)node).getData().getPath();
//            getModules(node, path, callback);
            getRootChildren(node, path, callback);
        } else if (node instanceof ModuleNode) {
            final String path = ((ModuleNode)node).getData().getPath();
            refresh(node, path, callback);
        } else {
            super.refreshChildren(node, callback);
        }
    }

    /** Refresh root's children. */
    private void getRootChildren(final AbstractTreeNode<?> parentNode, final String path,
                                 final AsyncCallback<AbstractTreeNode<?>> callback) {
        getModules(path, new AsyncCallback<Array<ProjectDescriptor>>() {
            @Override
            public void onSuccess(final Array<ProjectDescriptor> modules) {
                final Array<AbstractTreeNode<?>> array = Collections.createArray();
                parentNode.setChildren(array);

                // get root's children and add its to array
                Unmarshallable<Array<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ItemReference.class);
                projectServiceClient.getChildren(path, new AsyncRequestCallback<Array<ItemReference>>(unmarshaller) {
                    @Override
                    protected void onSuccess(Array<ItemReference> result) {
                        for (ItemReference item : result.asIterable()) {
                            // some folders may represents modules
                            ProjectDescriptor module = getModule(item);
                            if (module != null) {
                                array.add(new ModuleNode(parentNode, item, module));
                            } else if ("file".equals(item.getType())) {
                                array.add(new FileNode(parentNode, item));
                            } else if ("folder".equals(item.getType())) {
                                array.add(new FolderNode(parentNode, item));
                            }
                            callback.onSuccess(parentNode);
                        }
                    }

                    private ProjectDescriptor getModule(ItemReference item) {
                        if ("folder".equals(item.getType())) {
                            for (ProjectDescriptor module : modules.asIterable()) {
                                if (item.getName().equals(module.getName())) {
                                    return module;
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {

            }
        });
    }

    protected void getModules(String path, final AsyncCallback<Array<ProjectDescriptor>> callback) {
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

    /** Node that represents module of multi-module project. */
    protected static class ModuleNode extends FolderNode {
        private ProjectDescriptor module;

        ModuleNode(AbstractTreeNode parent, ItemReference data, ProjectDescriptor module) {
            super(parent, data);
            this.module = module;
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return '[' + data.getName() + ']';
        }
    }
}
