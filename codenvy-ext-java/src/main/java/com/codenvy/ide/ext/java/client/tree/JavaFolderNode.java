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
package com.codenvy.ide.ext.java.client.tree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * {@link FolderNode} that may contains {@link SourceFolderNode}s.
 *
 * @author Artem Zatsarynnyy
 */
public class JavaFolderNode extends FolderNode {

    private IconRegistry iconRegistry;

    public JavaFolderNode(AbstractTreeNode parent, ItemReference data, TreeSettings settings, EventBus eventBus,
                          ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory,
                          IconRegistry iconRegistry) {
        super(parent, data, settings, eventBus, projectServiceClient, dtoUnmarshallerFactory);
        this.iconRegistry = iconRegistry;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshChildren(final AsyncCallback<AbstractTreeNode<?>> callback) {
        final Unmarshallable<Array<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(ItemReference.class);
        projectServiceClient.getChildren(data.getPath(), new AsyncRequestCallback<Array<ItemReference>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<ItemReference> children) {
                final boolean isShowHiddenItems = settings.isShowHiddenItems();
                Array<AbstractTreeNode<?>> newChildren = Collections.createArray();
                setChildren(newChildren);
                for (ItemReference item : children.asIterable()) {
                    if (isShowHiddenItems || !item.getName().startsWith(".")) {
                        if (isFile(item)) {
                            newChildren.add(new FileNode(JavaFolderNode.this, item, eventBus));
                        } else if (isFolder(item)) {
                            if (isSourceFolder(item)) {
                                newChildren.add(new SourceFolderNode(JavaFolderNode.this, item, settings, eventBus, projectServiceClient,
                                                                     dtoUnmarshallerFactory, iconRegistry));
                            } else {
                                newChildren.add(new JavaFolderNode(JavaFolderNode.this, item, settings, eventBus, projectServiceClient,
                                                                   dtoUnmarshallerFactory, iconRegistry));
                            }
                        }
                    }
                }
                callback.onSuccess(JavaFolderNode.this);
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
        return isFolder(item) && (item.getPath().endsWith("src/main/java") || item.getPath().endsWith("src/test/java"));
    }
}
