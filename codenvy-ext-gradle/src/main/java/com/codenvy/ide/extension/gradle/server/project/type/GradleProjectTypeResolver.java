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
package com.codenvy.ide.extension.gradle.server.project.type;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.*;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GradleProjectTypeResolver implements ProjectTypeResolver {

    @Inject
    private ProjectManager projectManager;

    @Override
    public boolean resolve(FolderEntry projectFolder) throws ServerException, ValueStorageException, InvalidValueException {
        try {
            ProjectType projectType = projectManager.getTypeDescriptionRegistry().getProjectType(GradleAttributes.GRADLE_ID);

            if (projectType == null) {
                throw new ServerException(String.format("Project type '%s' not registered. ", GradleAttributes.GRADLE_ID));
            }

            if (projectFolder.getChild("build.gradle") == null) {
                return false;
            }

            Project project = new Project(projectFolder, projectManager);
            project.updateDescription(createProjectDescriptor(projectType));

            VirtualFile virtualProjectFolder = project.getBaseFolder().getVirtualFile();
            ProjectConnection connection = GradleConnector.newConnector()
                                                          .forProjectDirectory(((VirtualFileImpl)virtualProjectFolder).getIoFile())
                                                          .connect();

            try {
                GradleProject gradleProject = connection.getModel(GradleProject.class);

                createGradleProjectStructure(gradleProject, project, projectType);

            } finally {
                connection.close();
            }

            return true;
        } catch (ForbiddenException e) {
            throw new ServerException("An error occurred when trying to resolve gradle project.", e);
        }
    }

    /** Create new {@link com.codenvy.api.project.server.Builders} description for resolved project. */
    private ProjectDescription createProjectDescriptor(ProjectType projectType) {
        Builders builders = new Builders();
        builders.setDefault(GradleAttributes.GRADLE_ID);
        return new ProjectDescription(projectType, builders, null);
    }

    private void createGradleProjectStructure(GradleProject gradleProject, Project parentProject, ProjectType projectType)
            throws ServerException, ForbiddenException, ValueStorageException {
        for (GradleProject childProject : gradleProject.getChildren()) {
            final String path = childProject.getPath().startsWith(":") ? childProject.getPath().substring(1)
                                                                       : childProject.getPath();

            FolderEntry parentFolder = parentProject.getBaseFolder();

            FolderEntry childModuleFolder = (FolderEntry)parentFolder.getChild(path);
            if (childModuleFolder != null && !childModuleFolder.isProjectFolder()) {
                Project project = projectManager.getProject(parentProject.getWorkspace(), parentFolder.getPath() + "/" + path);
                if (project == null) {
                    project = new Project(childModuleFolder, projectManager);
                    project.getMisc().setCreationDate(System.currentTimeMillis());
                }
                createGradleProjectStructure(childProject, project, projectType);
                project.updateDescription(createProjectDescriptor(projectType));
            }


        }
    }
}
