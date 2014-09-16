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
import com.codenvy.api.project.server.FileEntry;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.VirtualFileEntry;
import com.codenvy.api.project.shared.ValueStorageException;
import com.codenvy.ide.maven.tools.MavenUtils;

import org.apache.maven.model.Model;

import java.io.IOException;

/**
 * @author Evgen Vidolob
 */
public abstract class AbstractMavenValueProviderFactory implements ValueProviderFactory {


    protected Model readModel(Project project) throws ValueStorageException, ServerException, ForbiddenException, IOException {
        FileEntry pomFile = (FileEntry)project.getBaseFolder().getChild("pom.xml");
        if(pomFile == null) {
            throw new ValueStorageException("pom.xml does not exist.");
        }
        return MavenUtils.readModel(pomFile.getInputStream());
    }

    protected Model readOrCreateModel(Project project) {
        FileEntry pomFile;
        try {
            pomFile = (FileEntry)project.getBaseFolder().getChild("pom.xml");
            Model model;
            if (pomFile != null) {
                model = MavenUtils.readModel(pomFile.getInputStream());
            } else {
                model = new Model();
                model.setModelVersion("4.0.0");
                MavenProjectGenerator.generateProjectStructure(project.getBaseFolder());
            }
            return model;
        } catch (ForbiddenException | ServerException | ConflictException | IOException e) {
            return null;
        }
    }

    protected void writeModel(Model model, Project project) throws ServerException, ForbiddenException, ConflictException, IOException {
        VirtualFileEntry pomFile = project.getBaseFolder().getChild("pom.xml");
        if(pomFile == null) {
            pomFile = project.getBaseFolder().createFile("pom.xml", new byte[0], "text/xml");
        }
        MavenUtils.writeModel(model, pomFile.getVirtualFile());
    }

    protected void throwReadException(Exception e) throws ValueStorageException {
        throw new ValueStorageException("Can't read pom.xml : " + e.getMessage());
    }
    protected void throwWriteException(Exception e) throws ValueStorageException {
        throw new ValueStorageException("Can't write pom.xml : " + e.getMessage());
    }
}
