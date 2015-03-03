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
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.api.project.server.handlers.ProjectTypeChangedHandler;
import com.codenvy.ide.extension.maven.server.projecttype.MavenProjectResolver;
import com.google.inject.Inject;

import java.io.IOException;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Vitaly Parfonov
 */
public class ProjectHasBecomeMaven implements ProjectTypeChangedHandler {


    @Inject
    private ProjectManager projectManager;

    @Override
    public void onProjectTypeChanged(FolderEntry projectFolder)
            throws ForbiddenException, ConflictException, ServerException, IOException, NotFoundException {
        MavenProjectResolver.resolve(projectFolder, projectManager);

    }

    @Override
    public String getProjectType() {
        return MAVEN_ID;
    }
}
