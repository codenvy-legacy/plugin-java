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
package org.eclipse.che.gradle.client.projecttree;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaNodeFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory that helps to create nodes for {@link com.codenvy.ide.extension.gradle.client.projecttree.GradleProjectTreeStructure}.
 *
 * @author Vladyslav Zhukovskii
 * @see JavaNodeFactory
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
