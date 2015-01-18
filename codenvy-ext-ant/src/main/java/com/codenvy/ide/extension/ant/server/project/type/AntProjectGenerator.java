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

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.handlers.CreateProjectHandler;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.ide.extension.ant.shared.AntAttributes;

import java.util.Map;

/**
 * Generates Ant-project structure.
 *
 * @author Artem Zatsarynnyy
 */
public class AntProjectGenerator implements CreateProjectHandler {


    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
//        final String buildXmlContent = new BuildFileGenerator(newProjectDescriptor.getName()).getBuildFileContent();
//        baseFolder.createFile(BUILD_FILE, buildXmlContent.getBytes(), "text/xml");
//
//        Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();
//        List<String> sourceFolders = attributes.get(SOURCE_FOLDER);
//        if (sourceFolders != null) {
//            baseFolder.createFolder(sourceFolders.get(0));
//        }
//        List<String> testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
//        if (testSourceFolders != null) {
//            baseFolder.createFolder(testSourceFolders.get(0));
//        }

    }

    @Override
    public String getProjectType() {
        return AntAttributes.ANT_ID;
    }
}
