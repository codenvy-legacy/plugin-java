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
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.ProjectGenerator;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.vfs.server.VirtualFileSystem;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.ide.extension.maven.server.archetypegenerator.ArchetypeGenerator;
import com.codenvy.ide.extension.maven.server.archetypegenerator.GenerateResult;
import com.codenvy.ide.extension.maven.server.archetypegenerator.GeneratorException;
import com.codenvy.ide.extension.maven.server.archetypegenerator.MavenArchetype;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_GENERATOR_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Generates sample Maven project using maven-archetype-quickstart.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenProjectGenerator implements ProjectGenerator {
    private final ArchetypeGenerator        archetypeGenerator;
    private final VirtualFileSystemRegistry vfsRegistry;

    @Inject
    public MavenProjectGenerator(ArchetypeGenerator archetypeGenerator, VirtualFileSystemRegistry vfsRegistry) {
        this.archetypeGenerator = archetypeGenerator;
        this.vfsRegistry = vfsRegistry;
    }

    @Override
    public String getId() {
        return MAVEN_GENERATOR_ID;
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
            return;
        }

        MavenArchetype quickstartArchetype = new MavenArchetype("org.apache.maven.archetypes",
                                                                "maven-archetype-quickstart",
                                                                "RELEASE", null);
        try {
            final GenerateResult result = archetypeGenerator.generateFromArchetype(quickstartArchetype,
                                                                                   groupId.get(0),
                                                                                   artifactId.get(0),
                                                                                   version.get(0),
                                                                                   null);
            if (!result.isSuccessful()) {
                throw new ServerException(new String(Files.readAllBytes(result.getGenerateReport().toPath())));
            }
            copyGeneratedFiles(baseFolder, baseFolder.getWorkspace(), result.getResult());
        } catch (GeneratorException | IOException | NotFoundException e) {
            throw new ServerException(e);
        }
    }

    private void copyGeneratedFiles(FolderEntry baseFolder, String vfsId, File file)
            throws ForbiddenException, ServerException, NotFoundException, ConflictException, IOException {
        final VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null);
        vfs.importZip(baseFolder.getVirtualFile().getId(), Files.newInputStream(file.toPath()), true, false);
    }
}
