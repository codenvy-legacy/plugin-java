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

package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.shared.InvalidValueException;
import com.codenvy.api.project.shared.ValueProvider;
import com.codenvy.api.project.shared.ValueStorageException;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.MavenUtils;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class MavenTestSourceFolderValueProviderFactory extends AbstractMavenValueProviderFactory {
    @Override
    public String getName() {
        return MavenAttributes.TEST_SOURCE_FOLDER;
    }

    @Override
    public ValueProvider newInstance(Project project) {
        return new MavenValueProvider(project) {
            @Override
            protected String getValue(Model model) {
                Build build = model.getBuild();
                if(build != null && build.getTestSourceDirectory() != null) {
                    return build.getTestSourceDirectory();
                }
                return "src/test/java";
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
                    String srcPath = value.get(0);
                    if (project.getBaseFolder().getChild(srcPath) == null) {
                        project.getBaseFolder().createFolder(srcPath);

                    }
                    if(!"src/test/java".equals(srcPath)) {
                        VirtualFile pom = getOrCreatePom(project);
                        MavenUtils.setTestSourceFolder(pom, srcPath);
                    }
                } catch (ForbiddenException | ServerException | ConflictException | IOException e) {
                    throwWriteException(e);
                }
            }
        };
    }
}
