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

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.*;
import com.codenvy.api.project.server.type.Attribute2;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.api.project.server.type.ProjectTypeRegistry;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.ext.java.server.projecttype.JavaProjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.*;

/** @author gazarenkov */
public class MavenProjectTypeTest {
    private static final String workspace = "my_ws";

    private ProjectManager        pm;

    @Before
    public void setUp() throws Exception {

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


        Set<ProjectType2> projTypes = new HashSet<>();
        projTypes.add(new JavaProjectType ());
        projTypes.add(new MavenProjectType(new MavenValueProviderFactory(),
                new JavaProjectType ()));

        ProjectTypeRegistry ptRegistry = new ProjectTypeRegistry(projTypes);

        ProjectGeneratorRegistry generatorRegistry = new ProjectGeneratorRegistry(new HashSet<ProjectGenerator>());

        pm = new DefaultProjectManager(vfsRegistry, eventService,
                ptRegistry, generatorRegistry);

    }

    @Test
    public void testGetProjectType() throws Exception {

        ProjectType2 pt = pm.getProjectTypeRegistry().getProjectType("maven");

        Assert.assertNotNull(pt);
        Assert.assertEquals(pt.getDefaultBuilder(), "maven");
        Assert.assertTrue(pt.getAttributes().size() > 0);
        Assert.assertTrue(pt.isTypeOf("java"));

    }

    @Test
    public void testMavenProject() throws Exception {

        // TODO

//        Project project = pm.createProject(workspace, "myProject", new ProjectConfig("my config", "maven"), null);
//
//        ProjectConfig config = project.getConfig();
//
//        System.out.println(">>>>>" + config.getBuilders()+" "+config.getRunners()+" "+config.getAttributes());
//
//
//
//        for(String name:config.getAttributes().keySet())
//            System.out.println(">>a>>> " + config.getAttributes().get(name));
//



    }



}