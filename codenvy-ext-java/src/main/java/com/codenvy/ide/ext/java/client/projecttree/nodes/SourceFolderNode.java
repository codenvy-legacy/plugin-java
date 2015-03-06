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
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Node that represents a source folder (folder that may contains Java source files and packages directly).
 *
 * @author Artem Zatsarynnyy
 */
public class SourceFolderNode extends AbstractSourceContainerNode {

    @Inject
    public SourceFolderNode(@Assisted TreeNode<?> parent, @Assisted ItemReference data, @Assisted JavaTreeStructure treeStructure,
                            EventBus eventBus, ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            IconRegistry iconRegistry) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
        setDisplayIcon(iconRegistry.getIcon("java.sourceFolder").getSVGImage());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRenamable() {
        // Do not allow to rename source folder as simple folder.
        return false;
    }
}
