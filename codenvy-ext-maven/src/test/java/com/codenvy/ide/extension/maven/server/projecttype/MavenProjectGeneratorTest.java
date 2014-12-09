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
import com.codenvy.api.project.server.AttributeDescription;
import com.codenvy.api.project.server.DefaultProjectManager;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectDescription;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeDescriptionExtension;
import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.api.vfs.server.VirtualFileSystemUser;
import com.codenvy.api.vfs.server.VirtualFileSystemUserContext;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.extension.maven.server.archetypegenerator.ArchetypeGenerator;
import com.codenvy.ide.extension.maven.server.archetypegenerator.GenerateResult;
import com.codenvy.ide.extension.maven.server.archetypegenerator.MavenArchetype;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_GENERATOR_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public class MavenProjectGeneratorTest {

    private static final String                    workspace   = "my_ws";
    private final        VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
    private ProjectManager        pm;
    private MavenProjectGenerator generator;
    @Spy
    private ArchetypeGenerator archetypeGenerator = new ArchetypeGenerator();
    @Mock
    private GenerateResult generateResult;

    @Before
    public void setUp() throws Exception {
        doReturn(generateResult).when(archetypeGenerator).generateFromArchetype((MavenArchetype)anyObject(),
                                                                                anyString(), anyString(), anyString(), anyMap());
        generator = new MavenProjectGenerator(archetypeGenerator, vfsRegistry);
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(MAVEN_GENERATOR_ID, generator.getId());
    }

    @Test
    public void testGetProjectTypeId() throws Exception {
        Assert.assertEquals(MAVEN_ID, generator.getProjectTypeId());
    }

    @Test(expected = ServerException.class)
    public void testWhenGeneratingIsFailed() throws Exception {
        when(generateResult.isSuccessful()).thenReturn(false);

        Map<String, List<String>> attributeValues = new HashMap<>();
        attributeValues.put(ARTIFACT_ID, Arrays.asList("my_artifact"));
        attributeValues.put(GROUP_ID, Arrays.asList("my_group"));
        attributeValues.put(PACKAGING, Arrays.asList("jar"));
        attributeValues.put(VERSION, Arrays.asList("1.0-SNAPSHOT"));
        attributeValues.put(SOURCE_FOLDER, Arrays.asList("src/main/java"));
        attributeValues.put(TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        GeneratorDescription generatorDescription = DtoFactory.getInstance().createDto(GeneratorDescription.class).withName("my_generator");
        NewProject newProjectDescriptor = DtoFactory.getInstance().createDto(NewProject.class)
                                                    .withType("my_project_type")
                                                    .withDescription("new project")
                                                    .withAttributes(attributeValues)
                                                    .withGeneratorDescription(generatorDescription);

        generator.generateProject(null, newProjectDescriptor);

        verify(archetypeGenerator).generateFromArchetype((MavenArchetype)anyObject(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void testGenerating() throws Exception {
        prepareProject();

        when(generateResult.isSuccessful()).thenReturn(true);
        final URI testZipUri = Thread.currentThread().getContextClassLoader().getResource("test.zip").toURI();
        when(generateResult.getResult()).thenReturn(new File(testZipUri));

        Map<String, List<String>> attributeValues = new HashMap<>();
        attributeValues.put(ARTIFACT_ID, Arrays.asList("my_artifact"));
        attributeValues.put(GROUP_ID, Arrays.asList("my_group"));
        attributeValues.put(PACKAGING, Arrays.asList("jar"));
        attributeValues.put(VERSION, Arrays.asList("1.0-SNAPSHOT"));
        attributeValues.put(SOURCE_FOLDER, Arrays.asList("src/main/java"));
        attributeValues.put(TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        GeneratorDescription generatorDescription = DtoFactory.getInstance().createDto(GeneratorDescription.class).withName("my_generator");
        NewProject newProjectDescriptor = DtoFactory.getInstance().createDto(NewProject.class)
                                                    .withType("my_project_type")
                                                    .withDescription("new project")
                                                    .withAttributes(attributeValues)
                                                    .withGeneratorDescription(generatorDescription);
        FolderEntry folder = pm.getProject(workspace, "my_project").getBaseFolder();

        generator.generateProject(folder, newProjectDescriptor);

        verify(archetypeGenerator).generateFromArchetype((MavenArchetype)anyObject(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    public void shouldNotGenerateWhenRequiredAttributeMissed() throws Exception {
        when(generateResult.isSuccessful()).thenReturn(false);

        Map<String, List<String>> attributeValues = new HashMap<>();
//        attributeValues.put(ARTIFACT_ID, Arrays.asList("my_artifact"));
        attributeValues.put(GROUP_ID, Arrays.asList("my_group"));
        attributeValues.put(PACKAGING, Arrays.asList("jar"));
        attributeValues.put(VERSION, Arrays.asList("1.0-SNAPSHOT"));
        attributeValues.put(SOURCE_FOLDER, Arrays.asList("src/main/java"));
        attributeValues.put(TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        GeneratorDescription generatorDescription = DtoFactory.getInstance().createDto(GeneratorDescription.class).withName("my_generator");
        NewProject newProjectDescriptor = DtoFactory.getInstance().createDto(NewProject.class)
                                                    .withType("my_project_type")
                                                    .withDescription("new project")
                                                    .withAttributes(attributeValues)
                                                    .withGeneratorDescription(generatorDescription);

        generator.generateProject(null, newProjectDescriptor);

        verify(archetypeGenerator, never())
                .generateFromArchetype((MavenArchetype)anyObject(), anyString(), anyString(), anyString(), anyMap());
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