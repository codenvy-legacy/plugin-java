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

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.commons.xml.XMLTreeException;
import com.codenvy.ide.maven.tools.Model;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public abstract class AbstractMavenValueProviderFactory implements ValueProviderFactory {

    protected Model readModel(Project project) throws ValueStorageException, ServerException, ForbiddenException, IOException {
        FileEntry pomFile = (FileEntry)project.getBaseFolder().getChild("pom.xml");
        if (pomFile == null) {
            throw new ValueStorageException("pom.xml does not exist.");
        }
        return Model.readFrom(pomFile.getInputStream());
    }

    @Nullable
    protected VirtualFile getPom(Project project) {
        try {
            final VirtualFileEntry pomFile = project.getBaseFolder().getChild("pom.xml");
            if (pomFile != null) {
                return pomFile.getVirtualFile();
            }
            return null;
        } catch (ForbiddenException | ServerException e) {
            return null;
        }
    }

    protected void throwReadException(Exception e) throws ValueStorageException {
        throw new ValueStorageException("Can't read pom.xml : " + e.getMessage());
    }

    protected void throwWriteException(Exception e) throws ValueStorageException {
        throw new ValueStorageException("Can't write pom.xml : " + e.getMessage());
    }

    protected abstract class MavenValueProvider implements ValueProvider {

        protected Project project;

        protected MavenValueProvider(Project project) {
            this.project = project;
        }

        @Override
        public List<String> getValues() throws ValueStorageException {
            try {
                Model model = readModel(project);
                String value = getValue(model);
                if (value == null) {
                    return null;
                }
                return Arrays.asList(value);
            } catch (ServerException | ForbiddenException | IOException | XMLTreeException e) {
                throwReadException(e);
            }
            return null;
        }

        protected abstract String getValue(Model model);
    }
}
