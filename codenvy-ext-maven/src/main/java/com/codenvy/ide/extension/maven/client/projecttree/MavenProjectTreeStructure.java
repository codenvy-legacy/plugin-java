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
    private MavenProjectNode projectNode;

    protected MavenProjectTreeStructure(MavenNodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                        ProjectServiceClient projectServiceClient, IconRegistry iconRegistry,
                                        DtoUnmarshallerFactory dtoUnmarshallerFactory, JavaNavigationService javaNavigationService) {
        super(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory, javaNavigationService);
    }

    /** {@inheritDoc} */
    @Override
    public void getRootNodes(@Nonnull AsyncCallback<Array<TreeNode<?>>> callback) {
        if (projectNode == null) {
            final CurrentProject currentProject = appContext.getCurrentProject();
            if (currentProject != null) {
                projectNode = newMavenProjectNode(currentProject.getRootProject());
            } else {
                callback.onFailure(new IllegalStateException("No project is opened."));
                return;
            }
        }
        callback.onSuccess(Collections.<TreeNode<?>>createArray(projectNode));
    }

    @Override
    public MavenNodeFactory getNodeFactory() {
        return (MavenNodeFactory)nodeFactory;
    }

    private MavenProjectNode newMavenProjectNode(ProjectDescriptor data) {
        return getNodeFactory().newMavenProjectNode(null, data, this);
    }

    @Override
    public MavenFolderNode newJavaFolderNode(@Nonnull AbstractTreeNode parent, @Nonnull ItemReference data) {
        if (!"folder".equals(data.getType())) {
            throw new IllegalArgumentException("The associated ItemReference type must be - folder.");
        }
        return getNodeFactory().newMavenFolderNode(parent, data, this);
    }

    /**
     * Creates a new {@link ModuleNode} owned by this tree
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @return a new {@link ModuleNode}
     */
    public ModuleNode newModuleNode(@Nonnull AbstractTreeNode parent, @Nonnull ProjectDescriptor data) {
        return getNodeFactory().newModuleNode(parent, data, this);
    }
}
