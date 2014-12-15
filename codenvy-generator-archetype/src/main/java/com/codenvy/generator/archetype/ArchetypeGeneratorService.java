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

import com.codenvy.api.core.util.ContentTypeGuesser;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.generator.archetype.dto.GenerateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static com.codenvy.generator.archetype.dto.GenerateTask.Status.FAILED;
import static com.codenvy.generator.archetype.dto.GenerateTask.Status.IN_PROGRESS;
import static com.codenvy.generator.archetype.dto.GenerateTask.Status.SUCCESSFUL;

/**
 * Provides access to {@link ArchetypeGenerator} through HTTP.
 *
 * @author Artem Zatsarynnyy
 */
@Path("maven-generator-archetype")
public class ArchetypeGeneratorService {
    private static final Logger LOG = LoggerFactory.getLogger(ArchetypeGeneratorService.class);
    @Inject
    private ArchetypeGenerator archetypeGenerator;

    @Path("generate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GenerateTask generate(@Context UriInfo uriInfo,
                                 @QueryParam("archetypeGroupId") String archetypeGroupId,
                                 @QueryParam("archetypeArtifactId") String archetypeArtifactId,
                                 @QueryParam("archetypeVersion") String archetypeVersion,
                                 @QueryParam("groupId") String groupId,
                                 @QueryParam("artifactId") String artifactId,
                                 @QueryParam("version") String version,
                                 Map<String, String> options) throws GeneratorException {
        final String archetypeRepository = options.remove("archetypeRepository");
        final MavenArchetype archetype = new MavenArchetype(archetypeGroupId, archetypeArtifactId, archetypeVersion, archetypeRepository);
        ArchetypeGenerator.GenerateTask task = archetypeGenerator.generateFromArchetype(archetype, groupId, artifactId, version, options);
        final GenerateTask generateTask = DtoFactory.getInstance().createDto(GenerateTask.class);
        generateTask
                .setStatusUrl(uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "getStatus").build(task.getId()).toString());
        return generateTask;
    }

    @GET
    @Path("status/{taskid}")
    @Produces(MediaType.APPLICATION_JSON)
    public GenerateTask getStatus(@Context UriInfo uriInfo, @PathParam("taskid") String taskId) throws GeneratorException {
        ArchetypeGenerator.GenerateTask task = archetypeGenerator.getTaskById(Long.valueOf(taskId));
        final GenerateTask status = DtoFactory.getInstance().createDto(GenerateTask.class);
        if (!task.isDone()) {
            status.setStatus(IN_PROGRESS);
        } else if (task.getResult().isSuccessful()) {
            status.setStatus(SUCCESSFUL);
            status.setDownloadUrl(
                    uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "getProjectFile").build(task.getId()).toString());
        } else {
            status.setStatus(FAILED);
            if (task.getResult().hasGenerateReport()) {
                try {
                    status.setReport(new String(Files.readAllBytes(task.getResult().getGenerateReport().toPath())));
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return status;
    }

    @GET
    @Path("project/{taskid}")
    public Response getProjectFile(@PathParam("taskid") String taskId) throws GeneratorException {
        ArchetypeGenerator.GenerateTask task = archetypeGenerator.getTaskById(Long.valueOf(taskId));
        final File projectZip = task.getResult().getResult();
        return Response.status(200)
                       .header("Content-Disposition", String.format("attachment; filename=\"%s\"", projectZip.getName()))
                       .type(ContentTypeGuesser.guessContentType(projectZip))
                       .entity(projectZip)
                       .build();
    }
}
