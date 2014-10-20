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

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.List;

/** @author Vladyslav Zhukovskii */
@Singleton
public class AntSourceFoldersValueProviderFactory extends AbstractAntValueProviderFactory {
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AntAttributes.BUILDER_SOURCE_FOLDERS;
    }

    /** {@inheritDoc} */
    @Override
    public ValueProvider newInstance(final Project project) {
        return new ValueProvider() {
            @Override
            public List<String> getValues() throws ValueStorageException {
                org.apache.tools.ant.Project antProject;
                try {
                    antProject = readAntProject(project);
                } catch (ServerException | ForbiddenException e) {
                    throw readException(e);
                }

                String srcDir = getSourceDir(antProject, project);
                String testSrcDir = getTestSourceDir(antProject, project);

                return Arrays.asList(srcDir, testSrcDir);
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {
                // doesn't support writing values
            }
        };
    }
}
