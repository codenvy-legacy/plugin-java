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

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Node that represents Java project.
 *
 * @author Artem Zatsarynnyy
 * @author Vladyslav Zhukovskii
 */
public class JavaProjectNode extends ProjectNode {

    protected boolean shouldAddExternalLibrariesNode = true;
    private ExternalLibrariesNode librariesNode;

    @AssistedInject
    public JavaProjectNode(@Assisted TreeNode<?> parent,
                           @Assisted ProjectDescriptor data,
                           @Assisted JavaTreeStructure treeStructure,
                           EventBus eventBus,
                           ProjectServiceClient projectServiceClient,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
        librariesNode = treeStructure.newExternalLibrariesNode(this);
    }

    @Nonnull
    @Override
    public JavaTreeStructure getTreeStructure() {
        return (JavaTreeStructure)super.getTreeStructure();
    }

    @Override
    public void setChildren(Array<TreeNode<?>> children) {
        if (getTreeStructure().getSettings().isShowExternalLibraries() && shouldAddExternalLibrariesNode) {
            librariesNode = getTreeStructure().newExternalLibrariesNode(this);
            children.add(librariesNode);
        }
        super.setChildren(children);
    }

    public ExternalLibrariesNode getLibrariesNode() {
        return librariesNode;
    }
}
