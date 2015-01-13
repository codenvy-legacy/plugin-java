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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyy */
public class SourceFolderNodeTest extends BaseNodeTest {
    @Mock
    private ItemReference    folderItemReference;
    private SourceFolderNode sourceFolderNode;

    @Before
    public void setUp() {
        super.setUp();
        sourceFolderNode =
                new SourceFolderNode(null, folderItemReference, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory,
                                     iconRegistry);
    }

    @Test
    public void testIsRenamable() throws Exception {
        assertFalse(sourceFolderNode.isRenamable());
    }

    @Test
    public void shouldCreateChildSourceFileNode() {
        ItemReference javaFileItem = mock(ItemReference.class);
        when(javaFileItem.getName()).thenReturn("Test.java");
        when(javaFileItem.getType()).thenReturn("file");

        sourceFolderNode.createChildNode(javaFileItem);

        verify(treeStructure).newSourceFileNode(eq(sourceFolderNode), eq(javaFileItem));
    }

    @Test
    public void shouldCreateChildPackageNode() {
        ItemReference packageItem = mock(ItemReference.class);
        when(packageItem.getType()).thenReturn("folder");

        sourceFolderNode.createChildNode(packageItem);

        verify(treeStructure).newPackageNode(eq(sourceFolderNode), eq(packageItem));
    }
}
