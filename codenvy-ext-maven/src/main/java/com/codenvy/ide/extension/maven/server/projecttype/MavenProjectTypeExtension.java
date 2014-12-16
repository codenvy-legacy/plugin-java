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

import com.codenvy.api.project.newproj.server.AbstractProjectType;
import com.codenvy.api.project.server.ProjectTemplateDescriptionLoader;
import com.codenvy.api.project.shared.Builders;
import com.codenvy.api.project.shared.Runners;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/** @author Evgen Vidolob */
@Singleton
public class MavenProjectTypeExtension extends AbstractProjectType {
    private static final Logger LOG = LoggerFactory.getLogger(MavenProjectTypeExtension.class);
    private final ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader;

    @Inject
    public MavenProjectTypeExtension(ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader,
                                     MavenValueProviderFactory mavenArtifactIdValueProviderFactory) {
        super(MavenAttributes.MAVEN_ID, MavenAttributes.MAVEN_NAME);

        addConstantDefinition(Constants.LANGUAGE, "language", "java");
        addConstantDefinition(Constants.LANGUAGE_VERSION, "language", "1.6");
        addVariableDefinition(MavenAttributes.GROUP_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.ARTIFACT_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.VERSION, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PARENT_VERSION, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PARENT_ARTIFACT_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PARENT_GROUP_ID, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.PACKAGING, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.SOURCE_FOLDER, "", true, mavenArtifactIdValueProviderFactory);
        addVariableDefinition(MavenAttributes.TEST_SOURCE_FOLDER, "", true, mavenArtifactIdValueProviderFactory);
        this.projectTemplateDescriptionLoader = projectTemplateDescriptionLoader;
    }


    //    @Override
    public Builders getBuilders() {
        return new Builders("maven");
    }

    //    @Override
    public Runners getRunners() {
        return null;
    }

//    @Override
//    public List<ProjectTemplateDescription> getTemplates() {
//        final List<ProjectTemplateDescription> list = new ArrayList<>();
//        try {
//            projectTemplateDescriptionLoader.load(getProjectType().getId(), list);
//        } catch (IOException e) {
//            LOG.error("Unable to load external templates for project type: {}", getProjectType().getId());
//        }
//        return list;
//    }


}