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
package org.eclipse.che.gradle.server.project.type;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.ext.java.server.projecttype.JavaProjectType;

import static org.eclipse.che.gradle.shared.GradleAttributes.DISTRIBUTION_TYPE;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_NAME;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;

/**
 * Registration of new project type.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleProjectType extends ProjectType {

    @Inject
    public GradleProjectType(GradleValueProviderFactory valueProviderFactory,
                             JavaProjectType javaProjectType) {
        super(GRADLE_ID, GRADLE_NAME, true, false);

        addVariableDefinition(SOURCE_FOLDER, "", false, valueProviderFactory);
        addVariableDefinition(TEST_FOLDER, "", false, valueProviderFactory);
        addVariableDefinition(DISTRIBUTION_TYPE, "", true, valueProviderFactory);

        addParent(javaProjectType);
        setDefaultBuilder(GRADLE_ID);
    }
}
