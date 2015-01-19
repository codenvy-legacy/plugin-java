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

/** @author Vladyslav Zhukovskii */
public interface GradleNodeFactory extends JavaNodeFactory {

    GradleProjectNode newGradleProjectNode(@Nullable TreeNode<?> parent,
                                           @Nonnull ProjectDescriptor data,
                                           @Nonnull GradleProjectTreeStructure treeStructure);

    GradleFolderNode newGradleFolderNode(@Nonnull TreeNode<?> parent,
                                         @Nonnull ItemReference data,
                                         @Nonnull GradleProjectTreeStructure treeStructure);

    ModuleNode newModuleNode(@Nonnull TreeNode<?> parent,
                             @Nonnull ProjectDescriptor data,
                             @Nonnull GradleProjectTreeStructure treeStructure);


}
