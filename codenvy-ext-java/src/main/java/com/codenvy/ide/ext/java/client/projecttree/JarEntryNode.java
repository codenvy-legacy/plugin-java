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

import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * @author Evgen Vidolob
 */
public abstract class JarEntryNode extends AbstractTreeNode<JarEntry> {

    protected final JavaNavigationService  service;
    protected final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    protected       int                    libId;

    /**
     * Creates new node with the specified parent, associated data and display name.
     *
     * @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param eventBus
     */
    public JarEntryNode(TreeNode<?> parent, JarEntry data,
                        EventBus eventBus, int libId, JavaNavigationService service,
                        DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, eventBus);
        this.libId = libId;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Nonnull
    @Override
    public String getId() {
        return data.getName();
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return data.getName();
    }
}
