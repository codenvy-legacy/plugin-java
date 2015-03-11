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
package org.eclipse.che.gradle.analyzer;

import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.util.ServiceLoader;

/**
 * Responsible for building Gradle project model.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleModelBuilder implements ToolingModelBuilder {

    private static ServiceLoader<ModelBuilderService> buildersLoader =
            ServiceLoader.load(ModelBuilderService.class, GradleModelBuilder.class.getClassLoader());

    /** {@inheritDoc} */
    @Override
    public boolean canBuild(String modelName) {
        for (ModelBuilderService service : buildersLoader) {
            if (service.canExec(modelName)) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Object buildAll(String modelName, Project rootProject) {
        for (ModelBuilderService service : buildersLoader) {
            if (service.canExec(modelName)) {
                return service.buildModel(rootProject);
            }
        }

        return null;
    }
}
