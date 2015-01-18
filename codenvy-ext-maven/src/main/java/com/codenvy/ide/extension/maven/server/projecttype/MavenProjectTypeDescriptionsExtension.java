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
package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.project.server.AttributeDescription;
import com.codenvy.api.project.server.ProjectType;
import com.codenvy.api.project.server.ProjectTypeDescriptionExtension;
import com.codenvy.api.project.server.ProjectTypeDescriptionRegistry;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenProjectTypeDescriptionsExtension implements ProjectTypeDescriptionExtension {

    @Inject
    public MavenProjectTypeDescriptionsExtension(ProjectTypeDescriptionRegistry registry) {
        registry.registerDescription(this);
    }

    @Override
    public List<ProjectType> getProjectTypes() {
        final List<ProjectType> list = new ArrayList<>(1);
        list.add(new ProjectType(MavenAttributes.MAVEN_ID, MavenAttributes.MAVEN_NAME, Constants.JAVA_CATEGORY));
        return list;
    }

    @Override
    public List<AttributeDescription> getAttributeDescriptions() {
        final List<AttributeDescription> list = new ArrayList<>();
        list.add(new AttributeDescription(Constants.LANGUAGE));
        list.add(new AttributeDescription(Constants.LANGUAGE_VERSION));
        list.add(new AttributeDescription(Constants.FRAMEWORK));

        list.add(new AttributeDescription(MavenAttributes.GROUP_ID));
        list.add(new AttributeDescription(MavenAttributes.ARTIFACT_ID));
        list.add(new AttributeDescription(MavenAttributes.VERSION));
        list.add(new AttributeDescription(MavenAttributes.PARENT_VERSION));
        list.add(new AttributeDescription(MavenAttributes.PARENT_ARTIFACT_ID));
        list.add(new AttributeDescription(MavenAttributes.PARENT_GROUP_ID));
        list.add(new AttributeDescription(MavenAttributes.PACKAGING));

        list.add(new AttributeDescription(MavenAttributes.SOURCE_FOLDER));
        list.add(new AttributeDescription(MavenAttributes.TEST_SOURCE_FOLDER));
        return list;
    }
}
