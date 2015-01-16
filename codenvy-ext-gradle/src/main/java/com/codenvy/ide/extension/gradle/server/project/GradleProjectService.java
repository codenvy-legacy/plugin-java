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
package com.codenvy.ide.extension.gradle.server.project;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.Service;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.gradle.tools.GradleUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/** @author Vladyslav Zhukovskii */
@Path("gradle/{ws-id}")
@Singleton
public class GradleProjectService extends Service {

    @Inject
    private ProjectManager projectManager;

    @Path("tasks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectTasks(@QueryParam("projectpath") String projectPath,
                                    @PathParam("ws-id") String workspace) throws ServerException, ForbiddenException {
        Project project = projectManager.getProject(workspace, projectPath);
        VirtualFile projectFolder = project.getBaseFolder().getVirtualFile();

        Map<String, String> tasks = GradleUtils.getTasks(projectFolder);

        String json = DtoFactory.getInstance().toJson(tasks);

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
