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
package com.codenvy.ide.extension.gradle.server.project.generator;

import com.codenvy.api.core.ServerException;
import com.codenvy.ide.extension.gradle.server.project.generator.impl.SimpleProjectGenerator;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.SIMPLE_GENERATION_STRATEGY;

/**
 * Project generation provider.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class ProjectGeneratorProvider {
    private final Map<String, ProjectGenerator> services = new HashMap<>();

    @Inject
    public ProjectGeneratorProvider(Set<ProjectGenerator> generatorStrategies) {
        for (ProjectGenerator generatorStrategy : generatorStrategies) {
            services.put(generatorStrategy.getId(), generatorStrategy);
        }

        if (!services.containsKey(SIMPLE_GENERATION_STRATEGY)) { //must always be if not added in DI we add it here
            services.put(SIMPLE_GENERATION_STRATEGY, new SimpleProjectGenerator());
        }
    }

    /**
     * Get project generator by registered generator ID.
     *
     * @param id
     *         generator ID
     * @return project generator
     * @throws ServerException
     *         if project generator wasn't found
     */
    public ProjectGenerator getGenerator(String id) throws ServerException {
        if (!services.containsKey(id)) {
            throw new ServerException(String.format("Generator service '%s' not found.", id));
        }

        return services.get(id);
    }

    /**
     * Get default project generator.
     *
     * @return default project generator
     */
    public ProjectGenerator getDefaultGenerator() {
        return services.get(SIMPLE_GENERATION_STRATEGY);
    }
}
