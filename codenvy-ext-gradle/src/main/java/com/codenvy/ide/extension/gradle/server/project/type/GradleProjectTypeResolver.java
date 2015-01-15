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
package com.codenvy.ide.extension.gradle.server.project.type;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.*;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
                return false;
            }

            if (projectFolder.getChild("build.gradle") == null) {
                return false;
            }

            Project project = new Project(projectFolder, projectManager);
            project.updateDescription(createProjectDescriptor(projectType));

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
}
