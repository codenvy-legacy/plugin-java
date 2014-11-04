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
package com.codenvy.ide.extension.maven.server;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.MavenUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;

/**
 * @author Evgen Vidolob
 */
@Path("maven/pom/{ws-id}")
public class MavenPomService {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @PathParam("ws-id")
    @Inject
    private String wsId;

    @Inject
    private ProjectManager projectManager;

    @Path("read")
    @GET
    @Produces("application/json")
    public String readPomAttributes(@QueryParam("projectpath") String projectPath) throws Exception {
        Project project = projectManager.getProject(wsId, projectPath);
        VirtualFile projectFolder = project.getBaseFolder().getVirtualFile();

        VirtualFile pomFile = projectFolder.getChild("pom.xml");
        if (pomFile != null) {
            Model model = MavenUtils.readModel(pomFile);
            JsonObject object = new JsonObject();
            Parent parent = model.getParent();
            object.addProperty(MavenAttributes.ARTIFACT_ID, model.getArtifactId());
            if (model.getGroupId() == null) {
                if (parent != null) {
                    object.addProperty(MavenAttributes.GROUP_ID, parent.getGroupId());
                }
            } else {
                object.addProperty(MavenAttributes.GROUP_ID, model.getGroupId());
            }

            if (model.getVersion() == null) {
                if (parent != null) {
                    object.addProperty(MavenAttributes.VERSION, parent.getVersion());
                }
            } else {
                object.addProperty(MavenAttributes.VERSION, model.getVersion());
            }
            object.addProperty(MavenAttributes.PACKAGING, model.getPackaging());
            return gson.toJson(object);
        } else {
            throw new IllegalArgumentException("There is no pom.xml file in project: " + projectPath);
        }
    }

    @POST
    @Path("add-module")
    public void addModule(@QueryParam("projectpath") String projectPath, @QueryParam("module") String moduleName)
            throws ServerException, ForbiddenException, IOException {
        Project project = projectManager.getProject(wsId, projectPath);
        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        if(pom == null) {
            throw new IllegalArgumentException("Can't find pom.xml file in path: " + projectPath);
        }

        Model model = MavenUtils.readModel(pom.getVirtualFile());
        if("pom".equals(model.getPackaging())){
            MavenUtils.addModule(pom.getVirtualFile(), moduleName);
        } else {
            throw new IllegalArgumentException("Project must have packaging 'pom' in order to adding modules.");
        }
    }
}
