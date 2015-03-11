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
package com.codenvy.ide.extension.gradle.server.project.handler;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.handlers.GetModulesHandler;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.List;

import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GetGradleModulesHandler implements GetModulesHandler {
    /** {@inheritDoc} */
    @Override
    public void onGetModules(FolderEntry parentProjectFolder, List<String> modulesPath)
            throws ForbiddenException, ServerException, NotFoundException, IOException {
        List<FolderEntry> childFolders = parentProjectFolder.getChildFolders();
        for (FolderEntry folderEntry : childFolders) {
            if (folderEntry.isProjectFolder() && !modulesPath.contains(folderEntry.getPath())) {
                modulesPath.add(folderEntry.getPath());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectType() {
        return GRADLE_ID;
    }
}
