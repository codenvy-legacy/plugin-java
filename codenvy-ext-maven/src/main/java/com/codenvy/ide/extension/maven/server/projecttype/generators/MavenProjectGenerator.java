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
package com.codenvy.ide.extension.maven.server.projecttype.generators;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.handlers.CreateProjectHandler;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author gazarenkov
 */
@Singleton
public class MavenProjectGenerator implements CreateProjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MavenProjectGenerator.class);

    private final Map<String, GeneratorStrategy> strategies = new HashMap<>();;

    @Inject
    public MavenProjectGenerator(Set<GeneratorStrategy> generatorStrategies) {
        for (GeneratorStrategy generatorStrategy : generatorStrategies) {
            strategies.put(generatorStrategy.getId(), generatorStrategy);
        }
        if (!strategies.containsKey(MavenAttributes.SIMPLE_GENERATION_STRATEGY)) { //must always be if not added in DI we add it here
            strategies.put(MavenAttributes.SIMPLE_GENERATION_STRATEGY, new SimpleGeneratorStrategy());
        }
    }

    @Override
    public String getProjectType() {
        return MavenAttributes.MAVEN_ID;
    }

    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes,
                                Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        if (options == null || options.isEmpty() || !options.containsKey("type")) {
            strategies.get(MavenAttributes.SIMPLE_GENERATION_STRATEGY).generateProject(baseFolder, attributes, options);
        } else {
            if (strategies.containsKey(options.get("type"))) {
                strategies.get(options.get("type")).generateProject(baseFolder, attributes, options);
            } else {
                String errorMsg = String.format("Generation strategy %s don't found", options.get("type"));
                LOG.warn("MavenProjectGenerator", errorMsg);
                throw new ServerException(errorMsg);
            }
        }
    }
}
