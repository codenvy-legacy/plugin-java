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

import com.codenvy.ide.ext.java.server.JavaNavigation;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class JarNavigationTest extends BaseTest {

    @Test
    public void testJars() throws Exception {
        List<Jar> jars = new JavaNavigation().getProjectDepandecyJars(project);
        assertThat(jars).isNotNull().isNotEmpty().onProperty("name").contains("rt.jar", "zipfs.jar", "dnsns.jar");
    }

    @Test
    public void testPackageFragmentContent() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getPackageFragmentRootContent(project, root.hashCode());
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").contains("META-INF", "java", "javax").excludes("(default package)");
        assertThat(rootContent).onProperty("path").contains("/META-INF");
    }

    @Test
    public void testNonJavaElement() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/ext/zipfs.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getPackageFragmentRootContent(project, root.hashCode());
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").contains("META-INF");
        assertThat(rootContent).onProperty("path").contains("/META-INF");
    }

    @Test
    public void testNonJavaFolder() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/ext/zipfs.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(), "/META-INF");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").contains("services", "MANIFEST.MF");
    }

    @Test
    public void testNonJavaFile() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/ext/zipfs.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(), "/META-INF/services");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").containsExactly("java.nio.file.spi.FileSystemProvider");
    }

    @Test
    public void testJavaPackage() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(),"java");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").contains("lang", "io", "util", "net", "nio");
    }

    @Test
    public void testPackageCollapsing() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(), "org");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").contains("omg", "w3c.dom", "xml.sax");
    }

    @Test
    public void testClassFile() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(),"java.lang");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").contains("Object.class", "String.class", "Integer.class");
    }

    @Test
    public void testClassFileFQN() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(),"java.lang");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("path").contains("java.lang.Object", "java.lang.String", "java.lang.Integer");
    }

    @Test
    public void testDoesNotReturnInnerClasses() throws Exception {
        String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
        IPackageFragmentRoot root = project.getPackageFragmentRoot(new File(javaHome));
        List<JarEntry> rootContent = new JavaNavigation().getChildren(project, root.hashCode(),"java.lang");
        assertThat(rootContent).isNotNull().isNotEmpty().onProperty("name").excludes("Character$Subset.class");
    }
}
