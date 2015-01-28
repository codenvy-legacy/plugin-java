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
import com.codenvy.api.project.server.ProjectGenerator;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.ide.ant.tools.buildfile.BuildFileGenerator;

import java.util.List;
import java.util.Map;

import static com.codenvy.ide.extension.ant.shared.AntAttributes.ANT_GENERATOR_ID;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.ANT_ID;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.BUILD_FILE;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.ant.shared.AntAttributes.TEST_SOURCE_FOLDER;

/**
 * Generates Ant-project structure.
 *
 * @author Artem Zatsarynnyy
 */
public class AntProjectGenerator implements ProjectGenerator {

    @Override
    public String getId() {
        return ANT_GENERATOR_ID;
    }

    @Override
    public String getProjectTypeId() {
        return ANT_ID;
    }

    @Override
    public void generateProject(FolderEntry baseFolder, NewProject newProjectDescriptor)
            throws ForbiddenException, ConflictException, ServerException {
        final String buildXmlContent = new BuildFileGenerator(newProjectDescriptor.getName()).getBuildFileContent();
        baseFolder.createFile(BUILD_FILE, buildXmlContent.getBytes(), "text/xml");

        Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();
        List<String> sourceFolders = attributes.get(SOURCE_FOLDER);
        if (sourceFolders != null) {
            baseFolder.createFolder(sourceFolders.get(0));
        }
        List<String> testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
        if (testSourceFolders != null) {
            baseFolder.createFolder(testSourceFolders.get(0));
        }
    }
}
