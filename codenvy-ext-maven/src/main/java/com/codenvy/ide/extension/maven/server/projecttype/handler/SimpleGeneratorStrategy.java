/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.maven.server.projecttype.handler;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.ide.maven.tools.Build;
import com.codenvy.ide.maven.tools.Model;

import java.util.Map;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.*;

/**
 * Generates simple Maven project.
 *
 * @author Artem Zatsarynnyy
 */
public class SimpleGeneratorStrategy implements GeneratorStrategy {

    @Override
    public String getId() {
        return SIMPLE_GENERATION_STRATEGY;
    }

    @Override
    public void generateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        //Map<String, List<String>> attributes = newProjectDescriptor.getAttributes();
        AttributeValue artifactId = attributes.get(ARTIFACT_ID);
        AttributeValue groupId = attributes.get(GROUP_ID);
        AttributeValue version = attributes.get(VERSION);
        if (artifactId == null)
            throw new ConflictException("Missed required attribute artifactId");

        if (groupId == null)
            throw new ConflictException("Missed required attribute groupId");

        if (version == null)
            throw new ConflictException("Missed required attribute version");

        Model model = Model.createModel();
        model.setModelVersion("4.0.0");
        baseFolder.createFile("pom.xml", new byte[0], "text/xml");

        AttributeValue parentArtifactId = attributes.get(PARENT_ARTIFACT_ID);
        if (parentArtifactId != null) {
            model.setArtifactId(parentArtifactId.getString());
        }
        AttributeValue parentGroupId = attributes.get(PARENT_GROUP_ID);
        if (parentGroupId != null) {
            model.setGroupId(parentGroupId.getString());
        }
        AttributeValue parentVersion = attributes.get(PARENT_VERSION);
        if (parentVersion != null) {
            model.setVersion(parentVersion.getString());
        }
        model.setArtifactId(artifactId.getString());
        model.setGroupId(groupId.getString());
        model.setVersion(version.getString());
        AttributeValue packaging = attributes.get(PACKAGING);
        if (packaging != null) {
            model.setPackaging(packaging.getString());
        }
        AttributeValue sourceFolders = attributes.get(SOURCE_FOLDER);
        if (sourceFolders != null) {
            final String sourceFolder = sourceFolders.getString();
            baseFolder.createFolder(sourceFolder);
            if (!"src/main/java".equals(sourceFolder)) {
                model.setBuild(new Build().setSourceDirectory(sourceFolder));
            }
        }
        AttributeValue testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
        if (testSourceFolders != null) {
            final String testSourceFolder = testSourceFolders.getString();
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
