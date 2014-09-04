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
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Node that represents a Java source file (class, interface, enum, etc.).
 *
 * @author Artem Zatsarynnyy
 */
public class SourceFileNode extends FileNode {
    public SourceFileNode(AbstractTreeNode parent, ItemReference data, EventBus eventBus, ProjectServiceClient projectServiceClient) {
        super(parent, data, eventBus, projectServiceClient);

        final String name = data.getName();
        // display name without '.java' extension
        getPresentation().setDisplayName(name.substring(0, name.length() - "java".length() - 1));
    }

    @Override
    public boolean isRenemable() {
        // Do not allow to rename Java source file as simple file.
        // This type of node needs to implement rename refactoring.
        return false;
    }
}
