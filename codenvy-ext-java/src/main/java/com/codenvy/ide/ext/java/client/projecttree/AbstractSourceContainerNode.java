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
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import java.util.Comparator;

/**
 * Abstract node that represents a container for Java source files and packages.
 * It may resolve 'empty' child packages as one 'compacted' package (e.g. com.codenvy.ide).
 * A package is considered 'empty' if it has only one child package.
 *
 * @author Artem Zatsarynnyy
 * @see SourceFolderNode
 * @see PackageNode
 */
public abstract class AbstractSourceContainerNode extends FolderNode {
    protected Comparator<TreeNode> comparator = new NodeComparator();

    public AbstractSourceContainerNode(TreeNode<?> parent, ItemReference data, JavaTreeStructure treeStructure, EventBus eventBus,
                                       EditorAgent editorAgent, ProjectServiceClient projectServiceClient,
                                       DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory);
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        if (!((JavaTreeStructure)treeStructure).getSettings().isCompactEmptyPackages()) {
            super.refreshChildren(callback);
        } else {
            super.refreshChildren(new AsyncCallback<TreeNode<?>>() {
                @Override
                public void onSuccess(TreeNode<?> result) {
                    replaceEmptyChildPackagesWithCompacted(callback);
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }
            });
        }
    }

    @Override
    protected AbstractTreeNode<?> createChildNode(ItemReference item) {
        if ("file".equals(item.getType()) && item.getName().endsWith(".java")) {
            return ((JavaTreeStructure)treeStructure).newSourceFileNode(this, item);
        } else if ("folder".equals(item.getType())) {
            return ((JavaTreeStructure)treeStructure).newPackageNode(this, item);
        } else {
            return super.createChildNode(item);
        }
    }

    private void replaceEmptyChildPackagesWithCompacted(final AsyncCallback<TreeNode<?>> callback) {
        final Array<TreeNode<?>> newChildren = Collections.createArray();
        for (final TreeNode<?> childNode : getChildren().asIterable()) {
            if (childNode instanceof PackageNode) {
                getCompactedPackage((PackageNode)childNode, new AsyncCallback<PackageNode>() {
                    @Override
                    public void onSuccess(PackageNode pack) {
                        newChildren.add(pack);
                        if (newChildren.size() == getChildren().size()) {
                            newChildren.sort(comparator);
                            setChildren(newChildren);
                            callback.onSuccess(AbstractSourceContainerNode.this);
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Log.error(AbstractSourceContainerNode.class, caught);
                    }
                });
            } else {
                newChildren.add(childNode);
                if (newChildren.size() == getChildren().size()) {
                    newChildren.sort(comparator);
                    setChildren(newChildren);
                    callback.onSuccess(AbstractSourceContainerNode.this);
                }
            }
        }
    }

    /** Get 'compacted' package node (like com.codenvy.ide) or the same package otherwise. */
    private void getCompactedPackage(final PackageNode pack, final AsyncCallback<PackageNode> callback) {
        getChildren(pack.getPath(), new AsyncCallback<Array<ItemReference>>() {
            @Override
            public void onSuccess(Array<ItemReference> children) {
                if (children.size() == 1 && "folder".equals(children.get(0).getType())) {
                    final PackageNode emptyPackage = ((JavaTreeStructure)treeStructure).newPackageNode(AbstractSourceContainerNode.this,
                                                                                                       children.get(0));
                    getCompactedPackage(emptyPackage, callback);
                } else {
                    callback.onSuccess(pack);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    private static class NodeComparator implements Comparator<TreeNode> {
        @Override
        public int compare(TreeNode o1, TreeNode o2) {
            if (o1 instanceof FolderNode && o2 instanceof FileNode) {
                return -1;
            }
            if (o1 instanceof FileNode && o2 instanceof FolderNode) {
                return 1;
            }
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    }
}
