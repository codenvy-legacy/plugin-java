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

import com.codenvy.api.project.server.ProjectTemplateDescriptionLoader;
import com.codenvy.api.project.server.ProjectTemplateRegistry;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.api.project.shared.dto.ProjectTemplateDescriptor;
import com.codenvy.ide.ext.java.server.projecttype.JavaProjectType;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

/** @author Evgen Vidolob */
@Singleton
public class MavenProjectType extends ProjectType2 {
    private static final Logger LOG = LoggerFactory.getLogger(MavenProjectType.class);
    private ProjectTemplateRegistry templateRegistry;
    private ProjectTemplateDescriptionLoader templateLoader;

    @Inject
    public MavenProjectType(ProjectTemplateRegistry templateRegistry,
                            ProjectTemplateDescriptionLoader templateLoader,
                            MavenValueProviderFactory mavenArtifactIdValueProviderFactory,
                            JavaProjectType javaProjectType) {

        super(MavenAttributes.MAVEN_ID, MavenAttributes.MAVEN_NAME);
        this.templateRegistry = templateRegistry;
        this.templateLoader = templateLoader;
        addVariableDefinition(MavenAttributes.GROUP_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.ARTIFACT_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.VERSION, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PARENT_VERSION, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PARENT_ARTIFACT_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PARENT_GROUP_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PACKAGING, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.SOURCE_FOLDER, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.TEST_SOURCE_FOLDER, "", true, mavenArtifactIdValueProviderFactory);

        addParent(javaProjectType);
        setDefaultBuilder("maven");

        registerTemplates();

    }

    private void registerTemplates() {
        try {
            List<ProjectTemplateDescriptor> list = templateLoader.load(MavenAttributes.MAVEN_ID);
            for(ProjectTemplateDescriptor templateDescriptor : list) {
                templateRegistry.register(templateDescriptor);
            }
        } catch (IOException e) {
           LOG.info("MavenProjectType", "Templates not loaded for maven project type");
        }


    }
}