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
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.Build;
import com.codenvy.ide.maven.tools.Model;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

/**
 * {@link ValueProviderFactory} implementation for 'maven.source.folder' attribute.
 *
 * @author Artem Zatsarynnyy
 * @author Evgen Vidolob
 */
@Singleton
public class MavenSourceFolderValueProviderFactory extends AbstractMavenValueProviderFactory {

    @Override
    public String getName() {
        return MavenAttributes.SOURCE_FOLDER;
    }

    @Override
    public ValueProvider newInstance(final Project project) {
        return new MavenValueProvider(project) {

            @Override
            protected String getValue(Model model) {
                Build build = model.getBuild();
                if (build != null && build.getSourceDirectory() != null) {
                    return build.getSourceDirectory();
                }
                return "src/main/java";
            }

            @Override
            public void setValues(List<String> strings) throws ValueStorageException {
                if (strings == null || strings.isEmpty()) {
                    return;
                }
                if (strings.size() > 1) {
                    throw new IllegalArgumentException("Must be only one source folder");
                }

                try {
                    String srcPath = strings.get(0);
//                    if (project.getBaseFolder().getChild(srcPath) == null) {
//                        project.getBaseFolder().createFolder(srcPath);
//                    }
                    if (!"src/main/java".equals(srcPath)) {
                        VirtualFile pom = getPom(project);
                        if (pom != null) {
                            Model model = Model.readFrom(pom);
                            Build build = model.getBuild();
                            if (build != null) {
                                build.setSourceDirectory(srcPath);
                            } else {
                                model.setBuild(new Build().setSourceDirectory(srcPath));
                            }
                            model.writeTo(pom);
                        }
                    }
                } catch (ForbiddenException | ServerException | IOException e) {
                    throwWriteException(e);
                }
            }
        };
    }
}
