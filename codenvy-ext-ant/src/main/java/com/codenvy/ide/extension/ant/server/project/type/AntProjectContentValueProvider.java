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
package com.codenvy.ide.extension.ant.server.project.type;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.ant.tools.buildfile.BuildFileGenerator;
import com.codenvy.ide.extension.ant.shared.AntAttributes;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/** @author Vladyslav Zhukovskii */
public class AntProjectContentValueProvider implements ValueProviderFactory {

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AntAttributes.ANT_PROJECT_CONTENT;
    }

    /** {@inheritDoc} */
    @Override
    public ValueProvider newInstance(final Project project) {
        return new ValueProvider() {
            @Override
            public List<String> getValues() throws ValueStorageException {
                return null; // nothing to get
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {
                if (!value.isEmpty() && AntAttributes.ANT_DEF_PROJECT_CONTENT.equals(value.get(0))) {
                    String buildFile;
                    try {
                        buildFile = new BuildFileGenerator(project.getName()).getBuildFileContent();
                    } catch (ParserConfigurationException | IOException | TransformerException e) {
                        throw new IllegalStateException("Failed to generate Ant build file.");
                    }

                    FolderEntry projectFolder = project.getBaseFolder();

                    try {
                        projectFolder.createFile("build.xml", buildFile.getBytes(), "text/xml");

                        projectFolder.createFolder("src");
                        projectFolder.createFolder("lib");
                    } catch (ForbiddenException | ConflictException | ServerException e) {
                        throw new ValueStorageException("Can't create project structure: " + e.getMessage());
                    }
                }
            }
        };
    }
}
