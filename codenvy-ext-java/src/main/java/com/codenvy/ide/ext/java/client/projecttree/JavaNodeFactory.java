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
package com.codenvy.ide.ext.java.client.projecttree;

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.NodeFactory;
import com.codenvy.ide.ext.java.client.projecttree.nodes.ExternalLibrariesNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JarClassNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JarContainerNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JarFileNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JarNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JavaFolderNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JavaProjectNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFileNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFolderNode;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factory that helps to create nodes for {@link JavaTreeStructure}.
 *
 * @author Artem Zatsarynnyy
 * @see NodeFactory
 */
public interface JavaNodeFactory extends NodeFactory {
    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JavaProjectNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ProjectDescriptor}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JavaProjectNode}
     */
    JavaProjectNode newJavaProjectNode(@Nullable TreeNode<?> parent,
                                       @Nonnull ProjectDescriptor data,
                                       @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JavaFolderNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JavaFolderNode}
     */
    JavaFolderNode newJavaFolderNode(@Nonnull TreeNode<?> parent,
                                     @Nonnull ItemReference data,
                                     @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFolderNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFolderNode}
     */
    SourceFolderNode newSourceFolderNode(@Nonnull TreeNode<?> parent,
                                         @Nonnull ItemReference data,
                                         @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.PackageNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.PackageNode}
     */
    PackageNode newPackageNode(@Nonnull TreeNode<?> parent,
                               @Nonnull ItemReference data,
                               @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFileNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link ItemReference}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFileNode}
     */
    SourceFileNode newSourceFileNode(@Nonnull TreeNode<?> parent,
                                     @Nonnull ItemReference data,
                                     @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.ExternalLibrariesNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated data
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.ExternalLibrariesNode}
     */
    ExternalLibrariesNode newExternalLibrariesNode(@Nonnull JavaProjectNode parent,
                                                   @Nonnull Object data,
                                                   @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent} and associated {@code data}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link Jar}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarNode}
     */
    JarNode newJarNode(@Nonnull ExternalLibrariesNode parent,
                       @Nonnull Jar data,
                       @Nonnull JavaTreeStructure treeStructure);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarContainerNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent}, associated {@code data} and {@code libId}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link JarEntry}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @param libId
     *         lib ID
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarContainerNode}
     */
    JarContainerNode newJarContainerNode(@Nonnull TreeNode<?> parent,
                                         @Nonnull JarEntry data,
                                         @Nonnull JavaTreeStructure treeStructure,
                                         int libId);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarFileNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent}, associated {@code data} and {@code libId}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link JarEntry}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @param libId
     *         lib ID
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarFileNode}
     */
    JarFileNode newJarFileNode(@Nonnull TreeNode<?> parent,
                               @Nonnull JarEntry data,
                               @Nonnull JavaTreeStructure treeStructure,
                               int libId);

    /**
     * Creates a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarClassNode} owned by the specified {@code treeStructure}
     * with the specified {@code parent}, associated {@code data} and {@code libId}.
     *
     * @param parent
     *         the parent node
     * @param data
     *         the associated {@link JarEntry}
     * @param treeStructure
     *         the {@link JavaTreeStructure} to create the node for
     * @param libId
     *         lib ID
     * @return a new {@link com.codenvy.ide.ext.java.client.projecttree.nodes.JarClassNode}
     */
    JarClassNode newJarClassNode(@Nonnull TreeNode<?> parent,
                                 @Nonnull JarEntry data,
                                 @Nonnull JavaTreeStructure treeStructure,
                                 int libId);
}
