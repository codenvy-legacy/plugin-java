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
package com.codenvy.ide.extension.gradle.server.project.type;

import com.codenvy.api.project.server.Attribute;
import com.codenvy.api.project.server.Builders;
import com.codenvy.api.project.server.ProjectTemplateDescription;
import com.codenvy.api.project.server.ProjectTemplateDescriptionLoader;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.api.project.server.ProjectTypeExtension;
import com.codenvy.api.project.server.Runners;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GradleProjectTypeExtension implements ProjectTypeExtension {

    private static final Logger LOG = LoggerFactory.getLogger(GradleProjectTypeExtension.class);
    private final ProjectType                      projectType;
    private final ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader;

    @Inject
    public GradleProjectTypeExtension(ProjectTypeDescriptionRegistry registry,
                                      ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader) {
        this.projectTemplateDescriptionLoader = projectTemplateDescriptionLoader;
        this.projectType = new ProjectType(GradleAttributes.GRADLE_ID, GradleAttributes.GRADLE_NAME, Constants.JAVA_CATEGORY);
        registry.registerProjectType(this);
    }

    @Override
    public ProjectType getProjectType() {
        return projectType;
    }

    @Nonnull
    @Override
    public List<Attribute> getPredefinedAttributes() {
        final List<Attribute> list = new ArrayList<>(1);
        list.add(new Attribute(Constants.LANGUAGE, "java"));
        return list;
    }

    @Override
    public Builders getBuilders() {
        return new Builders(GradleAttributes.GRADLE_ID);
    }

    @Override
    public Runners getRunners() {
        return null;
    }

    @Override
    public List<ProjectTemplateDescription> getTemplates() {
        final List<ProjectTemplateDescription> list = new ArrayList<>();
        try {
            projectTemplateDescriptionLoader.load(getProjectType().getId(), list);
        } catch (IOException e) {
            LOG.error("Unable to load external templates for project type: {}", getProjectType().getId());
        }
        return list;
    }

    @Override
    public Map<String, String> getIconRegistry() {
        return null;
    }
}
