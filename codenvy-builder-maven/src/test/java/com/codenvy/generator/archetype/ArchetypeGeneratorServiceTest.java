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
package com.codenvy.generator.archetype;

import com.codenvy.generator.archetype.dto.GenerateTask;

import org.everrest.core.impl.uri.UriBuilderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.generator.archetype.dto.GenerateTask.Status.FAILED;
import static com.codenvy.generator.archetype.dto.GenerateTask.Status.IN_PROGRESS;
import static com.codenvy.generator.archetype.dto.GenerateTask.Status.SUCCESSFUL;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public class ArchetypeGeneratorServiceTest {
    private final static long taskId = 1;
    @Mock
    private ArchetypeGenerator archetypeGenerator;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private ArchetypeGenerator.GenerateTask taskMock;

    @InjectMocks
    private ArchetypeGeneratorService service;

    @Before
    public void setUp() throws Exception {
        doReturn(new UriBuilderImpl().uri(URI.create("http://localhost:8080"))).when(uriInfo).getBaseUriBuilder();
        when(taskMock.getId()).thenReturn(taskId);
    }

    @Test
    public void testGenerate() throws Exception {
        MavenArchetype archetype = new MavenArchetype("archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null);

        when(archetypeGenerator.generateFromArchetype((MavenArchetype)anyObject(), anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(taskMock);

        Map<String, String> options = new HashMap<>();
        GenerateTask task = service.generate(uriInfo, archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion(),
                                             "groupId", "artifactId", "version", options);

        verify(archetypeGenerator).generateFromArchetype(eq(archetype), eq("groupId"), eq("artifactId"),
                                                         eq("version"), anyMap());
        Assert.assertEquals("http://localhost:8080/maven-generator-archetype/status/" + taskId, task.getStatusUrl());
    }

    @Test
    public void testGetStatusWhenTaskIsNotDone() throws Exception {
        doReturn(false).when(taskMock).isDone();
        doReturn(taskMock).when(archetypeGenerator).getTaskById(anyLong());

        GenerateTask task = service.getStatus(uriInfo, String.valueOf(taskId));

        Assert.assertEquals(IN_PROGRESS, task.getStatus());
    }

    @Test
    public void testGetStatusWhenTaskIsSuccessful() throws Exception {
        doReturn(true).when(taskMock).isDone();

        GenerateResult generateResult = mock(GenerateResult.class);
        doReturn(true).when(generateResult).isSuccessful();

        doReturn(generateResult).when(taskMock).getResult();
        doReturn(taskMock).when(archetypeGenerator).getTaskById(anyLong());

        GenerateTask task = service.getStatus(uriInfo, String.valueOf(taskId));

        Assert.assertEquals(SUCCESSFUL, task.getStatus());
        Assert.assertEquals("http://localhost:8080/maven-generator-archetype/project/1", task.getDownloadUrl());
    }

    @Test
    public void testGetStatusWhenTaskIsFailed() throws Exception {
        doReturn(true).when(taskMock).isDone();

        GenerateResult generateResult = mock(GenerateResult.class);
        doReturn(false).when(generateResult).isSuccessful();

        doReturn(generateResult).when(taskMock).getResult();
        doReturn(taskMock).when(archetypeGenerator).getTaskById(anyLong());

        GenerateTask task = service.getStatus(uriInfo, String.valueOf(taskId));

        Assert.assertEquals(FAILED, task.getStatus());
    }

    @Test(expected = GeneratorException.class)
    public void testGetStatusWithInvalidTaskId() throws Exception {
        doThrow(GeneratorException.class).when(archetypeGenerator).getTaskById(anyLong());

        service.getStatus(uriInfo, String.valueOf(taskId));
    }
}
