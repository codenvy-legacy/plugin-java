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
package com.codenvy.ide.ext.java.client.projecttree.nodes;

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyy */
public class PackageNodeTest extends BaseNodeTest {
    private static final String SOURCE_FOLDER_ITEM_PATH  = "/project/src/main/java";
    private static final String PACKAGE_ITEM_NAME        = "pack_2";
    private static final String PACKAGE_ITEM_PATH        = "/project/src/main/java/pack_1/pack_2";
    private static final String PARENT_PACKAGE_ITEM_NAME = "pack_1";
    private static final String PARENT_PACKAGE_ITEM_PATH = "/project/src/main/java/pack_1";
    @Mock
    private SourceFolderNode sourceFolderNode;
    @Mock
    private ItemReference    parentPackageItemReference;
    @Mock
    private ItemReference    packageItemReference;
    @Mock
    private PackageNode      parentPackageNode;
    private PackageNode      packageNode;

    @Before
    public void setUp() {
        super.setUp();

        when(sourceFolderNode.getPath()).thenReturn(SOURCE_FOLDER_ITEM_PATH);

        when(parentPackageItemReference.getPath()).thenReturn(PARENT_PACKAGE_ITEM_PATH);
        when(parentPackageItemReference.getName()).thenReturn(PARENT_PACKAGE_ITEM_NAME);
        when(packageItemReference.getPath()).thenReturn(PACKAGE_ITEM_PATH);
        when(packageItemReference.getName()).thenReturn(PACKAGE_ITEM_NAME);
        parentPackageNode = new PackageNode(projectNode, parentPackageItemReference, treeStructure, eventBus, projectServiceClient,
                                            dtoUnmarshallerFactory, iconRegistry);

        packageNode = new PackageNode(parentPackageNode, packageItemReference, treeStructure, eventBus, projectServiceClient,
                                      dtoUnmarshallerFactory, iconRegistry);

        final Array<TreeNode<?>> children = Collections.createArray();
        when(projectNode.getChildren()).thenReturn(children);
    }

    @Test
    public void testGetDisplayName() throws Exception {
        when(javaTreeSettings.isCompactEmptyPackages()).thenReturn(Boolean.FALSE);

        assertEquals(PACKAGE_ITEM_NAME, packageNode.getDisplayName());
    }

    @Test
    public void testGetDisplayNameForCompactedPackage() throws Exception {
        when(javaTreeSettings.isCompactEmptyPackages()).thenReturn(Boolean.TRUE);

        packageNode.setParent(sourceFolderNode);
        assertEquals(PARENT_PACKAGE_ITEM_NAME + '.' + PACKAGE_ITEM_NAME, packageNode.getDisplayName());
    }

    @Test
    public void testGetQualifiedName() throws Exception {
        assertEquals(PARENT_PACKAGE_ITEM_NAME + '.' + PACKAGE_ITEM_NAME, packageNode.getQualifiedName());
    }

    @Test
    public void testGetQualifiedNameWhenNoSourceFolders() throws Exception {
        when(projectDescriptor.getAttributes()).thenReturn(new HashMap<String, List<String>>());

        assertEquals("", packageNode.getQualifiedName());
    }

    @Test
    public void testIsRenamable() throws Exception {
        assertFalse(packageNode.isRenamable());
    }

    @Test
    public void shouldCreateChildSourceFileNode() {
        ItemReference javaFile = mock(ItemReference.class);
        when(javaFile.getName()).thenReturn("Test.java");
        when(javaFile.getType()).thenReturn("file");

        packageNode.createChildNode(javaFile);

        verify(treeStructure).newSourceFileNode(eq(packageNode), eq(javaFile));
    }

    @Test
    public void shouldCreateChildPackageNode() {
        ItemReference javaFile = mock(ItemReference.class);
        when(javaFile.getType()).thenReturn("folder");

        packageNode.createChildNode(javaFile);

        verify(treeStructure).newPackageNode(eq(packageNode), eq(javaFile));
    }
}
