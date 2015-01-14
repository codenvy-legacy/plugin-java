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

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaNodeFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory that helps to create nodes for {@link MavenProjectTreeStructure}.
 *
 * @author Artem Zatsarynnyy
 * @see JavaNodeFactory
 */
public interface MavenNodeFactory extends JavaNodeFactory {
    /**
     * Creates a new {@link MavenProjectNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @param treeStructure
     *         the {@link MavenProjectTreeStructure} to create the node for
     * @return a new {@link MavenProjectNode}
     */
    MavenProjectNode newMavenProjectNode(@Nullable TreeNode<?> parent,
                                         @Nonnull ProjectDescriptor data,
                                         @Nonnull MavenProjectTreeStructure treeStructure);

    /**
     * Creates a new {@link MavenFolderNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link MavenProjectTreeStructure} to create the node for
     * @return a new {@link MavenFolderNode}
     */
    MavenFolderNode newMavenFolderNode(@Nonnull TreeNode<?> parent,
                                       @Nonnull ItemReference data,
                                       @Nonnull MavenProjectTreeStructure treeStructure);

    /**
     * Creates a new {@link ModuleNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @param treeStructure
     *         the {@link MavenProjectTreeStructure} to create the node for
     * @return a new {@link ModuleNode}
     */
    ModuleNode newModuleNode(@Nonnull TreeNode<?> parent,
                             @Nonnull ProjectDescriptor data,
                             @Nonnull MavenProjectTreeStructure treeStructure);
}
