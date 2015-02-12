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
package com.codenvy.ide.extension.maven.server.projecttype.handler;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.DefaultProjectManager;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectConfig;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.server.handlers.ProjectHandler;
import com.codenvy.api.project.server.handlers.ProjectHandlerRegistry;
import com.codenvy.api.project.server.type.ProjectType;
import com.codenvy.api.project.server.type.ProjectTypeRegistry;
import com.codenvy.api.vfs.server.ContentStream;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.NameGenerator;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Vitaly Parfonov
 */
@RunWith(MockitoJUnitRunner.class)
public class AddMavenModuleHandlerTest {

    private static final String workspace = "my_ws";

    private static final String POM_XML_TEMPL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><packaging>%s</packaging></project>";

    private AddMavenModuleHandler addMavenModuleHandler;
    private DefaultProjectManager projectManager;
    private ProjectTypeRegistry   projectTypeRegistry;


    @Before
    public void setUp() throws Exception {
        addMavenModuleHandler = new AddMavenModuleHandler();
        ProjectType mavenProjectType = Mockito.mock(ProjectType.class);
        Mockito.when(mavenProjectType.getId()).thenReturn(MavenAttributes.MAVEN_ID);
        Mockito.when(mavenProjectType.getDisplayName()).thenReturn(MavenAttributes.MAVEN_ID);
        Mockito.when(mavenProjectType.canBePrimary()).thenReturn(true);
        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));
        final EventService eventService = new EventService();
        VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
        final MemoryFileSystemProvider memoryFileSystemProvider =
                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
                    @Override
                    public VirtualFileSystemUser getVirtualFileSystemUser() {
                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
                    }
                }, vfsRegistry);
        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);


        Set<ProjectType> projTypes = new HashSet<>();
        projTypes.add(mavenProjectType);

        projectTypeRegistry = new ProjectTypeRegistry(projTypes);

        Set<ProjectHandler> handlers = new HashSet<>();
        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(handlers);

        projectManager = new DefaultProjectManager(vfsRegistry, eventService,
                                                   projectTypeRegistry, handlerRegistry);
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddModuleIfNotPomPackage() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        Project project =
                projectManager.createProject(workspace, parent, new ProjectConfig(null, MavenAttributes.MAVEN_ID), null, "public");
        project.getBaseFolder().createFile("pom.xml", String.format(POM_XML_TEMPL, "jar").getBytes(), "text/xml");
        addMavenModuleHandler
                .onCreateModule(project.getBaseFolder(), NameGenerator.generate("module", 5), new ProjectConfig(null, "maven"),
                                Collections.<String, String>emptyMap());
    }


    @Test(expected = IllegalArgumentException.class)
    public void pomNotFound() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        Project project =  projectManager.createProject(workspace, parent, new ProjectConfig(null, MavenAttributes.MAVEN_ID), null, "public");
        addMavenModuleHandler
                .onCreateModule(project.getBaseFolder(), NameGenerator.generate("module", 5), new ProjectConfig(null, "maven"),
                                Collections.<String, String>emptyMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAddModuleIfModuleNotMaven() throws Exception {
        ProjectType notMaven = Mockito.mock(ProjectType.class);
        Mockito.when(notMaven.getId()).thenReturn("notMaven");
        Mockito.when(notMaven.getDisplayName()).thenReturn("notMaven");
        Mockito.when(notMaven.canBePrimary()).thenReturn(true);
        projectTypeRegistry.registerProjectType(notMaven);

        String parent = NameGenerator.generate("parent", 5);
        Project project =
                projectManager.createProject(workspace, parent, new ProjectConfig(null, MavenAttributes.MAVEN_ID), null, "public");
        project.getBaseFolder().createFile("pom.xml", String.format(POM_XML_TEMPL, "pom").getBytes(), "text/xml");
        addMavenModuleHandler.onCreateModule(project.getBaseFolder(), NameGenerator.generate("module", 5),
                                             new ProjectConfig(null, notMaven.getId()),
                                             Collections.<String, String>emptyMap());
    }

    @Test
    public void addModuleOk() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        Project project =
                projectManager.createProject(workspace, parent, new ProjectConfig(null, MavenAttributes.MAVEN_ID), null, "public");
        project.getBaseFolder().createFile("pom.xml", String.format(POM_XML_TEMPL, "pom").getBytes(), "text/xml");
        addMavenModuleHandler.onCreateModule(project.getBaseFolder(), module,
                                             new ProjectConfig(null, MavenAttributes.MAVEN_ID),
                                             Collections.<String, String>emptyMap());

        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        Assert.assertNotNull(pom);
        ContentStream content = pom.getVirtualFile().getContent();
        Assert.assertNotNull(content);
        InputStream stream = content.getStream();
        String pomContent = IoUtil.readStream(stream);
        Assert.assertNotNull(pomContent);
        Assert.assertFalse(pomContent.isEmpty());
        String mavenModule = String.format("<module>%s</module>", module);
        Assert.assertTrue(pomContent.contains(mavenModule));
    }

}
