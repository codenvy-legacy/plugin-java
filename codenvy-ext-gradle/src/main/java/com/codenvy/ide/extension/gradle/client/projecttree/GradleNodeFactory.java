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
package com.codenvy.ide.extension.gradle.client.projecttree;

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaNodeFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory that helps to create nodes for {@link com.codenvy.ide.extension.gradle.client.projecttree.GradleProjectTreeStructure}.
 *
 * @author Vladyslav Zhukovskii
 * @see com.codenvy.ide.ext.java.client.projecttree.JavaNodeFactory
 */
public interface GradleNodeFactory extends JavaNodeFactory {

    /**
     * Creates a new {@link GradleProjectNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @param treeStructure
     *         the {@link GradleProjectTreeStructure} to create the node for
     * @return a new {@link GradleProjectNode}
     */
    GradleProjectNode newGradleProjectNode(@Nullable TreeNode<?> parent,
                                           @Nonnull ProjectDescriptor data,
                                           @Nonnull GradleProjectTreeStructure treeStructure);

    /**
     * Creates a new {@link GradleFolderNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link GradleProjectTreeStructure} to create the node for
     * @return a new {@link GradleFolderNode}
     */
    GradleFolderNode newGradleFolderNode(@Nonnull TreeNode<?> parent,
                                         @Nonnull ItemReference data,
                                         @Nonnull GradleProjectTreeStructure treeStructure);

    /**
     * Creates a new {@link GradleModuleNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @param treeStructure
     *         the {@link GradleProjectTreeStructure} to create the node for
     * @return a new {@link GradleModuleNode}
     */
    GradleModuleNode newGradleModuleNode(@Nonnull TreeNode<?> parent,
                                         @Nonnull ProjectDescriptor data,
                                         @Nonnull GradleProjectTreeStructure treeStructure);


}
