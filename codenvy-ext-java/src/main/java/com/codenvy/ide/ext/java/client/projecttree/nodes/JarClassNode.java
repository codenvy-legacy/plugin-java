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

import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.VirtualFile;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Evgen Vidolob
 */
public class JarClassNode extends JarEntryNode implements VirtualFile {

    /**
     * Creates new node with the specified parent, associated data and display name.
     *
     * @param parent
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
    @AssistedInject
    public JarClassNode(@Assisted TreeNode<?> parent, @Assisted JarEntry data, @Assisted JavaTreeStructure treeStructure,
                        @Assisted int libId, EventBus eventBus, JavaNavigationService service,
                        DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(parent, data, treeStructure, eventBus, libId, service, dtoUnmarshallerFactory);
        setDisplayIcon(iconRegistry.getIcon("java.class").getSVGImage());
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {

    }

    @Override
    public void processNodeAction() {
        eventBus.fireEvent(new FileEvent(this, FileEvent.FileOperation.OPEN));
    }

    @Nonnull
    @Override
    public String getPath() {
        return getData().getPath();
    }

    @Nonnull
    @Override
    public String getName() {
        return getData().getName();
    }

    @Nullable
    @Override
    public String getMediaType() {
        return "application/java-class";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getContentUrl() {
        return null;
    }

    @Override
    public void getContent(final AsyncCallback<String> callback) {
        service.getContent(getProject().getPath(), libId, getData().getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            @Override
            protected void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void updateContent(String content, AsyncCallback<Void> callback) {
        throw new UnsupportedOperationException("Update content on class file is not supported.");
    }
}
