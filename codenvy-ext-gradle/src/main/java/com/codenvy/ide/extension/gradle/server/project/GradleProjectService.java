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
package com.codenvy.ide.extension.gradle.server.project;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.Service;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.extension.gradle.shared.dto.Task;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** @author Vladyslav Zhukovskii */
@Path("gradle/{ws-id}")
@Singleton
public class GradleProjectService extends Service {

    @Inject
    private ProjectManager projectManager;

    @Path("tasks")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Task> getProjectTasks(@QueryParam("projectpath") String projectPath,
                                    @PathParam("ws-id") String workspace) throws ServerException, ForbiddenException {
        Project project = projectManager.getProject(workspace, projectPath);
        ProjectConnection gradleProjectConnection = getGradleProjectConnection(project);

        List<Task> tasks = new ArrayList<>();

        try {
            GradleProject gradleProject = gradleProjectConnection.getModel(GradleProject.class);
            for (GradleTask task : gradleProject.getTasks()) {
                Task dtoTask = DtoFactory.getInstance().createDto(Task.class)
                                         .withName(task.getName())
                                         .withDescription(task.getDescription());
                tasks.add(dtoTask);
            }
        } finally {
            gradleProjectConnection.close();
        }

        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        return tasks;
    }

    private ProjectConnection getGradleProjectConnection(Project project) {
        VirtualFile virtualProjectFolder = project.getBaseFolder().getVirtualFile();

        return GradleConnector.newConnector()
                              .forProjectDirectory(((VirtualFileImpl)virtualProjectFolder).getIoFile())
                              .connect();
    }
}
