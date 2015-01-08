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
package com.codenvy.ide.maven.tools;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Eugene Voevodin
 */
public class ModelBuildTest extends ModelTestBase {

    @Test
    public void shouldBeAbleToSetBuildToModelWhichDoesNotHaveIt() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setBuild(new Build().setSourceDirectory("src/main/java")
                                  .setTestSourceDirectory("src/main/test"))
             .writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <build>\n" +
                                "        <sourceDirectory>src/main/java</sourceDirectory>\n" +
                                "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                                "    </build>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToSetBuildToModelWhichAlreadyHasIt() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <build>\n" +
                   "        <sourceDirectory>src/main/java</sourceDirectory>\n" +
                   "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setBuild(new Build().setSourceDirectory("src/main/groovy")
                                  .setTestSourceDirectory("src/main/test"))
             .writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <build>\n" +
                                "        <sourceDirectory>src/main/groovy</sourceDirectory>\n" +
                                "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                                "    </build>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToUpdateBuild() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <build>\n" +
                   "        <sourceDirectory>src/main/java</sourceDirectory>\n" +
                   "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.getBuild()
             .setSourceDirectory("src/main/groovy")
             .setOutputDirectory("output/path");

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <build>\n" +
                                "        <sourceDirectory>src/main/groovy</sourceDirectory>\n" +
                                "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                                "        <outputDirectory>output/path</outputDirectory>\n" +
                                "    </build>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToRemoveBuild() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <build>\n" +
                   "        <sourceDirectory>src/main/java</sourceDirectory>\n" +
                   "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setBuild(null)
             .writeTo(pom);

        assertNull(model.getBuild());
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToRemoveBuildMembers() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <build>\n" +
                   "        <sourceDirectory>src/main/java</sourceDirectory>\n" +
                   "        <testSourceDirectory>src/main/test</testSourceDirectory>\n" +
                   "        <outputDirectory>output/path</outputDirectory>\n" +
                   "        <testOutputDirectory>test/output/path</testOutputDirectory>\n" +
                   "        <scriptSourceDirectory>script/source/path</scriptSourceDirectory>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.getBuild()
             .setOutputDirectory(null)
             .setSourceDirectory(null)
             .setScriptSourceDirectory(null)
             .setTestOutputDirectory(null)
             .setTestSourceDirectory(null);

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <build>\n" +
                                "    </build>\n" +
                                "</project>");
    }
}
