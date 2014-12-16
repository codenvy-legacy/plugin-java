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
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.Build;
import com.codenvy.ide.maven.tools.Model;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class MavenValueProviderFactory implements ValueProviderFactory {

    protected Model readModel(Project project) throws ValueStorageException, ServerException, ForbiddenException, IOException {
        FileEntry pomFile = (FileEntry)project.getBaseFolder().getChild("pom.xml");
        if (pomFile == null) {
            throw new ValueStorageException("pomN.xml does not exist.");
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

    @Override
    public ValueProvider newInstance(Project project) {
        return new MavenValueProvider(project);
    }

    protected class MavenValueProvider implements ValueProvider {

        protected Project project;

        protected MavenValueProvider(Project project) {
            this.project = project;
        }

        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            try {
                String value = "";
                Model model = readModel(project);
                if (attributeName.equals(MavenAttributes.ARTIFACT_ID))
                    value = model.getArtifactId();
                if (attributeName.equals(MavenAttributes.GROUP_ID))
                    value = model.getGroupId();
                if (attributeName.equals(MavenAttributes.PACKAGING))
                    value = model.getPackaging();
                if (attributeName.equals(MavenAttributes.VERSION))
                    value = model.getVersion();
                if (attributeName.equals(MavenAttributes.PARENT_ARTIFACT_ID) && model.getParent() != null)
                    value = model.getParent().getArtifactId();
                if (attributeName.equals(MavenAttributes.PARENT_GROUP_ID) && model.getParent() != null)
                    value = model.getParent().getGroupId();
                if (attributeName.equals(MavenAttributes.SOURCE_FOLDER)) {
                    Build build = model.getBuild();
                    if (build != null && build.getSourceDirectory() != null) {
                        value = build.getSourceDirectory();
                    } else {
                        value = "src/main/java";
                    }
                }
                if (attributeName.equals(MavenAttributes.TEST_SOURCE_FOLDER)) {
                    Build build = model.getBuild();
                    if(build != null && build.getTestSourceDirectory() != null) {
                        value = build.getTestSourceDirectory();
                    } else {
                        value = "src/test/java";
                    }
                }



                return Arrays.asList(value);
            } catch (ServerException | ForbiddenException | IOException e) {
                throwReadException(e);
            }
            return null;
        }

        @Override
        public void setValues(String attributeName, List<String> value) throws ValueStorageException, InvalidValueException {
            try {
                VirtualFile pom = getPom(project);
                if (pom != null) {
                    Model.readFrom(pom)
                         .setArtifactId(value.get(0))
                         .writeTo(pom);
                }
            } catch (ForbiddenException | ServerException | IOException e) {
                throwWriteException(e);
            }
        }




    }
}
