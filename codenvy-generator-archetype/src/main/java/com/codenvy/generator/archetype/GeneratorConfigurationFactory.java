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
package com.codenvy.generator.archetype;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/** @author Artem Zatsarynnyy */
class GeneratorConfigurationFactory {
    private final ArchetypeGenerator generator;

    GeneratorConfigurationFactory(ArchetypeGenerator generator) {
        this.generator = generator;
    }

    GeneratorConfiguration createConfiguration(String artifactId, Map<String, String> options) throws GeneratorException {
        final java.io.File projectDir = createProjectDir();
        return new GeneratorConfiguration(projectDir, createWorkDir(projectDir, artifactId), artifactId, options);
    }

    private java.io.File createProjectDir() throws GeneratorException {
        try {
            return Files.createTempDirectory(generator.getProjectsDirectory().toPath(), "project-").toFile();
        } catch (IOException e) {
            throw new GeneratorException(e);
        }
    }

    private java.io.File createWorkDir(java.io.File parent, String artifactId) throws GeneratorException {
        try {
            return Files.createDirectory(new java.io.File(parent, artifactId).toPath()).toFile();
        } catch (IOException e) {
            throw new GeneratorException(e);
        }
    }
}
