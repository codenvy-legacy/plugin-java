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
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectConfig;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.server.handlers.CreateModuleHandler;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author Vitaly Parfonov
 */
public class AddMavenModuleHandler implements CreateModuleHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AddMavenModuleHandler.class);

    @Override
    public void onCreateModule(FolderEntry parentFolder, String modulePath, ProjectConfig moduleConfig, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        if (!moduleConfig.getTypeId().equals(MavenAttributes.MAVEN_ID)) {
            LOG.warn("Module must be Maven project to able be added to Maven project");
            throw new IllegalArgumentException("Module must be Maven project to able be added to Maven project");
        }
        VirtualFileEntry pom = parentFolder.getChild("pom.xml");
        if (pom == null) {
            throw new IllegalArgumentException("Can't find pom.xml file in path: " + parentFolder.getPath());
        }
        try {
            Model model = Model.readFrom(pom.getVirtualFile());
            if ("pom".equals(model.getPackaging())) {
                final String relativePath = modulePath.substring(parentFolder.getPath().length() + 1);
                if (!model.getModules().contains(relativePath)) {
                    model.addModule(relativePath);
                    model.writeTo(pom.getVirtualFile());
                }
            } else {
                throw new IllegalArgumentException("Project must have packaging 'pom' in order to adding modules.");
            }
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public String getProjectType() {
        return MavenAttributes.MAVEN_ID;
    }
}
