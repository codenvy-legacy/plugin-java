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
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.AbstractTreeStructure;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.api.projecttree.generic.GenericTreeStructure;
import com.codenvy.ide.api.projecttree.generic.ProjectRootNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

/**
 * {@link AbstractTreeStructure} for Java project.
 *
 * @author Artem Zatsarynnyy
 */
public abstract class AbstractJavaTreeStructure extends GenericTreeStructure {

    public AbstractJavaTreeStructure(EventBus eventBus, AppContext appContext, ProjectServiceClient projectServiceClient,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(eventBus, appContext, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @Override
    public void getRoots(AsyncCallback<Array<AbstractTreeNode<?>>> callback) {
        Array<AbstractTreeNode<?>> roots = Collections.<AbstractTreeNode<?>>createArray(
                new ProjectRootNode(null, appContext.getCurrentProject().getProjectDescription()), new ExternalLibrariesNode());
        callback.onSuccess(roots);
    }

    private static class ExternalLibrariesNode extends AbstractTreeNode<String> {
        protected ExternalLibrariesNode() {
            super(null, "External Libraries");
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return data;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isLeaf() {
            return false;
        }
    }

    private static class ClassNode extends FileNode {

        public ClassNode(AbstractTreeNode parent, ItemReference data) {
            super(parent, data);
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            final String name = data.getName();
            return name.substring(0, name.length() - "java".length());
        }
    }

    private static class SourceFolderNode extends FolderNode {

        public SourceFolderNode(AbstractTreeNode parent, ItemReference data) {
            super(parent, data);
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            return data.getName();
        }
    }

    private static class PackageNode extends FolderNode {

        public PackageNode(AbstractTreeNode parent, ItemReference data) {
            super(parent, data);
        }

        /** {@inheritDoc} */
        @Override
        public String getName() {
            // TODO
            return data.getName();
        }
    }
}
