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
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Represents jar package or non java resource
 * @author Evgen Vidolob
 */
public class JarContainerNode extends JarEntryNode {


    private JavaTreeStructure treeStructure;

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
    public JarContainerNode(TreeNode<?> parent, JarEntry data, EventBus eventBus, int libId,
                            JavaNavigationService service, DtoUnmarshallerFactory dtoUnmarshallerFactory, JavaTreeStructure treeStructure,  IconRegistry iconRegistry) {
        super(parent, data, eventBus, libId, service, dtoUnmarshallerFactory);
        this.treeStructure = treeStructure;
        if(data.getType() == JarEntry.JarEntryType.PACKAGE){
            setDisplayIcon(iconRegistry.getIcon("java.package").getSVGImage());
        } else {
            setDisplayIcon(iconRegistry.getIcon(getProject().getProjectTypeId() + ".folder.small.icon").getSVGImage());
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        Unmarshallable<Array<JarEntry>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(JarEntry.class);
        service.getChildren(getProject().getPath(), libId, data.getPath(), new AsyncRequestCallback<Array<JarEntry>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<JarEntry> result) {
                Array<TreeNode<?>> nodes = Collections.createArray();
                for (JarEntry jarNode : result.asIterable()) {
                    nodes.add(createNode(jarNode));
                }
                setChildren(nodes);
                callback.onSuccess(JarContainerNode.this);
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
                return treeStructure.newJarContainerNode(this, entry, libId);

            case FILE:
                return treeStructure.newJarFileNode(this, entry, libId);

            case CLASS_FILE:
                return treeStructure.newJarClassNode(this, entry, libId);
        }
        return null;
    }

}
