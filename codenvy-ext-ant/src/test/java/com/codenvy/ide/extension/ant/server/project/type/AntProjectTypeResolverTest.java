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
package com.codenvy.ide.extension.ant.server.project.type;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.DefaultProjectManager;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectConfig;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.api.project.server.type.ProjectTypeRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author Vladyslav Zhukovskii */
public class AntProjectTypeResolverTest {
//    private static final String workspace = "my_ws";
//    private AntProjectTypeResolver antProjectTypeResolver;
//
//    private static final String      vfsUser       = "dev";
//    private static final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));
//
//    private ProjectManager projectManager;
//
//    private String buildXML =
//            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
//            "<project basedir=\".\" default=\"build\" name=\"antproj\">\n" +
//            "    <target description=\"Builds the application\" name=\"build\">\n" +
//            "        <echo message=\"Hello, world\"/>\n" +
//            "    </target>\n" +
//            "</project>";
//
//
//    @Before
//    public void setUp() throws Exception {
//        final String vfsUser = "dev";
//        final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));
//        Set<ProjectType2> pts = new HashSet<>();
//        final ProjectType2 pt = new ProjectType2("ant", "ant") {
//            {
//                setDefaultBuilder("ant");
//            }
//        };
//
//
//
//        pts.add(pt);
//
//
//        final ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(pts);
//
//        final EventService eventService = new EventService();
//        final VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
//        final MemoryFileSystemProvider memoryFileSystemProvider =
//                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
//                    @Override
//                    public VirtualFileSystemUser getVirtualFileSystemUser() {
//                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
//                    }
//                }, vfsRegistry);
//        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);
//        projectManager = new DefaultProjectManager(vfsRegistry, eventService, projectTypeRegistry);
//        projectManager.createProject(workspace, "my_project", new ProjectConfig("", pt.getId()));
//
//        MockitoAnnotations.initMocks(this);
//        // Bind components
//        Injector injector = Guice.createInjector(new AbstractModule() {
//            @Override
//            protected void configure() {
//                Multibinder<ProjectTypeResolver> projectTypeResolverMultibinder =
//                        Multibinder.newSetBinder(binder(), ProjectTypeResolver.class);
//                projectTypeResolverMultibinder.addBinding().to(AntProjectTypeResolver.class);
//                bind(ProjectManager.class).toInstance(projectManager);
//            }
//        });
//        antProjectTypeResolver = injector.getInstance(AntProjectTypeResolver.class);
//        projectManager = injector.getInstance(ProjectManager.class);
//    }
//
//    @Test
//    public void withoutBuildXml() throws Exception {
//        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
//        boolean resolve = antProjectTypeResolver.resolve(test);
//        Assert.assertFalse(resolve);
//        Assert.assertNull(projectManager.getProject(workspace, "test"));
//    }
//
//    @Test
//    public void withBuildXml() throws Exception {
//        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
//        test.createFile("build.xml", buildXML.getBytes(), "text/xml");
//        boolean resolve = antProjectTypeResolver.resolve(test);
//        Assert.assertTrue(resolve);
//        Assert.assertNotNull(projectManager.getProject(workspace, "test"));
//        ;
//        Assert.assertNotNull(projectManager.getProject(workspace, "test").getConfig());
//        Assert.assertNotNull(projectManager.getProject(workspace, "test").getConfig().getTypeId());
//        Assert.assertEquals("ant", projectManager.getProject(workspace, "test").getConfig().getTypeId());
//        Assert.assertNotNull(projectManager.getProject(workspace, "test").getConfig().getBuilders());
//        Assert.assertEquals("ant", projectManager.getProject(workspace, "test").getConfig().getBuilders().getDefault());
//    }
//
//    @Test
//    public void withBuildXmlWithFolders() throws Exception {
//        FolderEntry test = projectManager.getProjectsRoot(workspace).createFolder("test");
//        test.createFile("build.xml", buildXML.getBytes(), "text/xml");
//
//        FolderEntry folder = test.createFolder("folder1");
//        folder.createFile("build.xml", buildXML.getBytes(), "text/xml");
//
//        FolderEntry folder1 = test.createFolder("folder2");
//        folder1.createFile("build.xml", buildXML.getBytes(), "text/xml");
//
//
//        boolean resolve = antProjectTypeResolver.resolve(test);
//        Assert.assertTrue(resolve);
//        Assert.assertNotNull(projectManager.getProject(workspace, "test"));
//        Assert.assertNull(projectManager.getProject(workspace, "test/folder1"));
//        Assert.assertNull(projectManager.getProject(workspace, "test/folder2"));
//    }
}
