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
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.ant.tools.AntUtils;
import com.codenvy.ide.ant.tools.buildfile.BuildFileGenerator;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;

import org.apache.tools.ant.helper.ProjectHelper2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Provide value for specific property from Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public abstract class AbstractAntValueProviderFactory implements ValueProviderFactory {

    /**
     * Try to find build.xml in project root directory and parse it into {@link org.apache.tools.ant.Project} to ba able to obtain various
     * information from Ant build file. Otherwise if no build.xml was found try to create default one.
     *
     * @param project
     *         current opened project in Codenvy
     * @return {@link org.apache.tools.ant.Project} object of parsed build file
     * @throws ServerException
     *         if error occurred while getting file on server side
     * @throws ForbiddenException
     *         if access to build file is forbidden
     */
    protected VirtualFile getOrCreateBuildXml(Project project) throws ServerException, ForbiddenException {
        VirtualFileEntry buildXml = project.getBaseFolder().getChild(AntAttributes.BUILD_FILE);
        if (buildXml == null) {
            final byte[] buildXmlContent = new BuildFileGenerator(project.getName()).getBuildFileContent().getBytes();
            try {
                buildXml = project.getBaseFolder().createFile(AntAttributes.BUILD_FILE, buildXmlContent, null);
            } catch (ConflictException e) {
                // File existence was checked before create so not expected to be here.
                throw new ServerException(e.getServiceError());
            }
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

    /** Provide access to value of various information from {@link org.apache.tools.ant.Project}. */
    protected abstract class AntValueProvider implements ValueProvider {

        /** IDE project. */
        protected Project project;

        /** Create instance of {@link AntValueProvider}. */
        protected AntValueProvider(Project project) {
            this.project = project;
        }

        /** {@inheritDoc} */
        @Override
        public List<String> getValues() throws ValueStorageException {
            try {
                org.apache.tools.ant.Project antProject = AntUtils.readProject(getOrCreateBuildXml(project));
                return Collections.unmodifiableList(getValues(antProject));
            } catch (IOException | ForbiddenException | ServerException e) {
                throw readException(e);
            }
        }

        /** @return value for the specified attribute from {@link org.apache.tools.ant.Project}. */
        public abstract List<String> getValues(org.apache.tools.ant.Project antProject);
    }
}
