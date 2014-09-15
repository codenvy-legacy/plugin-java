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
import com.codenvy.api.project.shared.dto.ProjectUpdate;
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
    public boolean resolve(Project project, ProjectUpdate projectUpdate) throws ServerException {
        try {
            ProjectType projectType = projectManager.getTypeDescriptionRegistry().getProjectType(Constants.MAVEN_ID);
            if (projectType == null) {
                //thea are no maven project type registered
                return false;
            }
            if (project.getBaseFolder().getChild("pom.xml") == null) {
                return false;
            }

            projectUpdate.setProjectTypeId(Constants.MAVEN_ID);
            projectUpdate.setBuilder("maven");
            selectPackagingAndRunner(projectType, project.getBaseFolder(), project, null, projectUpdate);
        } catch (ForbiddenException | IOException | ConflictException e) {
            throw new ServerException("An error occurred when trying to resolve maven project.", e);
        }

        return true;
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
                description.setProjectType(projectType);
                description.setBuilder("maven");
                selectPackagingAndRunner(projectType, moduleEntry, project, description, null);
                project.updateDescription(description);
            }
        }
    }

    private void selectPackagingAndRunner(ProjectType projectType, VirtualFileEntry moduleEntry, Project project,
                                          ProjectDescription description, ProjectUpdate projectUpdate)
            throws IOException, ForbiddenException, ServerException, ConflictException {
        Model model = MavenUtils.readModel(moduleEntry.getVirtualFile().getChild("pom.xml"));
        String packaging = model.getPackaging();
        switch (packaging) {
            case "pom":
                createProjectsOnModules(model, project.getBaseFolder(), project.getWorkspace(), projectType);
                break;
            case "war":
                if (description == null)
                    projectUpdate.setRunner("java-webapp-default");
                else
                    description.setRunner("java-webapp-default");
                break;
            case "jar":
                if (description == null)
                    projectUpdate.setRunner("java-standalone-default");
                else
                    description.setRunner("java-standalone-default");
                break;
        }
    }
}
