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
package org.eclipse.che.gradle.server;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.project.server.*;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.gradle.GradleUtils;
import org.eclipse.che.gradle.analyzer.ModelRequest;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.gradle.server.project.GradleProjectResolver;
import org.eclipse.che.gradle.server.project.analyzer.GradleProjectAnalyzerCallback;
import org.eclipse.che.gradle.server.project.analyzer.GradleProjectAnalyzerClientService;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.everrest.websockets.message.MessageConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.eclipse.che.gradle.shared.GradleAttributes.WEBSOCKET_ANALYZER_CHANNEL;

/** @author Vladyslav Zhukovskii */
@Api(value = "/gradle",
        description = "Gradle manager")
@Path("gradle/{ws-id}")
public class GradleProjectService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(GradleProjectService.class);

    private ProjectManager                     projectManager;
    private GradleProjectResolver              projectResolver;
    private GradleProjectAnalyzerClientService analyzerService;

    @Inject
    public GradleProjectService(ProjectManager projectManager,
                                GradleProjectResolver projectResolver,
                                GradleProjectAnalyzerClientService analyzerService) {
        this.projectManager = projectManager;
        this.projectResolver = projectResolver;
        this.analyzerService = analyzerService;
    }

    @ApiOperation(value = "Project model",
            notes = "Project model. Fetch stored model of Gradle project. Contains various information, e.g. tasks, plugins, " +
                    "paths, description.",
            response = GrdConfiguration.class,
            position = 1)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Forbidden operation"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @Path("model")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GrdConfiguration getProjectModel(@ApiParam(value = "Workspace ID", required = true)
                                            @PathParam("ws-id") String workspace,
                                            @ApiParam(value = "Project path", required = true)
                                            @QueryParam("project") String projectPath) throws ServerException, ForbiddenException {
        Project project = projectManager.getProject(workspace, projectPath);

        //project may be sub module, any way, our gradle configuration stores in the root project, that's why, at first we should get
        //root project and read configuration from there
        return GradleUtils.getGrdProjectConfiguration(project.getBaseFolder());
    }

    @ApiOperation(value = "Perform analysis of Gradle project",
            notes = "Perform analysis of Gradle project. It is necessary for understanding model of user's project. " +
                    "With this model Codenvy may allow user to work with code assistant or to perform correct building project. " +
                    "Asynchronous operation, so mechanism which called this method directly should subscribe to websocket " +
                    "channel 'gradle:analyzer:{ws-name}:{project-path}'.",
            position = 2)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 403, message = "Forbidden operation"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @Path("analyze")
    @POST
    @RolesAllowed("user")
    public void performProjectAnalysis(@ApiParam(value = "Workspace ID", required = true)
                                       @PathParam("ws-id") final String workspace,
                                       @ApiParam(value = "Project path", required = true)
                                       @QueryParam("project") final String projectPath) throws ServerException, ForbiddenException {
        final Project project = projectManager.getProject(workspace, projectPath);
        analyzerService.getAsyncProjectModel(project.getBaseFolder(), ModelRequest.PROJECT, new GradleProjectAnalyzerCallback() {
            @Override
            public void onSuccess(GrdConfiguration grdConfiguration) {
                ChannelBroadcastMessage message = new ChannelBroadcastMessage();
                message.setChannel(String.format(WEBSOCKET_ANALYZER_CHANNEL, project.getWorkspace(), project.getPath()));
                message.setBody(DtoFactory.getInstance().toJson(grdConfiguration));


                try {
                    GradleUtils.writeGrdConfiguration(project.getBaseFolder(), grdConfiguration);
                } catch (ServerException e) {
                    message.setType(ChannelBroadcastMessage.Type.ERROR);
                    message.setBody(e.getMessage());
                    LOG.error(e.getMessage(), e);
                }

                try {
                    WSConnectionContext.sendMessage(message);
                } catch (MessageConversionException | IOException e) {
                    LOG.error("A problem occurred while sending websocket message", e);
                }
            }

            @Override
            public void onFailed(String buildLog) {
                ChannelBroadcastMessage message = new ChannelBroadcastMessage();
                message.setChannel(String.format(WEBSOCKET_ANALYZER_CHANNEL, project.getWorkspace(), project.getPath()));
                message.setType(ChannelBroadcastMessage.Type.ERROR);
                message.setBody(buildLog);

                try {
                    WSConnectionContext.sendMessage(message);
                } catch (MessageConversionException | IOException e) {
                    LOG.error("A problem occurred while sending websocket message", e);
                }
            }
        });
    }

    @ApiOperation(value = "Synchronize project",
            notes = "Synchronize user project with configuration fetched by project analysis. It necessary for correct project" +
                    "structure building.",
            position = 3)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 403, message = "Forbidden operation"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @Path("synchronize")
    @POST
    @RolesAllowed("user")
    public void synchronizeProject(@ApiParam(value = "Workspace ID", required = true)
                                   @PathParam("ws-id") String workspace,
                                   @ApiParam(value = "Project path", required = true)
                                   @QueryParam("project") String projectPath)
            throws ServerException, ForbiddenException, ProjectTypeConstraintException, ValueStorageException {
        Project project = projectManager.getProject(workspace, projectPath);
        FolderEntry rootProjectFolder = GradleUtils.getRootProjectFolder(project.getBaseFolder());

        projectResolver.resolve(rootProjectFolder);
    }
}
