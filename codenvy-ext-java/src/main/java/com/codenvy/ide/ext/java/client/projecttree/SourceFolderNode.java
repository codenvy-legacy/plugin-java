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

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Node that represents a source folder (folder that contains Java source files and packages directly).
 *
 * @author Artem Zatsarynnyy
 */
public class SourceFolderNode extends FolderNode {

    public SourceFolderNode(AbstractTreeNode parent, ItemReference data, JavaTreeStructure treeStructure, TreeSettings settings,
                            EventBus eventBus, EditorAgent editorAgent, ProjectServiceClient projectServiceClient,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(parent, data, treeStructure, settings, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory);
        setDisplayIcon(iconRegistry.getIcon("java.sourceFolder").getSVGImage());
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractTreeNode<?> createChildNode(ItemReference item) {
        if (isFile(item) && item.getName().endsWith(".java")) {
            return ((JavaTreeStructure)treeStructure).newSourceFileNode(this, item);
        } else if (isFolder(item)) {
            return ((JavaTreeStructure)treeStructure).newPackageNode(this, item);
        } else {
            return super.createChildNode(item);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRenamable() {
        // Do not allow to rename Maven source folder as simple folder.
        return false;
    }
}
