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

package com.codenvy.ide.ext.java;

import com.codenvy.ide.ext.java.server.SourcesFromBytecodeGenerator;

import org.eclipse.jdt.core.IType;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Evgen Vidolob
 */
public class SourceFromBytecodeGeneratorTest extends BaseTest {


    private IType type;
    private IType zipFileSystem;

    @Before
    public void setUp() throws Exception {
        type = project.findType("com.sun.nio.zipfs.ZipFileStore");
        zipFileSystem = project.findType("com.sun.nio.zipfs.ZipFileSystem");
    }

    @Test
    public void testClassComment() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        Assertions.assertThat(source).isNotNull().isNotEmpty()
                  .contains("// Failed to get sources. Instead, stub sources have been generated.")
                  .contains("// Implementation of methods is unavailable.");
    }

    @Test
    public void testPackageDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        Assertions.assertThat(source).contains("package com.sun.nio.zipfs;");
    }

    @Test
    public void testClassDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        Assertions.assertThat(source).contains("public class ZipFileStore extends java.nio.file.FileStore {");
    }

    @Test
    public void testFieldsDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        Assertions.assertThat(source).contains("    private final com.sun.nio.zipfs.ZipFileSystem zfs;");
    }

    @Test
    public void testFieldsDeclaration2() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private boolean readOnly;");
    }

    @Test
    public void testFieldsDeclaration3() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private final boolean createNew;");
    }

    @Test
    public void testFieldsDeclaration4() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private static final java.util.Set<java.lang.String> supportedFileAttributeViews;");
    }

    @Test
    public void testFieldsDeclaration5() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private static final java.lang.String GLOB_SYNTAX = \"glob\";");
    }

    @Test
    public void testFieldsDeclaration6() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private static byte[] ROOTPATH;");
    }

    @Test
    public void testFieldsDeclaration7() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private java.util.LinkedHashMap<com.sun.nio.zipfs.ZipFileSystem.IndexNode,com.sun.nio.zipfs.ZipFileSystem.IndexNode> inodes;");
    }

    @Test
    public void testFieldsDeclaration8() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private com.sun.nio.zipfs.ZipFileSystem.IndexNode root;");
    }

    @Test
    public void testFieldsDeclaration9() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private final int MAX_FLATER = 20;");
    }

    @Test
    public void testConstructorDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    ZipFileSystem(com.sun.nio.zipfs.ZipFileSystemProvider arg0, java.nio.file.Path arg1, java.util.Map<java.lang.String,?> arg2) throws java.io.IOException { /* compiled code */ }");
    }

    @Test
    public void testMethodDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    public java.nio.file.spi.FileSystemProvider provider() { /* compiled code */ }");
    }

    @Test
    public void testMethodDeclaration2() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    public com.sun.nio.zipfs.ZipPath getPath(java.lang.String arg0, java.lang.String[] arg1) { /* compiled code */ }");
    }

    @Test
    public void testMethodDeclaration3() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    void createDirectory(byte[] arg0, java.nio.file.attribute.FileAttribute<?>[] arg1) throws java.io.IOException { /* compiled code */ }");
    }

    @Test
    public void testGenericMethodDeclaration() throws Exception {
        IType iType = project.findType("com.sun.nio.zipfs.ZipFileStore");
        String source = new SourcesFromBytecodeGenerator().generateSource(iType);
        Assertions.assertThat(source).contains("public <V extends java.nio.file.attribute.FileStoreAttributeView> V getFileStoreAttributeView(java.lang.Class<V> aClass) { /* compiled code */ }");
    }

    @Test
    public void testEnumDeclaration() throws Exception {
        IType enumType = project.findType("javax.servlet.DispatcherType");
        String source = new SourcesFromBytecodeGenerator().generateSource(enumType);
        Assertions.assertThat(source).contains("\n" +
                                               "public final enum DispatcherType {\n" +
                                               "    FORWARD, INCLUDE, REQUEST, ASYNC, ERROR;\n" +
                                               "\n" +
                                               "    public static javax.servlet.DispatcherType[] values() { /* compiled code */ }\n" +
                                               "\n" +
                                               "    public static javax.servlet.DispatcherType valueOf(java.lang.String name) { /* " +
                                               "compiled code */ }\n" +
                                               "\n" +
                                               "    private DispatcherType() { /* compiled code */ }\n" +
                                               "\n" +
                                               "}");
    }

    @Test
    public void testInnerTypeDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        Assertions.assertThat(source).contains("    private static class ExChannelCloser {\n" +
                                               "        java.nio.file.Path path;\n" +
                                               "        java.nio.channels.SeekableByteChannel ch;\n" +
                                               "        java.util.Set<java.io.InputStream> streams;\n" +
                                               "\n" +
                                               "        ExChannelCloser(java.nio.file.Path arg0, java.nio.channels.SeekableByteChannel " +
                                               "arg1, java.util.Set<java.io.InputStream> arg2) { /* compiled code */ }\n" +
                                               "\n"+
                                               "    }");
    }




}
