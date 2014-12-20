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
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.ant.tools.AntUtils;
import com.codenvy.ide.extension.ant.shared.AntAttributes;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.codenvy.ide.extension.ant.shared.AntAttributes.BUILD_FILE;

/**
 * Provide value for specific property from Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public class AntValueProviderFactory implements ValueProviderFactory {

    /**
     * Try to find build.xml in project root directory and parse it into {@link org.apache.tools.ant.Project} to ba able to obtain various
     * information from Ant build file.
     *
     * @param project
     *         current opened project in Codenvy
     * @return {@link org.apache.tools.ant.Project} object of parsed build file
     * @throws ServerException
     *         if error occurred while getting file on server side
     * @throws ForbiddenException
     *         if access to build file is forbidden
     * @throws ValueStorageException
     */
    protected VirtualFile getBuildXml(Project project) throws ServerException, ForbiddenException, ValueStorageException {
        VirtualFileEntry buildXml = project.getBaseFolder().getChild(BUILD_FILE);
        if (buildXml == null) {
            throw new ValueStorageException(BUILD_FILE + " does not exist.");
        }
        return buildXml.getVirtualFile();
    }

    /** @return instance of {@link ValueStorageException} with specified message. */
    protected ValueStorageException readException(Exception e) {
        return new ValueStorageException("Can't read build.xml: " + e.getMessage());
    }

    /** @return instance of {@link ValueStorageException} with specified message. */
    protected ValueStorageException writeException(Exception e) {
        return new ValueStorageException("Can't write build.xml: " + e.getMessage());
    }

    @Override
    public ValueProvider newInstance(Project project) {
        return new AntValueProvider(project);
    }

    /** Provide access to value of various information from {@link org.apache.tools.ant.Project}. */
    protected class AntValueProvider implements ValueProvider {

        /** IDE project. */
        protected Project project;

        /** Create instance of {@link AntValueProvider}. */
        protected AntValueProvider(Project project) {
            this.project = project;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            try {
                org.apache.tools.ant.Project antProject = AntUtils.readProject(getBuildXml(project));
                if (AntAttributes.SOURCE_FOLDER.equals(attributeName)) {
                    String srcDir = antProject.getProperty("src.dir");
                    if (srcDir == null) {
                        srcDir = AntAttributes.DEF_TEST_SRC_PATH;
                    } else {
                        // Don't show absolute path (seems Ant parser resolves it automatically). User shouldn't know any absolute paths on our
                        // file system. This is temporary solution, this shouldn't be actual when get rid form ant parsers for build.xml files.
                        final java.nio.file.Path relPath = antProject.getBaseDir().toPath().relativize(Paths.get(srcDir));
                        srcDir = relPath.toString();
                    }
                    return Arrays.asList(srcDir);
                } else if(AntAttributes.TEST_SOURCE_FOLDER.equals(attributeName)) {
                    String testDir = antProject.getProperty("test.dir");
                    if (testDir == null) {
                        testDir = AntAttributes.DEF_TEST_SRC_PATH;
                    }
                    return Arrays.asList(testDir);
                }
                return Collections.emptyList();
            } catch (IOException | ForbiddenException | ServerException e) {
                throw readException(e);
            }
        }

        @Override
        public void setValues(String attributeName, List<String> value) throws ValueStorageException, InvalidValueException {

        }


    }
}
