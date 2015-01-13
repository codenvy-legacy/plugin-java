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

package com.codenvy.ide.ext.java.client.projecttree.nodes;

import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * @author Evgen Vidolob
 */
public class JarNode extends AbstractTreeNode<Jar> {

    private JavaTreeStructure      treeStructure;
    private JavaNavigationService  service;
    private DtoUnmarshallerFactory factory;

    /**
     * Creates new node with the specified parent, associated data and display name.
     *  @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param javaTreeStructure
     *         {@link JavaTreeStructure} which this node belongs
     * @param eventBus
     */
    @AssistedInject
    public JarNode(@Assisted ExternalLibrariesNode parent, @Assisted Jar data, @Assisted JavaTreeStructure javaTreeStructure,
                   EventBus eventBus, JavaNavigationService service, DtoUnmarshallerFactory factory, IconRegistry registry) {
        super(parent, data, javaTreeStructure, eventBus);
        treeStructure = javaTreeStructure;
        this.service = service;
        this.factory = factory;
        setDisplayIcon(registry.getIcon("java.jar").getSVGImage());
    }

    @Nonnull
    @Override
    public String getId() {
        return String.valueOf(getData().getId());
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return getData().getName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        Unmarshallable<Array<JarEntry>> unmarshaller = factory.newArrayUnmarshaller(JarEntry.class);
        service.getLibraryChildren(getParent().getProject().getPath(), getData().getId(),
                                   new AsyncRequestCallback<Array<JarEntry>>(unmarshaller) {
                                       @Override
                                       protected void onSuccess(Array<JarEntry> result) {
                Array<TreeNode<?>> nodes = Collections.createArray();
                for (JarEntry jarNode : result.asIterable()) {
                    nodes.add(createNode(jarNode));
                }
                setChildren(nodes);
                callback.onSuccess(JarNode.this);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private TreeNode<?> createNode(JarEntry entry) {
        switch (entry.getType()){
            case FOLDER:
            case PACKAGE:
            return treeStructure.newJarContainerNode(this, entry, getData().getId());

            case FILE:
                return treeStructure.newJarFileNode(this, entry, getData().getId());

            case CLASS_FILE:
                return treeStructure.newJarClassNode(this, entry, getData().getId());
        }
        return null;
    }
}
