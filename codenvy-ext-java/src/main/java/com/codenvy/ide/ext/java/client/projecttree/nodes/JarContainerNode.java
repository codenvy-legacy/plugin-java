/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
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
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Represents jar package or non java resource
 * @author Evgen Vidolob
 */
public class JarContainerNode extends JarEntryNode {

    /**
     * Creates new node with the specified parent, associated data and display name.
     *  @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param treeStructure
     *         {@link com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure} which this node belongs
     * @param libId
     * @param eventBus
     * @param service
     * @param dtoUnmarshallerFactory
     * @param iconRegistry
     */
    @Inject
    public JarContainerNode(@Assisted TreeNode<?> parent, @Assisted JarEntry data, @Assisted JavaTreeStructure treeStructure,
                            @Assisted int libId, EventBus eventBus, JavaNavigationService service,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(parent, data, treeStructure, eventBus, libId, service, dtoUnmarshallerFactory);
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

    @Nonnull
    @Override
    public JavaTreeStructure getTreeStructure() {
        return (JavaTreeStructure)super.getTreeStructure();
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        Unmarshallable<Array<JarEntry>> unmarshaller = dtoUnmarshallerFactory.newArrayUnmarshaller(JarEntry.class);
        service.getChildren(getProject().getPath(), libId, getData().getPath(), new AsyncRequestCallback<Array<JarEntry>>(unmarshaller) {
            @Override
            protected void onSuccess(Array<JarEntry> result) {
                Array<TreeNode<?>> nodes = Collections.createArray();
                for (JarEntry jarNode : result.asIterable()) {
                    nodes.add(getTreeStructure().createNodeForJarEntry(JarContainerNode.this, jarNode, libId));
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


}
