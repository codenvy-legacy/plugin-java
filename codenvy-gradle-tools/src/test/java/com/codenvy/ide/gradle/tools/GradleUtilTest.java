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
package com.codenvy.ide.gradle.tools;


import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/** @author Vladyslav Zhukovskii */
public class GradleUtilTest {

    public static final String GRADLE_SCRIPT = "apply plugin: 'java'";

    //@Test disable due to long downloading gradle wrapper
    public void testShouldReturnDefaultSourceDirectories() throws Exception {
        Path workDir = Files.createTempDirectory("gradle-");
        Path buildScript = Files.write(workDir.resolve("build.gradle"), GRADLE_SCRIPT.getBytes(), StandardOpenOption.CREATE_NEW);

        List<String> sourceDirectories = GradleUtils.getSourceDirectories(buildScript.toFile());

        List<String> expectedDirectories = Arrays.asList("src/main/resources", "src/main/java", "src/test/resources", "src/test/java");

        assertEquals(sourceDirectories, expectedDirectories);
    }
}
