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

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.ant.tools.AntUtils;

import java.io.IOException;
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
    protected VirtualFile getBuildXml(FolderEntry project) throws ServerException, ForbiddenException, ValueStorageException {
        VirtualFileEntry buildXml = project.getChild(BUILD_FILE);
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
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new AntValueProvider(projectFolder);
    }



    /** Provide access to value of various information from {@link org.apache.tools.ant.Project}. */
    protected class AntValueProvider implements ValueProvider {
        /** IDE project. */
        private final FolderEntry projectFolder;

        /** Create instance of {@link AntValueProvider}. */
        protected AntValueProvider(FolderEntry projectFolder) {
            this.projectFolder = projectFolder;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            try {
                org.apache.tools.ant.Project antProject = AntUtils.readProject(getBuildXml(projectFolder));
                return Collections.unmodifiableList(getValues(attributeName));
            } catch (Exception e) {
                throw readException(e);
            }
        }

        @Override
        public void setValues(String attributeName, List<String> value) throws ValueStorageException {

        }

    }
}