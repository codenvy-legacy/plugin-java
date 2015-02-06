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
package com.codenvy.ide.extension.ant.server.project.type;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Builders;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectDescription;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.codenvy.ide.extension.ant.shared.AntAttributes.ANT_ID;

/** @author Vladyslav Zhukovskii */
@Singleton
public class AntProjectTypeResolver implements ProjectTypeResolver {

    @Inject
    private ProjectManager projectManager;

    /** {@inheritDoc} */
    @Override
    public boolean resolve(FolderEntry folderEntry) throws ServerException, ValueStorageException, InvalidValueException {
        try {
            if (!folderEntry.isProjectFolder()) {
                ProjectType projectType = projectManager.getTypeDescriptionRegistry().getProjectType(ANT_ID);

                if (projectType == null) {
                    return false;
                }

                if (folderEntry.getChild(AntAttributes.BUILD_FILE) == null) {
                    return false;
                }

                Project project = new Project(folderEntry, projectManager);
                project.updateDescription(createProjectDescriptor(projectType));

                return true;
            }
            return false;//project configure in initial source
        } catch (ForbiddenException e) {
            throw new ServerException("An error occurred when trying to resolve ant project.", e);
        }
    }

    /** Create new {@link com.codenvy.api.project.server.Builders} description for resolved project. */
    private ProjectDescription createProjectDescriptor(ProjectType projectType) {
        Builders builders = new Builders();
        builders.setDefault("ant");
        return new ProjectDescription(projectType, builders, null);
    }
}
