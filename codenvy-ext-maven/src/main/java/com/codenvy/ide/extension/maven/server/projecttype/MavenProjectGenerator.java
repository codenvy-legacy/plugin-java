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
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectGenerator;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.maven.tools.MavenUtils;

import org.apache.maven.model.Model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.ext.java.shared.Constants.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_VERSION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Generates Maven-project structure.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenProjectGenerator implements ProjectGenerator {

    @Override
    public String getId() {
        return "maven";
    }

    @Override
    public String getProjectTypeId() {
        return MAVEN_ID;
    }

    @Override
    public void generateProject(FolderEntry baseFolder, NewProject newProjectDescriptor)
            throws ForbiddenException, ConflictException, ServerException {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        baseFolder.createFile("pom.xml", new byte[0], "text/xml");
        VirtualFile pomFile = baseFolder.getChild("pom.xml").getVirtualFile();
        try {
            MavenUtils.writeModel(model, pomFile);
            Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();

            List<String> parentArtifactId = attributes.get(PARENT_ARTIFACT_ID);
            if (parentArtifactId != null) {
                MavenUtils.setArtifactId(pomFile, parentArtifactId.get(0));
            }
            List<String> parentGroupId = attributes.get(PARENT_GROUP_ID);
            if (parentGroupId != null) {
                MavenUtils.setGroupId(pomFile, parentGroupId.get(0));
            }
            List<String> parentVersion = attributes.get(PARENT_VERSION);
            if (parentVersion != null) {
                MavenUtils.setVersion(pomFile, parentVersion.get(0));
            }
            List<String> artifactId = attributes.get(ARTIFACT_ID);
            if (artifactId != null) {
                MavenUtils.setArtifactId(pomFile, artifactId.get(0));
            }
            List<String> groupId = attributes.get(GROUP_ID);
            if (groupId != null) {
                MavenUtils.setGroupId(pomFile, groupId.get(0));
            }
            List<String> version = attributes.get(VERSION);
            if (version != null) {
                MavenUtils.setVersion(pomFile, version.get(0));
            }
            List<String> packaging = attributes.get(PACKAGING);
            if (packaging != null) {
                MavenUtils.setPackaging(pomFile, packaging.get(0));
            }
            List<String> sourceFolders = attributes.get(SOURCE_FOLDER);
            if (sourceFolders != null) {
                final String sourceFolder = sourceFolders.get(0);
                baseFolder.createFolder(sourceFolder);
                if (!"src/main/java".equals(sourceFolder)) {
                    MavenUtils.setSourceFolder(pomFile, sourceFolder);
                }
            }
            List<String> testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
            if (testSourceFolders != null) {
                final String testSourceFolder = testSourceFolders.get(0);
                baseFolder.createFolder(testSourceFolder);
                if (!"src/test/java".equals(testSourceFolder)) {
                    MavenUtils.setSourceFolder(pomFile, testSourceFolder);
                }
            }
        } catch (IOException e) {
            throw new ForbiddenException("Can't write pom.xml: " + e.getMessage());
        }
    }
}
