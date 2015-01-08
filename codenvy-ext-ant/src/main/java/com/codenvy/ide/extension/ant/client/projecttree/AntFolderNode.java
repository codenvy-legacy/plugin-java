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
package com.codenvy.ide.extension.ant.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaFolderNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nullable;

/**
 * {@link AntFolderNode} that may contains {@link JavaFolderNode}s.
 *
 * @author Vladyslav Zhukovskii
 */
public class AntFolderNode extends JavaFolderNode {

    /** Create instance of {@link AntFolderNode}. */
    @AssistedInject
    protected AntFolderNode(@Assisted TreeNode<?> parent, @Assisted ItemReference data, @Assisted AntProjectTreeStructure treeStructure,
                            EventBus eventBus, EditorAgent editorAgent, ProjectServiceClient projectServiceClient,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory);
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    protected AbstractTreeNode<?> createChildNode(ItemReference item) {
        if (JavaSourceFolderUtil.isSourceFolder(item, getProject())) {
            return ((AntProjectTreeStructure)treeStructure).newSourceFolderNode(this, item);
        } else if ("folder".equals(item.getType())) {
            return ((AntProjectTreeStructure)treeStructure).newJavaFolderNode(this, item);
        } else {
            return super.createChildNode(item);
        }
    }
}
