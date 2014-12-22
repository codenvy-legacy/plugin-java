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
package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.*;
import com.codenvy.api.project.server.type.Attribute2;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.api.project.server.type.ProjectTypeRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.ide.extension.maven.server.projecttype.MavenProjectTypeResolver;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author vetal
 */
public class MavenProjectTypeResolverTest {

    private static final String workspace = "my_ws";

    private String pomJar =
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>com.codenvy.sdk</groupId>\n" +
            "    <artifactId>codenvy-sdk-parent</artifactId>\n" +
            "    <version>3.1.0-SNAPSHOT</version>\n" +
            "</project>";

    private String pom =
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>com.codenvy.sdk</groupId>\n" +
            "    <artifactId>codenvy-sdk-parent</artifactId>\n" +
            "    <version>3.1.0-SNAPSHOT</version>\n" +
            "    <packaging>pom</packaging>\n" +
            "    <modules>" +
            "      <module>module1</module>" +
            "      <module>module2</module>" +
            "   </modules>" +
            "</project>";

    private String pomWithNestingModule =
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>com.codenvy.sdk</groupId>\n" +
            "    <artifactId>codenvy-sdk-parent</artifactId>\n" +
            "    <version>3.1.0-SNAPSHOT</version>\n" +
            "    <packaging>pom</packaging>\n" +
            "    <modules>" +
            "      <module>../module2</module>" +
            "      <module>../module3</module>" +
            "   </modules>" +
            "</project>";

    private MavenProjectTypeResolver mavenProjectTypeResolver;

    private static final String      vfsUser       = "dev";
    private static final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));

    private ProjectManager projectManager;

    @Before
    public void setUp() throws Exception {

        Set<ProjectType2> pts = new HashSet<>();
        final ProjectType2 pt = new ProjectType2("maven", "Maven type") {
            {
                //addParent(parent);
                //addConstantDefinition("child_const", "Constant", "const_value");
            }
        };


        pts.add(pt);
        final ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(pts);


        VirtualFileSystemRegistry virtualFileSystemRegistry = new VirtualFileSystemRegistry();
        EventService eventService = new EventService();
        ProjectGeneratorRegistry generatorRegistry = new ProjectGeneratorRegistry(new HashSet<ProjectGenerator>());
        projectManager =
                new DefaultProjectManager(virtualFileSystemRegistry,
                                          eventService,
                                          projectTypeRegistry, generatorRegistry);
        MockitoAnnotations.initMocks(this);
        // Bind components
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Multibinder<ProjectTypeResolver> projectTypeResolverMultibinder =
                        Multibinder.newSetBinder(binder(), ProjectTypeResolver.class);
                projectTypeResolverMultibinder.addBinding().to(MavenProjectTypeResolver.class);
                bind(ProjectManager.class).toInstance(projectManager);
            }
        });


        final MemoryFileSystemProvider memoryFileSystemProvider =
                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
                    @Override
                    public VirtualFileSystemUser getVirtualFileSystemUser() {
                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
                    }
                }, virtualFileSystemRegistry);
        virtualFileSystemRegistry.registerProvider(workspace, memoryFileSystemProvider);



        //projectTypeRegistry.registerProjectType(projectType);
        mavenProjectTypeResolver = injector.getInstance(MavenProjectTypeResolver.class);
        projectManager = injector.getInstance(ProjectManager.class);
    }


    @Test
    public void withoutPomXml() throws Exception {
        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
        boolean resolve = mavenProjectTypeResolver.resolve(test);
        Assert.assertFalse(resolve);
        Assert.assertNull(projectManager.getProject(workspace, "test"));
    }

    @Test
    public void withPomXml() throws Exception {
        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
        test.createFile("pom.xml", pomJar.getBytes(), "text/xml");
        boolean resolve = mavenProjectTypeResolver.resolve(test);
        Assert.assertTrue(resolve);
        Assert.assertNotNull(projectManager.getProject(workspace, "test"));
        Assert.assertNotNull(projectManager.getProject(workspace, "test").getConfig());
        Assert.assertNotNull(projectManager.getProject(workspace, "test").getConfig().getTypeId());
        Assert.assertEquals("maven", projectManager.getProject(workspace, "test").getConfig().getTypeId());
        Assert.assertNotNull(projectManager.getProject(workspace, "test").getConfig().getBuilders());
        Assert.assertEquals("maven", projectManager.getProject(workspace, "test").getConfig().getBuilders().getDefault());
    }


    @Test
    public void withPomXmlWithFolders() throws Exception {
        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
        test.createFile("pom.xml", pomJar.getBytes(), "text/xml");

        FolderEntry folder = test.createFolder("folder1");
        folder.createFile("pom.xml", pomJar.getBytes(), "text/xml");

        FolderEntry folder1 = test.createFolder("folder2");
        folder1.createFile("pom.xml", pomJar.getBytes(), "text/xml");


        boolean resolve = mavenProjectTypeResolver.resolve(test);
        Assert.assertTrue(resolve);
        Assert.assertNotNull(projectManager.getProject(workspace, "test"));
        Assert.assertNull(projectManager.getProject(workspace, "test/folder1"));
        Assert.assertNull(projectManager.getProject(workspace, "test/folder2"));

    }

    @Test
    public void withPomXmlMultiModule() throws Exception {
        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
        test.createFile("pom.xml", pom.getBytes(), "text/xml");

        FolderEntry module1 = test.createFolder("module1");
        module1.createFile("pom.xml", pom.getBytes(), "text/xml");

        FolderEntry module2 = test.createFolder("module2");
        module2.createFile("pom.xml", pom.getBytes(), "text/xml");

        FolderEntry moduleNotDescribedInParentPom = test.createFolder("moduleNotDescribedInParentPom");
        moduleNotDescribedInParentPom.createFile("pom.xml", pom.getBytes(), "text/xml");


        boolean resolve = mavenProjectTypeResolver.resolve(test);
        Assert.assertTrue(resolve);
        Assert.assertNotNull(projectManager.getProject(workspace, "test"));
        Assert.assertNotNull(projectManager.getProject(workspace, "test/module1"));
        Assert.assertNotNull(projectManager.getProject(workspace, "test/module2"));
        Assert.assertNull(projectManager.getProject(workspace, "test/moduleNotDescribedInParentPom"));
    }

    @Test
    public void withPomXmlMultiModuleWithNesting() throws Exception {
        //test for multi module project in which the modules are specified in format: <module>../module</module>
        FolderEntry rootProject = projectManager.getProjectsRoot(workspace).createFolder("rootProject");
        rootProject.createFile("pom.xml", pom.getBytes(), "text/xml");

        FolderEntry module1 = rootProject.createFolder("module1");
        module1.createFile("pom.xml", pomWithNestingModule.getBytes(), "text/xml");

        FolderEntry module2 = rootProject.createFolder("module2");
        module2.createFile("pom.xml", pom.getBytes(), "text/xml");

        FolderEntry module3 = rootProject.createFolder("module3");
        module3.createFile("pom.xml", pom.getBytes(), "text/xml");

        FolderEntry moduleNotDescribedInParentPom = rootProject.createFolder("moduleNotDescribedInParentPom");
        moduleNotDescribedInParentPom.createFile("pom.xml", pom.getBytes(), "text/xml");


        boolean resolve = mavenProjectTypeResolver.resolve(rootProject);
        Assert.assertTrue(resolve);
        Assert.assertNotNull(projectManager.getProject(workspace, "rootProject"));
        Assert.assertNotNull(projectManager.getProject(workspace, "rootProject/module1"));
        Assert.assertNotNull(projectManager.getProject(workspace, "rootProject/module2"));
        Assert.assertNotNull(projectManager.getProject(workspace, "rootProject/module3"));
        Assert.assertNull(projectManager.getProject(workspace, "rootProject/test/moduleNotDescribedInParentPom"));
    }

}
