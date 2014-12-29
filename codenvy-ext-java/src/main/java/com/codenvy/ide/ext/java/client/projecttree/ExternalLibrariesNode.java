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

import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.Openable;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * @author Evgen Vidolob
 */
public class ExternalLibrariesNode extends AbstractTreeNode<Object> implements Openable {
    private JavaTreeStructure      treeStructure;
    private JavaNavigationService  service;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private boolean                opened;

    /**
     * Creates new node with the specified parent, associated data and display name.
     *
     * @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param eventBus
     */
    ExternalLibrariesNode(JavaProjectNode parent, Object data, EventBus eventBus, JavaTreeStructure treeStructure,
                          IconRegistry iconRegistry, JavaNavigationService service,
                          DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, eventBus);
        this.treeStructure = treeStructure;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        setDisplayIcon(iconRegistry.getIcon("java.libraries").getSVGImage());
    }

    @Nonnull
    @Override
    public String getId() {
        return "External Libraries";
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "External Libraries";
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        Unmarshallable<Array<Jar>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(Jar.class);
        service.getExternalLibraries(parent.getProject().getPath(), new AsyncRequestCallback<Array<Jar>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<Jar> result) {
                Array<TreeNode<?>> array = Collections.createArray();
                for (Jar jar : result.asIterable()) {
                    array.add(treeStructure.newJarNode(ExternalLibrariesNode.this, jar));
                }
                setChildren(array);
                callback.onSuccess(ExternalLibrariesNode.this);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }


    @Override
    public void close() {
        opened = false;
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void open() {
        opened = true;
    }
}
