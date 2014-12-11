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

import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.dto.server.DtoFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.QUICKSTART_GENERATOR_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public class QuickstartProjectGeneratorTest {

    private final VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
    private QuickstartProjectGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new QuickstartProjectGenerator("http://localhost/", vfsRegistry);
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(QUICKSTART_GENERATOR_ID, generator.getId());
    }

    @Test
    public void testGetProjectTypeId() throws Exception {
        Assert.assertEquals(MAVEN_ID, generator.getProjectTypeId());
    }

    @Test(expected = ServerException.class)
    public void shouldNotGenerateWhenRequiredAttributeMissed() throws Exception {
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
    }
}