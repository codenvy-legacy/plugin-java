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
package com.codenvy.ide.extension.ant.server.project.type;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.project.server.AttributeDescription;
import com.codenvy.api.project.server.DefaultProjectManager;
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectDescription;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeDescriptionExtension;
import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.dto.server.DtoFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codenvy.ide.extension.ant.shared.AntAttributes.ANT_GENERATOR_ID;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.ANT_ID;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.TEST_SOURCE_FOLDER;

/** @author Artem Zatsarynnyy */
public class AntProjectGeneratorTest {
    private static final String workspace = "my_ws";

    private ProjectManager      pm;
    private AntProjectGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new AntProjectGenerator();
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(ANT_GENERATOR_ID, generator.getId());
    }

    @Test
    public void testGetProjectTypeId() throws Exception {
        Assert.assertEquals(ANT_ID, generator.getProjectTypeId());
    }

    @Test
    public void testGeneratingProject() throws Exception {
        prepareProject();
        final Path buildXml = Paths.get(Thread.currentThread().getContextClassLoader().getResource("test-build.xml").toURI());

        Map<String, List<String>> attributeValues = new HashMap<>();
        attributeValues.put(SOURCE_FOLDER, Arrays.asList("src"));
        attributeValues.put(TEST_SOURCE_FOLDER, Arrays.asList("test"));
        GeneratorDescription generatorDescription = DtoFactory.getInstance().createDto(GeneratorDescription.class).withName("my_generator");
        NewProject newProjectDescriptor = DtoFactory.getInstance().createDto(NewProject.class)
                                                    .withType("my_project_type")
                                                    .withDescription("new project")
                                                    .withAttributes(attributeValues)
                                                    .withGeneratorDescription(generatorDescription);

        FolderEntry folder = pm.getProject(workspace, "my_project").getBaseFolder();
        generator.generateProject(folder, newProjectDescriptor);

        VirtualFileEntry buildFile = pm.getProject(workspace, "my_project").getBaseFolder().getChild("build.xml");
        Assert.assertTrue(buildFile.isFile());
        Assert.assertEquals(new String(((FileEntry)buildFile).contentAsBytes()), new String(Files.readAllBytes(buildXml)));

        VirtualFileEntry srcFolder = pm.getProject(workspace, "my_project").getBaseFolder().getChild("src");
        Assert.assertTrue(srcFolder.isFolder());
        VirtualFileEntry testFolder = pm.getProject(workspace, "my_project").getBaseFolder().getChild("test");
        Assert.assertTrue(testFolder.isFolder());
    }

    private void prepareProject() throws ServerException, ConflictException, ForbiddenException {
        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));

        final ProjectTypeDescriptionRegistry ptdr = new ProjectTypeDescriptionRegistry("test");
        final ProjectType projectType = new ProjectType("my_project_type", "my project type", "my_category");
        ptdr.registerDescription(new ProjectTypeDescriptionExtension() {
            @Nonnull
            @Override
            public List<ProjectType> getProjectTypes() {
                return Arrays.asList(projectType);
            }

            @Nonnull
            @Override
            public List<AttributeDescription> getAttributeDescriptions() {
                return Collections.emptyList();
            }
        });
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
        pm = new DefaultProjectManager(ptdr, Collections.<ValueProviderFactory>emptySet(), vfsRegistry, eventService);
        pm.createProject(workspace, "my_project", new ProjectDescription(projectType));
    }
}