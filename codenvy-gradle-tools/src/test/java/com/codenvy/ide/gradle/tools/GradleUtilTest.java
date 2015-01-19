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
package com.codenvy.ide.gradle.tools;


import com.codenvy.api.project.server.Project;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.junit.Test;

import java.io.File;
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
//    @Test
    public void testShouldReturnDefaultSourceDirectories() throws Exception {
        Path workDir = Files.createTempDirectory("gradle-");
        Path buildScript = Files.write(workDir.resolve("build.gradle"), GRADLE_SCRIPT.getBytes(), StandardOpenOption.CREATE_NEW);

        List<String> sourceDirectories = GradleUtils.getSourceDirectories(workDir.toFile(), GradleUtils.SRC_DIR_TYPE.ALL);

        List<String> expectedDirectories = Arrays.asList("src/main/resources", "src/main/java", "src/test/resources", "src/test/java");

        assertEquals(sourceDirectories, expectedDirectories);
    }

    @Test
    public void testFoo() throws Exception {
        File project = new File("/home/vlad/gradle-2.2.1/samples/userguide/multiproject/dependencies/webDist/date");

        ProjectConnection gradleProjectConnection = GradleConnector.newConnector()
                                                                   .forProjectDirectory(project)
                                                                   .connect();

        try {
            GradleProject gradleProject = gradleProjectConnection.getModel(GradleProject.class);

            print(gradleProject);

            for (GradleProject childProject : gradleProject.getChildren()) {
//                System.out.println(childProject.getPath());
            }

        } finally {
            gradleProjectConnection.close();
        }
    }

    private void print(GradleProject project) {
        for (GradleProject childModule : project.getChildren()) {
            System.out.println(childModule.getPath());
            print(childModule);
        }
    }

}
