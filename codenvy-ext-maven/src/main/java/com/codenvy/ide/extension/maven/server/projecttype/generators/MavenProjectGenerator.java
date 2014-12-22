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
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.api.vfs.server.VirtualFileSystemRegistry;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * @author gazarenkov
 */
@Singleton
public class MavenProjectGenerator implements ProjectGenerator {

    private final String[] slaveBuilderURLs;
    private final VirtualFileSystemRegistry vfsRegistry;

    @Inject
    public MavenProjectGenerator(@Named("builder.slave_builder_urls") String[] slaveBuilderURLs,
                                     VirtualFileSystemRegistry vfsRegistry) {
        // As a temporary solution we're using first slave builder URL
        // in order to get archetype-generator service URL.
        this.slaveBuilderURLs = slaveBuilderURLs;
        this.vfsRegistry = vfsRegistry;
    }

    @Override
    public String getProjectTypeId() {
        return MavenAttributes.MAVEN_ID;
    }

    @Override
    public void generateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {

        if(options != null && options.get("type").equals(MavenAttributes.ARCHETYPE_GENERATOR_ID))
            new ArchetypeProjectGenerator(slaveBuilderURLs, vfsRegistry).generateProject(baseFolder, attributes, options);
        else
            new SimpleProjectGenerator().generateProject(baseFolder, attributes);

    }
}
