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

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectConfig;
import org.eclipse.che.api.project.server.handlers.CreateModuleHandler;
import org.eclipse.che.gradle.GradleUtils;
import org.eclipse.che.gradle.server.GradleTemplateHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.gradle.shared.GradleAttributes.PROJECT_CONTENT_MODIFY_OPTION;

/** @author Vladyslav Zhukovskii */
@Singleton
public class CreateGradleModuleHandler implements CreateModuleHandler {
    /** {@inheritDoc} */
    @Override
    public void onCreateModule(FolderEntry parentFolder, String modulePath, ProjectConfig moduleConfig, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {

        if (!(options == null || Boolean.parseBoolean(options.get(PROJECT_CONTENT_MODIFY_OPTION)))) {
            return;
        }

        FolderEntry rootProjectFolder = GradleUtils.getRootProjectFolder(parentFolder);
        Path relModulePath = Paths.get(rootProjectFolder.getPath()).relativize(Paths.get(parentFolder.getPath() + "/" + modulePath));
        try {
            //update settings.xml of root project
            GradleTemplateHelper.getInstance()
                                .createGradleSettingsFile(rootProjectFolder,
                                                          null,
                                                          Collections.singletonList(relModulePath.toString()));
        } catch (NotFoundException e) {
            throw new ServerException(e.getServiceError());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectType() {
        return GRADLE_ID;
    }
}
