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
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Represent non java file
 * @author Evgen Vidolob
 */
public class JarFileNode extends JarEntryNode {
    /**
     * Creates new node with the specified parent, associated data and display name.
     *  @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param eventBus
     * @param libId
     * @param service
     * @param dtoUnmarshallerFactory
     * @param iconRegistry
     */
    public JarFileNode(TreeNode<?> parent, JarEntry data,
                       EventBus eventBus, int libId,
                       JavaNavigationService service,
                       DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(parent, data, eventBus, libId, service, dtoUnmarshallerFactory);
        String[] split = data.getName().split("\\.");
        String ext = split[split.length - 1];
        setDisplayIcon(iconRegistry.getIcon(getProject().getProjectTypeId() + "/" + ext + ".file.small.icon").getSVGImage());
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {

    }
}
