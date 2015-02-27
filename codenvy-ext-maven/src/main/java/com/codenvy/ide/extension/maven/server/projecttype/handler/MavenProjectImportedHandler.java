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
package com.codenvy.ide.extension.maven.server.projecttype.handler;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectConfig;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.server.handlers.PostImportProjectHandler;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.api.project.shared.Builders;
import com.codenvy.ide.maven.tools.Model;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * @author Vitaly Parfonov
 */
@Singleton
public class MavenProjectImportedHandler implements PostImportProjectHandler {


    @Inject
    private ProjectManager projectManager;

    @Override
    public void onProjectImported(FolderEntry projectFolder)
            throws ForbiddenException, ConflictException, ServerException, IOException, NotFoundException {
        fillMavenProject(projectFolder);

    }

    private void fillMavenProject(FolderEntry projectFolder)
            throws IOException, ForbiddenException, ServerException, ConflictException, NotFoundException {
        VirtualFileEntry pom = projectFolder.getChild("pom.xml");
        if (pom != null) {
            Model model = Model.readFrom(pom.getVirtualFile());
            String packaging = model.getPackaging();
            if (packaging.equals("pom")) {
                createProjectsOnModules(model, projectManager.getProject(projectFolder.getWorkspace(), projectFolder.getPath()), projectFolder.getWorkspace());
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

    private ProjectConfig createProjectConfig(FolderEntry folderEntry)
            throws ServerException, ForbiddenException, IOException {
        Builders builders = new Builders();
        builders.setDefault("maven");

        VirtualFileEntry pom = folderEntry.getChild("pom.xml");
        Model model = Model.readFrom(pom.getVirtualFile());

        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(ARTIFACT_ID, new AttributeValue(model.getArtifactId()));
        attributes.put(GROUP_ID, new AttributeValue(model.getGroupId()));
        attributes.put(VERSION, new AttributeValue(model.getVersion()));
        attributes.put(PACKAGING, new AttributeValue(model.getPackaging()));

        return new ProjectConfig("Maven", MAVEN_ID, attributes, null, builders, null);
    }

    private void createProjectsOnModules(Model model, Project parentProject, String ws)
            throws ServerException, ForbiddenException, ConflictException, IOException, NotFoundException {
        List<String> modules = model.getModules();
        for (String module : modules) {
            FolderEntry parentFolder = getParentFolder(module, parentProject);
            module = module.replaceAll("\\.{2}/", "");
            FolderEntry moduleEntry = (FolderEntry)parentFolder.getChild(module);
            if (moduleEntry != null && !moduleEntry.isProjectFolder() && moduleEntry.getVirtualFile().getChild("pom.xml") != null) {
                Project project = projectManager.getProject(ws, parentFolder.getPath() + "/" + module);
                ProjectConfig projectConfig = createProjectConfig(moduleEntry);
                if (project == null) {
                    project = new Project(moduleEntry, projectManager);
                }
                project.updateConfig(projectConfig);

                projectManager.addModule(ws,
                                         parentProject.getPath(),
                                         module,
                                         projectConfig,
                                         new HashMap<String, String>(),
                                         parentProject.getVisibility());

                fillMavenProject(project.getBaseFolder());
            }
        }
    }

    @Override
    public String getProjectType() {
        return MAVEN_ID;
    }
}
