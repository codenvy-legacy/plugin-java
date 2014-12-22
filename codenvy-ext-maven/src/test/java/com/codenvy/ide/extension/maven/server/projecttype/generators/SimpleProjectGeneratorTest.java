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
package com.codenvy.ide.extension.maven.server.projecttype.generators;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.*;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.api.project.server.type.ProjectTypeRegistry;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




/** @author Artem Zatsarynnyy */
public class SimpleProjectGeneratorTest {
    private static final String workspace = "my_ws";

    private ProjectManager         pm;
    private MavenProjectGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new MavenProjectGenerator(null, null);
    }

//    @Test
//    public void testGetId() throws Exception {
//        Assert.assertEquals(MavenAttributes.SIMPLE_GENERATOR_ID, generator.getId());
//    }

    @Test
    public void testGetProjectTypeId() throws Exception {
        Assert.assertEquals(MavenAttributes.MAVEN_ID, generator.getProjectTypeId());
    }

    @Test
    public void testGeneratingProject() throws Exception {
        prepareProject();
        final Path pomXml = Paths.get(Thread.currentThread().getContextClassLoader().getResource("test-pom.xml").toURI());

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(MavenAttributes.ARTIFACT_ID, new AttributeValue("my_artifact"));
        attributeValues.put(MavenAttributes.GROUP_ID, new AttributeValue("my_group"));
        attributeValues.put(MavenAttributes.PACKAGING, new AttributeValue("jar"));
        attributeValues.put(MavenAttributes.VERSION, new AttributeValue("1.0-SNAPSHOT"));
        attributeValues.put(MavenAttributes.SOURCE_FOLDER, new AttributeValue("src/main/java"));
        attributeValues.put(MavenAttributes.TEST_SOURCE_FOLDER, new AttributeValue("src/test/java"));
//        GeneratorDescription generatorDescription = DtoFactory.getInstance().createDto(GeneratorDescription.class);
//        NewProject newProjectDescriptor = DtoFactory.getInstance().createDto(NewProject.class)
//                                                    .withType("my_project_type")
//                                                    .withDescription("new project")
//                                                    .withAttributes(attributeValues)
//                                                    .withGeneratorDescription(generatorDescription);

        FolderEntry folder = pm.getProject(workspace, "my_project").getBaseFolder();

        generator.generateProject(folder, attributeValues, null);

        VirtualFileEntry pomFile = pm.getProject(workspace, "my_project").getBaseFolder().getChild("pom.xml");
        Assert.assertTrue(pomFile.isFile());
        Assert.assertEquals(new String(((FileEntry)pomFile).contentAsBytes()), new String(Files.readAllBytes(pomXml)));

        VirtualFileEntry srcFolder = pm.getProject(workspace, "my_project").getBaseFolder().getChild("src/main/java");
        Assert.assertTrue(srcFolder.isFolder());
        VirtualFileEntry testFolder = pm.getProject(workspace, "my_project").getBaseFolder().getChild("src/test/java");
        Assert.assertTrue(testFolder.isFolder());
    }

    private void prepareProject() throws ServerException, ConflictException, ForbiddenException {
        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));

        Set<ProjectType2> pts = new HashSet<>();
        final ProjectType2 pt = new ProjectType2("mytype", "mytype type") {
            {
                //addParent(parent);
                //addConstantDefinition("child_const", "Constant", "const_value");
            }
        };


        pts.add(pt);

        final ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(pts);

        final EventService eventService = new EventService();
        final VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
        final MemoryFileSystemProvider memoryFileSystemProvider =
                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
                    @Override
                    public VirtualFileSystemUser getVirtualFileSystemUser() {
                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
                    }
                }, vfsRegistry);
        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);

        ProjectGeneratorRegistry generatorRegistry = new ProjectGeneratorRegistry(new HashSet<ProjectGenerator>());

        pm = new DefaultProjectManager(vfsRegistry, eventService, projectTypeRegistry, generatorRegistry);
        pm.createProject(workspace, "my_project", new ProjectConfig("", pt.getId()), null);
    }
}