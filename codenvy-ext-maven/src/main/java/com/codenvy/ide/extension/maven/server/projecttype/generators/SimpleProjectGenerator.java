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
package com.codenvy.ide.extension.maven.server.projecttype.generators;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectGenerator;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.ide.maven.tools.Build;
import com.codenvy.ide.maven.tools.Model;

import java.util.List;
import java.util.Map;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_VERSION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.SIMPLE_GENERATOR_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Generates simple Maven project.
 *
 * @author Artem Zatsarynnyy
 */
public class SimpleProjectGenerator implements ProjectGenerator {

    @Override
    public String getId() {
        return SIMPLE_GENERATOR_ID;
    }

    @Override
    public String getProjectTypeId() {
        return MAVEN_ID;
    }

    @Override
    public void generateProject(FolderEntry baseFolder, NewProject newProjectDescriptor)
            throws ForbiddenException, ConflictException, ServerException {
        Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();
        List<String> artifactId = attributes.get(ARTIFACT_ID);
        List<String> groupId = attributes.get(GROUP_ID);
        List<String> version = attributes.get(VERSION);
        if (artifactId == null || artifactId.isEmpty() || groupId == null || groupId.isEmpty() || version == null || version.isEmpty()) {
            throw new ServerException("Missed some required attribute (groupId, artifactId or version)");
        }

        Model model = Model.createModel();
        model.setModelVersion("4.0.0");
        baseFolder.createFile("pom.xml", new byte[0], "text/xml");

        List<String> parentArtifactId = attributes.get(PARENT_ARTIFACT_ID);
        if (parentArtifactId != null) {
            model.setArtifactId(parentArtifactId.get(0));
        }
        List<String> parentGroupId = attributes.get(PARENT_GROUP_ID);
        if (parentGroupId != null) {
            model.setGroupId(parentGroupId.get(0));
        }
        List<String> parentVersion = attributes.get(PARENT_VERSION);
        if (parentVersion != null) {
            model.setVersion(parentVersion.get(0));
        }
        model.setArtifactId(artifactId.get(0));
        model.setGroupId(groupId.get(0));
        model.setVersion(version.get(0));
        List<String> packaging = attributes.get(PACKAGING);
        if (packaging != null) {
            model.setPackaging(packaging.get(0));
        }
        List<String> sourceFolders = attributes.get(SOURCE_FOLDER);
        if (sourceFolders != null) {
            final String sourceFolder = sourceFolders.get(0);
            baseFolder.createFolder(sourceFolder);
            if (!"src/main/java".equals(sourceFolder)) {
                model.setBuild(new Build().setSourceDirectory(sourceFolder));
            }
        }
        List<String> testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
        if (testSourceFolders != null) {
            final String testSourceFolder = testSourceFolders.get(0);
            baseFolder.createFolder(testSourceFolder);
            if (!"src/test/java".equals(testSourceFolder)) {
                Build build = model.getBuild();
                if (build != null) {
                    build.setTestSourceDirectory(testSourceFolder);
                } else {
                    model.setBuild(new Build().setTestSourceDirectory(testSourceFolder));
                }
            }
        }
        model.writeTo(baseFolder.getChild("pom.xml").getVirtualFile());
    }
}
