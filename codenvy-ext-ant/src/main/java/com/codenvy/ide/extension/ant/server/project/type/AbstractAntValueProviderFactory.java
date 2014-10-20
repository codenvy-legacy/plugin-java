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
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;

import org.apache.tools.ant.helper.ProjectHelper2;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Provide value for specific property from Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
public abstract class AbstractAntValueProviderFactory implements ValueProviderFactory {

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
     *         if build.xml file doesn't exist
     */
    protected org.apache.tools.ant.Project readAntProject(Project project) throws ServerException, ForbiddenException,
                                                                                  ValueStorageException {
        FileEntry buildFile = (FileEntry)project.getBaseFolder().getChild("build.xml");
        if (buildFile == null) {
            throw new ValueStorageException("build.xml does not exist.");
        }

        File ioBuildFile = ((VirtualFileImpl)buildFile.getVirtualFile()).getIoFile();

        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();
        ProjectHelper2.configureProject(antProject, ioBuildFile);
        return antProject;
    }

    /** @return instance of {@link ValueStorageException} with specified message. */
    protected ValueStorageException readException(Exception e) {
        return new ValueStorageException("Can't read build.xml : " + e.getMessage());
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
                org.apache.tools.ant.Project antProject = readAntProject(project);
                String value = getValue(antProject);
                if (value == null) {
                    return null;
                }
                return Arrays.asList(value);
            } catch (ServerException | ForbiddenException e) {
                throw readException(e);
            }
        }

        /** @return value for the specified attribute from {@link org.apache.tools.ant.Project}. */
        protected abstract String getValue(org.apache.tools.ant.Project antProject);
    }

    /**
     * Fetch source directory path from {@link org.apache.tools.ant.Project} if it exist, otherwise fetch default value.
     *
     * @param antProject
     *         parsed Ant {@link org.apache.tools.ant.Project}
     * @param ideProject
     *         current opened IDE project
     * @return relative path of source folder
     */
    protected String getSourceDir(org.apache.tools.ant.Project antProject, Project ideProject) {
        Hashtable<String, Object> properties = antProject.getProperties();
        if (properties.containsKey("src.dir")) {
            String absSrcPath = (String)properties.get("src.dir");
            String absProjectPath = ((VirtualFileImpl)ideProject.getBaseFolder().getVirtualFile()).getIoFile().getAbsolutePath();
            absSrcPath = absSrcPath.substring(absProjectPath.length());

            if (absSrcPath.startsWith("/")) return absSrcPath.substring(1);

            return absSrcPath;
        }

        return AntAttributes.DEF_SRC_PATH;
    }

    /**
     * Fetch test source directory path from {@link org.apache.tools.ant.Project} if it exist, otherwise fetch default value.
     *
     * @param antProject
     *         parsed Ant {@link org.apache.tools.ant.Project}
     * @param ideProject
     *         current opened IDE project
     * @return relative path of source folder
     */
    protected String getTestSourceDir(org.apache.tools.ant.Project antProject, Project ideProject) {
        Hashtable<String, Object> properties = antProject.getProperties();
        if (properties.containsKey("test.dir")) {
            String absSrcPath = (String)properties.get("test.dir");
            String absProjectPath = ((VirtualFileImpl)ideProject.getBaseFolder().getVirtualFile()).getIoFile().getAbsolutePath();
            absSrcPath = absSrcPath.substring(absProjectPath.length());

            if (absSrcPath.startsWith("/")) return absSrcPath.substring(1);

            return absSrcPath;
        }

        return AntAttributes.DEF_TEST_SRC_PATH;
    }
}
