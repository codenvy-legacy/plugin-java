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
package org.eclipse.che.gradle.server.project.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.ProjectTypeChangedHandler;
import org.eclipse.che.gradle.server.project.GradleProjectResolver;

import java.io.IOException;

import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;

/** @author Vladyslav Zhukovskii */
@Singleton
public class ProjectHasBecomeGradle implements ProjectTypeChangedHandler {
    private GradleProjectResolver projectResolver;

    @Inject
    public ProjectHasBecomeGradle(GradleProjectResolver projectResolver) {
        this.projectResolver = projectResolver;
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectTypeChanged(FolderEntry projectFolder)
            throws ForbiddenException, ConflictException, ServerException, IOException, NotFoundException {
        projectResolver.resolve(projectFolder);
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectType() {
        return GRADLE_ID;
    }
}
