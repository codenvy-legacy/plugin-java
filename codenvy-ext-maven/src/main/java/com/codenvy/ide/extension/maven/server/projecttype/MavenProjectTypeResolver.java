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
import com.codenvy.api.project.server.ProjectConfig;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.shared.Builders;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.Model;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenProjectTypeResolver implements ProjectTypeResolver {

    @Inject
    private ProjectManager projectManager;

    private ProjectConfig createProjectDescriptor(ProjectType2 projectType) {
        Builders builders = new Builders();
        builders.setDefault("maven");
        return new ProjectConfig("Maven", projectType.getId(), null, null, builders, null);
    }

    @Override
    public boolean resolve(FolderEntry folderEntry) throws ServerException {
        try {
            if (!folderEntry.isProjectFolder()) {
                ProjectType2 projectType = projectManager.getProjectTypeRegistry().getProjectType(MavenAttributes.MAVEN_ID);
                if (projectType == null)
                    throw new ServerException(String.format("Project type '%s' not registered. ", MavenAttributes.MAVEN_ID));
                if (folderEntry.getChild("pom.xml") == null) {
                    return false;
                }
                Project project = new Project(folderEntry, projectManager);
                project.updateConfig(createProjectDescriptor(projectType));
                fillMavenProject(projectType, project);
                return true;
            }
            return false;//project configure in initial source
        } catch (ForbiddenException | IOException | ConflictException e) {
            throw new ServerException("An error occurred when trying to resolve maven project.", e);
        }
    }

    private void createProjectsOnModules(Model model, Project parentProject, String ws, ProjectType2 projectType)
            throws ServerException, ForbiddenException, ConflictException, IOException {
        List<String> modules = model.getModules();
        for (String module : modules) {
            FolderEntry parentFolder = getParentFolder(module, parentProject);
            module = module.replaceAll("\\.{2}/", "");
            FolderEntry moduleEntry = (FolderEntry)parentFolder.getChild(module);
            if (moduleEntry != null && !moduleEntry.isProjectFolder() && moduleEntry.getVirtualFile().getChild("pom.xml") != null) {
                Project project = projectManager.getProject(ws, parentFolder.getPath() + "/" + module);
                if (project == null) {
                    project = new Project(moduleEntry, projectManager);
                    project.getMisc().setCreationDate(System.currentTimeMillis());
                }
                fillMavenProject(projectType, project);
                project.updateConfig(createProjectDescriptor(projectType));
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

    private void fillMavenProject(ProjectType2 projectType, Project project)
            throws IOException, ForbiddenException, ServerException, ConflictException {
        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        if (pom != null) {
            Model model = Model.readFrom(pom.getVirtualFile());
            String packaging = model.getPackaging();
            if (packaging.equals("pom")) {
                createProjectsOnModules(model, project, project.getWorkspace(), projectType);
            }
        }
    }
}
