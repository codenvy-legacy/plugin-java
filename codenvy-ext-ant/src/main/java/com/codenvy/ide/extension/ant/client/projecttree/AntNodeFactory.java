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

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaNodeFactory;

/**
 * Factory that helps to create nodes for {@link AntProjectTreeStructure}.
 *
 * @author Artem Zatsarynnyy
 * @see JavaNodeFactory
 */
public interface AntNodeFactory extends JavaNodeFactory {
    AntFolderNode newAntFolderNode(TreeNode<?> parent, ItemReference data, AntProjectTreeStructure treeStructure);

    AntProjectNode newAntProjectNode(TreeNode<?> parent, ProjectDescriptor data, AntProjectTreeStructure treeStructure);
}
