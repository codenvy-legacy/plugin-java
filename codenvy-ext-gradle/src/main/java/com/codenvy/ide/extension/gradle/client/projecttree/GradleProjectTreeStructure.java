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
package com.codenvy.ide.extension.gradle.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/** @author Vladyslav Zhukovskii */
public class GradleProjectTreeStructure extends JavaTreeStructure {
    protected GradleProjectTreeStructure(GradleNodeFactory nodeFactory,
                                      EventBus eventBus, AppContext appContext,
                                      ProjectServiceClient projectServiceClient,
                                      IconRegistry iconRegistry,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                      JavaNavigationService javaNavigationService) {
        super(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory, javaNavigationService);
    }

    @Override
    public void getRootNodes(@Nonnull AsyncCallback<Array<TreeNode<?>>> callback) {
        if (projectNode == null) {
            final CurrentProject currentProject = appContext.getCurrentProject();
            if (currentProject != null) {
                projectNode = newGradleProjectNode(currentProject.getRootProject());
            } else {
                callback.onFailure(new IllegalStateException("No project is opened."));
                return;
            }
        }
        callback.onSuccess(Collections.<TreeNode<?>>createArray(projectNode));
    }

    @Override
    public GradleNodeFactory getNodeFactory() {
        return (GradleNodeFactory)nodeFactory;
    }

    private GradleProjectNode newGradleProjectNode(ProjectDescriptor data) {
        return getNodeFactory().newGradleProjectNode(null, data, this);
    }

    @Override
    public GradleFolderNode newJavaFolderNode(@Nonnull AbstractTreeNode parent, @Nonnull ItemReference data) {
        if (!"folder".equals(data.getType())) {
            throw new IllegalArgumentException("The associated ItemReference type must be - folder.");
        }

        return getNodeFactory().newGradleFolderNode(parent, data, this);
    }

    public ModuleNode newModuleNode(@Nonnull AbstractTreeNode parent, @Nonnull ProjectDescriptor data) {
        return getNodeFactory().newModuleNode(parent, data, this);
    }
}
