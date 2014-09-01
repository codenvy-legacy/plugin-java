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
import com.codenvy.api.project.shared.Attribute;
import com.codenvy.api.project.shared.ProjectDescription;
import com.codenvy.api.project.shared.ProjectType;
import com.codenvy.api.project.shared.dto.ProjectUpdate;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.MavenUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.maven.model.Model;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenProjectTypeResolver implements ProjectTypeResolver {
    @Inject
    private ProjectManager projectManager;

    @Override
    public boolean resolve(Project project, ProjectUpdate description) throws ServerException {
        try {
            ProjectType projectType = projectManager.getTypeDescriptionRegistry().getProjectType(Constants.MAVEN_ID);
            if(projectType == null){
                //thea are no maven project type registered
                return false;
            }
            if (project.getBaseFolder().getChild("pom.xml") == null) {
                return false;
            }

            description.setProjectTypeId(Constants.MAVEN_ID);
            description.setBuilder("maven");
            Model model = MavenUtils.readModel(project.getBaseFolder().getChild("pom.xml").getVirtualFile());
            Map<String, List<String>> attributes = description.getAttributes();
            String packaging = model.getPackaging();
            attributes.put(MavenAttributes.MAVEN_PACKAGING, Arrays.asList(packaging));
            attributes.put(MavenAttributes.MAVEN_ARTIFACT_ID, Arrays.asList(model.getArtifactId()));
            attributes.put(MavenAttributes.MAVEN_GROUP_ID, Arrays.asList(model.getGroupId()));
            switch (packaging) {
                case "pom":
                    createProjectsOnModules(model, project.getBaseFolder(), project.getWorkspace(), projectType);
                    break;
                case "war":
                    description.setRunner("JavaWeb");
                    break;
                case "jar":
                    description.setRunner("JavaStandalone");
                    break;
            }
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
            if(moduleEntry != null && moduleEntry.getVirtualFile().getChild("pom.xml") != null) {
                Project project = projectManager.getProject(ws, baseFolder.getPath() + "/" + module);
                if(project == null) {
                    project = new Project(ws, (FolderEntry)baseFolder.getChild(module), projectManager);
                }
                ProjectDescription description = project.getDescription();
                description.setProjectType(projectType);
                description.setBuilder("maven");
                Model moduleModel = MavenUtils.readModel(moduleEntry.getVirtualFile().getChild("pom.xml"));
                List<Attribute> attributeList = description.getAttributes();
                String packaging = model.getPackaging();
                attributeList.add(new Attribute(MavenAttributes.MAVEN_PACKAGING, Arrays.asList(packaging)));
                attributeList.add(new Attribute(MavenAttributes.MAVEN_ARTIFACT_ID, Arrays.asList(moduleModel.getArtifactId())));
                attributeList.add(new Attribute(MavenAttributes.MAVEN_GROUP_ID, Arrays.asList(moduleModel.getGroupId())));
                switch (packaging) {
                    case "pom":
                        createProjectsOnModules(moduleModel, project.getBaseFolder(), project.getWorkspace(), projectType);
                        break;
                    case "war":
                        description.setRunner("JavaWeb");
                        break;
                    case "jar":
                        description.setRunner("JavaStandalone");
                        break;
                }
                project.updateDescription(description);
            }
        }
    }
}
