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
package com.codenvy.ide.extension.ant.server.project.type;

import com.codenvy.api.project.server.*;
import com.codenvy.ide.ext.java.shared.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class AntProjectTypeExtension implements ProjectTypeExtension {
    /** {@inheritDoc} */
    @Override
    public Builders getBuilders() {
        return new Builders(Constants.ANT_ID);
    }

    /** {@inheritDoc} */
    @Override
    public Runners getRunners() {
        return null;
    }

    private static final Logger LOG = LoggerFactory.getLogger(AntProjectTypeExtension.class);
    private final ProjectType                      projectType;
    private final ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader;

    /** Create instance of {@link AntProjectTypeExtension}. */
    @Inject
    public AntProjectTypeExtension(ProjectTypeDescriptionRegistry registry,
                                   ProjectTemplateDescriptionLoader projectTemplateDescriptionLoader) {
        this.projectTemplateDescriptionLoader = projectTemplateDescriptionLoader;
        this.projectType = new ProjectType(Constants.ANT_ID, Constants.ANT_NAME, Constants.JAVA_CATEGORY);
        registry.registerProjectType(this);
    }

    /** {@inheritDoc} */
    @Override
    public ProjectType getProjectType() {
        return projectType;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Attribute> getPredefinedAttributes() {
        final List<Attribute> list = new ArrayList<>(1);
        list.add(new Attribute(Constants.LANGUAGE, "java"));
        return list;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getIconRegistry() {
        return null;
    }
}
