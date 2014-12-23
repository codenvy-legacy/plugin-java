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
package com.codenvy.ide.ext.java.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.api.projecttree.generic.GenericTreeStructure;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.util.Pair;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Tree structure for Java project.
 *
 * @author Artem Zatsarynnyy
 */
public class JavaTreeStructure extends GenericTreeStructure {

    protected final IconRegistry          iconRegistry;
    protected final JavaNavigationService service;

    protected JavaTreeStructure(TreeSettings settings, ProjectDescriptor project, EventBus eventBus, EditorAgent editorAgent,
                                AppContext appContext, ProjectServiceClient projectServiceClient, IconRegistry iconRegistry,
                                DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                JavaNavigationService service) {
        super(settings, project, eventBus, editorAgent, appContext, projectServiceClient, dtoUnmarshallerFactory);
        this.iconRegistry = iconRegistry;
        this.service = service;
    }

    /**
     * Find class in external libs.
     *
     * @param libId
     *         the lib id
     * @param path
     *         is FQN of the class like 'java.lang.String';
     * @param callback
     */
    public void getClassFileByPath(final int libId, final String path, final AsyncCallback<TreeNode<?>> callback) {
        getRoots(new AsyncCallback<Array<TreeNode<?>>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(Array<TreeNode<?>> result) {
                JavaProjectNode project = null;
                for (TreeNode<?> node : result.asIterable()) {
                    if (node instanceof JavaProjectNode) {
                        project = (JavaProjectNode)node;
                        break;
                    }
                }
                if (project != null) {
                    findLibInProject(project, libId, path, callback);
                }
            }
        });
    }

    private void findLibInProject(JavaProjectNode project, final int libId, final String path, final AsyncCallback<TreeNode<?>> callback) {
        project.getLibrariesNode().refreshChildren(new AsyncCallback<TreeNode<?>>() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(TreeNode<?> result) {
                for (TreeNode<?> treeNode : result.getChildren().asIterable()) {
                    if (treeNode instanceof JarNode) {
                        if (((JarNode)treeNode).getData().getId() == libId) {
                            String[] segments;
                            String separator;
                            if (path.startsWith("/")) {
                                segments = path.substring(1).split("/");
                                separator = "/";
                            } else {
                                segments = path.split("\\.");
                                if (!segments[segments.length - 1].endsWith(".class")) {
                                    segments[segments.length - 1] = segments[segments.length - 1] + ".class";
                                }
                                separator = ".";
                            }
                            refreshAndGetChildByName(treeNode, segments, 0, separator, callback);
                            return;
                        }
                    }
                }
            }
        });
    }

    private void refreshAndGetChildByName(TreeNode<?> node, final String[] path, final int index,
                                          final String separator, final AsyncCallback<TreeNode<?>> callback) {
        node.refreshChildren(new AsyncCallback<TreeNode<?>>() {
            @Override
            public void onSuccess(TreeNode<?> result) {
                for (TreeNode<?> childNode : result.getChildren().asIterable()) {
                    Pair<Boolean, Integer> pair = isPathSame(childNode.getId(), path, index, separator);
                    if (pair.first) {
                        if (index + 1 == path.length) {
                            callback.onSuccess(childNode);
                        } else {
                            refreshAndGetChildByName(childNode, path, pair.second + 1, separator, callback);
                        }
                        return;
                    }
                }
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    private Pair<Boolean, Integer> isPathSame(String path, String[] segments, int index, String separator) {
        if (path.equals(segments[index])) {
            return new Pair<>(true, index);
        }

        if (!path.startsWith(segments[index])) {
            return new Pair<>(false, index);
        }
        String collapsedPath = segments[index];
        int i = index;
        while (i < segments.length - 1) {
            i++;
            collapsedPath += separator + segments[i];
            if (path.equals(collapsedPath)) {
                return new Pair<>(true, i);
            }
        }

        return new Pair<>(false, index);
    }

    /** {@inheritDoc} */
    @Override
    public void getRoots(AsyncCallback<Array<TreeNode<?>>> callback) {
        AbstractTreeNode projectRoot =
                new JavaProjectNode(null, project, this, settings, eventBus, projectServiceClient, dtoUnmarshallerFactory);
        callback.onSuccess(Collections.<TreeNode<?>>createArray(projectRoot));
    }

    public JavaFolderNode newJavaFolderNode(AbstractTreeNode parent, ItemReference data) {
        return new JavaFolderNode(parent, data, this, settings, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory);
    }

    public SourceFolderNode newSourceFolderNode(AbstractTreeNode parent, ItemReference data) {
        return new SourceFolderNode(parent, data, this, settings, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory,
                                    iconRegistry);
    }

    public PackageNode newPackageNode(AbstractTreeNode parent, ItemReference data) {
        return new PackageNode(parent, data, this, settings, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory,
                               iconRegistry);
    }

    public SourceFileNode newSourceFileNode(AbstractTreeNode parent, ItemReference data) {
        return new SourceFileNode(parent, data, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    public ExternalLibrariesNode newExternalLibrariesNode(JavaProjectNode parent) {
        return new ExternalLibrariesNode(parent, new Object(), eventBus, this, iconRegistry, service, dtoUnmarshallerFactory);
    }

    public JarNode newJarNode(ExternalLibrariesNode parent, Jar jar) {
        return new JarNode(parent, jar, eventBus, this, service, dtoUnmarshallerFactory, iconRegistry);
    }

    public JarContainerNode newJarContainerNode(AbstractTreeNode<?> parent, JarEntry entry, int libId) {
        return new JarContainerNode(parent, entry, eventBus, libId, service, dtoUnmarshallerFactory, this, iconRegistry);
    }

    public TreeNode<?> newJarFileNode(AbstractTreeNode<?> parent, JarEntry entry, int libId) {
        return new JarFileNode(parent, entry, eventBus, libId, service, dtoUnmarshallerFactory, iconRegistry);
    }

    public TreeNode<?> newJarClassNode(AbstractTreeNode<?> parent, JarEntry entry, int libId) {
        return new JarClassNode(parent, entry, eventBus, libId, service, dtoUnmarshallerFactory, iconRegistry);
    }
}
