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
package com.codenvy.ide.extension.gradle.client.task.tree;

import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.codenvy.ide.gradle.dto.GrdProject;
import com.codenvy.ide.gradle.dto.GrdTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory that helps to create nodes for {@link GradleTaskTreeStructure}.
 *
 * @author Vladyslav Zhukovskii */
public interface GradleTaskNodeFactory {

    /**
     * Creates a new {@link GradleProjectNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link GrdConfiguration}
     * @param treeStructure
     *         the {@link GradleTaskTreeStructure} to create the node for
     * @return a new {@link GradleTaskTreeStructure}
     */
    GradleProjectNode newGradleProjectNode(@Nullable TreeNode<?> parent,
                                           @Nonnull GrdConfiguration data,
                                           @Nonnull GradleTaskTreeStructure treeStructure);

    /**
     * Creates a new {@link GradleModuleNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link GrdProject}
     * @param treeStructure
     *         the {@link GradleTaskTreeStructure} to create the node for
     * @return a new {@link GradleTaskTreeStructure}
     */
    GradleModuleNode newGradleModuleNode(@Nullable TreeNode<?> parent,
                                         @Nonnull GrdProject data,
                                         @Nonnull GradleTaskTreeStructure treeStructure);

    /**
     * Creates a new {@link GradleTaskNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link GrdTask}
     * @param treeStructure
     *         the {@link GradleTaskTreeStructure} to create the node for
     * @return a new {@link GradleTaskTreeStructure}
     */
    GradleTaskNode newGradleTaskNode(@Nullable TreeNode<?> parent,
                                     @Nonnull GrdTask data,
                                     @Nonnull GradleTaskTreeStructure treeStructure);
}
