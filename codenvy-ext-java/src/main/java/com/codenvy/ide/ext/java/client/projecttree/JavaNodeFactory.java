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

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.NodeFactory;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;

/**
 * Factory that helps to create nodes for {@link JavaTreeStructure}.
 *
 * @author Artem Zatsarynnyy
 * @see NodeFactory
 */
public interface JavaNodeFactory extends NodeFactory {
    JavaProjectNode newJavaProjectNode(TreeNode<?> parent, ProjectDescriptor data, JavaTreeStructure treeStructure);

    JavaFolderNode newJavaFolderNode(TreeNode<?> parent, ItemReference data, JavaTreeStructure treeStructure);

    SourceFolderNode newSourceFolderNode(TreeNode<?> parent, ItemReference data, JavaTreeStructure treeStructure);

    PackageNode newPackageNode(TreeNode<?> parent, ItemReference data, JavaTreeStructure treeStructure);

    SourceFileNode newSourceFileNode(TreeNode<?> parent, ItemReference data);

    ExternalLibrariesNode newExternalLibrariesNode(JavaProjectNode parent, Object o, JavaTreeStructure treeStructure);

    JarNode newJarNode(ExternalLibrariesNode parent, Jar jar, JavaTreeStructure treeStructure);

    JarContainerNode newJarContainerNode(TreeNode<?> parent, JarEntry data, int libId, JavaTreeStructure treeStructure);

    JarFileNode newJarFileNode(TreeNode<?> parent, JarEntry data, int libId);

    JarClassNode newJarClassNode(TreeNode<?> parent, JarEntry data, int libId);
}
