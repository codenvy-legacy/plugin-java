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
package com.codenvy.ide.extension.maven.server.archetypegenerator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generator configuration for particular generating process.
 *
 * @author Artem Zatsarynnyy
 */
class GeneratorConfiguration {
    private final java.io.File        buildDir;
    private final java.io.File        workDir;
    private final String              artifactId;
    private final Map<String, String> options;

    GeneratorConfiguration(java.io.File buildDir, java.io.File workDir, String artifactId, Map<String, String> options) {
        this.buildDir = buildDir;
        this.workDir = workDir;
        this.artifactId = artifactId;
        this.options = options;
    }

    java.io.File getBuildDir() {
        return buildDir;
    }

    java.io.File getWorkDir() {
        return workDir;
    }

    String getArtifactId() {
        return artifactId;
    }

    Map<String, String> getOptions() {
        return new LinkedHashMap<>(options);
    }

    @Override
    public String toString() {
        return "GeneratorConfiguration{" +
               "workDir=" + workDir +
               '}';
    }
}
