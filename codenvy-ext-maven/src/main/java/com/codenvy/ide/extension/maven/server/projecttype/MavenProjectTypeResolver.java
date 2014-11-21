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
import com.codenvy.api.project.server.Builders;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectDescription;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.VirtualFileEntry;
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

    private ProjectDescription createProjectDescriptor(ProjectType projectType) {
        Builders builders = new Builders();
        builders.setDefault("maven");
        return new ProjectDescription(projectType, builders, null);
    }

    @Override
    public boolean resolve(FolderEntry folderEntry) throws ServerException {
        try {
            ProjectType projectType = projectManager.getTypeDescriptionRegistry().getProjectType(Constants.MAVEN_ID);
            if (projectType == null)
                throw new ServerException(String.format("Project type '%s' not registered. ", Constants.MAVEN_ID));
            if (folderEntry.getChild("pom.xml") == null) {
                return false;
            }
            Project project = new Project(folderEntry, projectManager);
            project.updateDescription(createProjectDescriptor(projectType));
            fillMavenProject(projectType, project);
            return true;
        } catch (ForbiddenException | IOException | ConflictException e) {
            throw new ServerException("An error occurred when trying to resolve maven project.", e);
        }
    }

    private void createProjectsOnModules(Model model, Project parentProject, String ws, ProjectType projectType)
            throws ServerException, ForbiddenException, ConflictException, IOException {
        List<String> modules = model.getModules();
        for (String module : modules) {
            FolderEntry parentFolder = getParentFolder(module, parentProject);
            module = module.replaceAll("\\.{2}/", "");
            FolderEntry moduleEntry = (FolderEntry)parentFolder.getChild(module);
            if (moduleEntry != null && moduleEntry.getVirtualFile().getChild("pom.xml") != null) {
                Project project = projectManager.getProject(ws, parentFolder.getPath() + "/" + module);
                if (project == null) {
                    project = new Project(moduleEntry, projectManager);
                    project.getMisc().setCreationDate(System.currentTimeMillis());
                }
                fillMavenProject(projectType, project);
                project.updateDescription(createProjectDescriptor(projectType));
            }
        }
    }

    private FolderEntry getParentFolder(String module, Project parentProject) {
        FolderEntry parentFolder = parentProject.getBaseFolder();
        int level = module.split("\\.{2}/").length - 1;
        while (level != 0 && parentFolder != null) {
            parentFolder = parentFolder.getParent();
            level--;
        }
        return parentFolder;
    }

    private void fillMavenProject(ProjectType projectType, Project project)
            throws IOException, ForbiddenException, ServerException, ConflictException {
        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        if (pom != null) {
            Model model = MavenUtils.readModel(pom.getVirtualFile());
            String packaging = model.getPackaging();
            if (packaging.equals("pom")) {
                createProjectsOnModules(model, project, project.getWorkspace(), projectType);
            }
        }
    }
}
