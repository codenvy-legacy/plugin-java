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
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.shared.ProjectDescription;
import com.codenvy.api.project.shared.ProjectType;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.maven.tools.MavenUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.maven.model.Model;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenProjectTypeResolver implements ProjectTypeResolver {
    @Inject
    private ProjectManager projectManager;

    @Override
    public boolean resolve(Project project) throws ServerException {
        try {
            ProjectType projectType = projectManager.getTypeDescriptionRegistry().getProjectType(Constants.MAVEN_ID);
            if (projectType == null) {
                //thea are no maven project type registered
                return false;
            }
            if (project.getBaseFolder().getChild("pom.xml") == null) {
                return false;
            }
            ProjectDescription description = project.getDescription();
            fillMavenProject(projectType, project.getBaseFolder(), project, description);
            project.updateDescription(description);
            return true;
        } catch (ForbiddenException | IOException | ConflictException e) {
            throw new ServerException("An error occurred when trying to resolve maven project.", e);
        }
    }

    private void createProjectsOnModules(Model model, FolderEntry baseFolder, String ws, ProjectType projectType)
            throws ServerException, ForbiddenException, ConflictException, IOException {
        List<String> modules = model.getModules();
        for (String module : modules) {
            VirtualFileEntry moduleEntry = baseFolder.getChild(module);
            if (moduleEntry != null && moduleEntry.getVirtualFile().getChild("pom.xml") != null) {
                Project project = projectManager.getProject(ws, baseFolder.getPath() + "/" + module);
                if (project == null) {
                    project = new Project(ws, (FolderEntry)moduleEntry, projectManager);
                }
                ProjectDescription description = project.getDescription();
                fillMavenProject(projectType, moduleEntry, project, description);
                project.updateDescription(description);
            }
        }
    }

    private void fillMavenProject(ProjectType projectType, VirtualFileEntry moduleEntry, Project project,
                                  ProjectDescription description)
            throws IOException, ForbiddenException, ServerException, ConflictException {
        description.setProjectType(projectType);
        description.setBuilder("maven");
        Model model = MavenUtils.readModel(moduleEntry.getVirtualFile().getChild("pom.xml"));
        String packaging = model.getPackaging();
        switch (packaging) {
            case "pom":
                createProjectsOnModules(model, project.getBaseFolder(), project.getWorkspace(), projectType);
                break;
            case "war":
                description.setRunner("java-webapp-default");
                break;
            case "jar":
                description.setRunner("java-standalone-default");
                break;
        }
    }
}
