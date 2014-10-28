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
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/** @author Vladyslav Zhukovskii */
@Singleton
public class AntTestSourceFolderValueProviderFactory extends AbstractAntValueProviderFactory {
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AntAttributes.TEST_SOURCE_FOLDER;
    }

    /** {@inheritDoc} */
    @Override
    public ValueProvider newInstance(Project project) {
        return new AntValueProvider(project) {
            @Override
            public List<String> getValues(org.apache.tools.ant.Project antProject) {
                Hashtable<String, Object> properties = antProject.getProperties();
                if (properties.containsKey("test.dir")) {
                    String absSrcPath = (String)properties.get("test.dir");
                    String absProjectPath = ((VirtualFileImpl)project.getBaseFolder().getVirtualFile()).getIoFile().getAbsolutePath();
                    absSrcPath = absSrcPath.substring(absProjectPath.length());

                    if (absSrcPath.startsWith("/")) return Arrays.asList(absSrcPath.substring(1));

                    return Arrays.asList(absSrcPath);
                }

                return Arrays.asList(AntAttributes.DEF_TEST_SRC_PATH);
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {
                if (value == null || value.isEmpty()) {
                    return;
                }
                if (value.size() > 1) {
                    throw new IllegalArgumentException("Must be only one test source folder");
                }

                try {
                    String testSrcPath = value.get(0);
                    if (project.getBaseFolder().getChild(testSrcPath) == null) {
                        project.getBaseFolder().createFolder(testSrcPath);

                    }

                    getOrCreateDefaultAntProject(project);
                } catch (ForbiddenException | ServerException | ConflictException e) {
                    throw writeException(e);
                }
            }
        };
    }
}
