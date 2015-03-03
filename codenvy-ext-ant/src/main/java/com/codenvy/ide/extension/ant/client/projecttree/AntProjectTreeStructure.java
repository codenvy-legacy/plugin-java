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
package com.codenvy.ide.extension.ant.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JavaFolderNode;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Tree structure for Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public class AntProjectTreeStructure extends JavaTreeStructure {

    /** Create instance of {@link AntProjectTreeStructure}. */
    protected AntProjectTreeStructure(AntNodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                      ProjectServiceClient projectServiceClient, IconRegistry iconRegistry,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory, JavaNavigationService service) {
        super(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory, service);
    }

    /** {@inheritDoc} */
    @Override
    public void getRootNodes(@Nonnull AsyncCallback<Array<TreeNode<?>>> callback) {
        if (projectNode == null) {
            final CurrentProject currentProject = appContext.getCurrentProject();
            if (currentProject != null) {
                projectNode = getNodeFactory().newAntProjectNode(null, currentProject.getRootProject(), this);
            } else {
                callback.onFailure(new IllegalStateException("No project is opened."));
                return;
            }
        }
        callback.onSuccess(Collections.<TreeNode<?>>createArray(projectNode));
    }

    @Override
    public AntNodeFactory getNodeFactory() {
        return (AntNodeFactory)nodeFactory;
    }

    /** {@inheritDoc} */
    @Override
    public JavaFolderNode newJavaFolderNode(@Nonnull AbstractTreeNode parent, @Nonnull ItemReference data) {
        return getNodeFactory().newAntFolderNode(parent, data, this);
    }
}
