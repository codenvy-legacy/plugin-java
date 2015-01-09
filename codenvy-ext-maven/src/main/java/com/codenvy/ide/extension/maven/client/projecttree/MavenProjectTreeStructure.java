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
package com.codenvy.ide.extension.maven.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Tree structure for Maven project. It also respects multi-module projects.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenProjectTreeStructure extends JavaTreeStructure {

    protected MavenProjectTreeStructure(MavenNodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                        ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                        IconRegistry iconRegistry, JavaNavigationService service) {
        super(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory, service);
    }

    /** {@inheritDoc} */
    @Override
    public void getRootNodes(@Nonnull AsyncCallback<Array<TreeNode<?>>> callback) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            ProjectNode projectRoot = getNodeFactory().newMavenProjectNode(null, currentProject.getRootProject(), this);
            callback.onSuccess(Collections.<TreeNode<?>>createArray(projectRoot));
        } else {
            callback.onFailure(new IllegalStateException("No opened project"));
        }
    }

    @Override
    public MavenNodeFactory getNodeFactory() {
        return (MavenNodeFactory)nodeFactory;
    }

    @Override
    public MavenFolderNode newJavaFolderNode(AbstractTreeNode parent, ItemReference data) {
        return getNodeFactory().newMavenFolderNode(parent, data, this);
    }

    public ModuleNode newModuleNode(AbstractTreeNode parent, ProjectDescriptor data) {
        return getNodeFactory().newModuleNode(parent, data, this);
    }
}
