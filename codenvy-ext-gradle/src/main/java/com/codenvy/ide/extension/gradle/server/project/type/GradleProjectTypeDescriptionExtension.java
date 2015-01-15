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

import com.codenvy.api.project.server.AttributeDescription;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeDescriptionExtension;
import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GradleProjectTypeDescriptionExtension implements ProjectTypeDescriptionExtension {

    @Inject
    public GradleProjectTypeDescriptionExtension(ProjectTypeDescriptionRegistry registry) {
        registry.registerDescription(this);
    }

    @Nonnull
    @Override
    public List<ProjectType> getProjectTypes() {
        final List<ProjectType> list = new ArrayList<>(1);
        list.add(new ProjectType(GradleAttributes.GRADLE_ID, GradleAttributes.GRADLE_NAME, Constants.JAVA_CATEGORY));
        return list;
    }

    @Nonnull
    @Override
    public List<AttributeDescription> getAttributeDescriptions() {
        final List<AttributeDescription> list = new ArrayList<>(4);
        list.add(new AttributeDescription(Constants.LANGUAGE));
        list.add(new AttributeDescription(GradleAttributes.SOURCE_FOLDER));
        list.add(new AttributeDescription(GradleAttributes.TEST_SOURCE_FOLDER));
        return list;
    }
}
