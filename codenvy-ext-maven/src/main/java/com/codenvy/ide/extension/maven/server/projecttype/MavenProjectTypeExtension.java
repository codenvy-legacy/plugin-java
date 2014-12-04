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

import com.codenvy.api.project.server.Attribute;
import com.codenvy.api.project.server.Builders;
import com.codenvy.api.project.server.ProjectTemplateDescription;
import com.codenvy.api.project.server.ProjectTemplateDescriptionLoader;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.api.project.server.ProjectTypeExtension;
import com.codenvy.api.project.server.Runners;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Evgen Vidolob */
@Singleton
public class MavenProjectTypeExtension implements ProjectTypeExtension {
    private static final Logger LOG = LoggerFactory.getLogger(MavenProjectTypeExtension.class);
    private final ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader;
    private final ProjectType                      projectType;

    @Inject
    public MavenProjectTypeExtension(ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader,
                                     ProjectTypeDescriptionRegistry registry) {
        this.projectTemplateDescriptionLoader = projectTemplateDescriptionLoader;
        projectType = new ProjectType(MavenAttributes.MAVEN_ID, MavenAttributes.MAVEN_NAME, Constants.JAVA_CATEGORY);
        registry.registerProjectType(this);
    }

    @Override
    public ProjectType getProjectType() {
        return projectType;
    }

    @Override
    public List<Attribute> getPredefinedAttributes() {
        final List<Attribute> list = new ArrayList<>(1);
        list.add(new Attribute(Constants.LANGUAGE, "java"));
        return list;
    }

    @Override
    public Builders getBuilders() {
        return new Builders("maven");
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
        Map<String, String> iconRegistry = new HashMap<>();
        iconRegistry.put("big.project.icon.svg", "maven/icons/maven.svg");
        return iconRegistry;
    }
}