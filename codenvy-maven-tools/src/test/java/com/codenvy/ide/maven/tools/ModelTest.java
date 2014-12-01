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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Eugene Voevodin
 */
public class ModelTest {

    @Test
    public void shouldBeAbleToCreateModel() throws Exception {
        final Model model = Model.createModel();
        model.setModelVersion("4.0.0")
             .setArtifactId("artifact-id")
             .setGroupId("group-id")
             .setVersion("x.x.x")
             .setName("name")
             .setDescription("description")
             .setDependencies(asList(new Dependency("junit", "org.junit", "x.x.x")))
             .addDependency(new Dependency("testng", "org.testng", "x.x.x"));
        final File pom = targetDir().resolve("test-pom.xml").toFile();

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <name>name</name>\n" +
                                "    <description>description</description>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <artifactId>junit</artifactId>\n" +
                                "            <groupId>org.junit</groupId>\n" +
                                "            <version>x.x.x</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <artifactId>testng</artifactId>\n" +
                                "            <groupId>org.testng</groupId>\n" +
                                "            <version>x.x.x</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToReadModelFromFile() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                   "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <name>name</name>\n" +
                   "    <description>description</description>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <artifactId>junit</artifactId>\n" +
                   "            <groupId>org.junit</groupId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>\n" +
                   "        <dependency>\n" +
                   "            <artifactId>testng</artifactId>\n" +
                   "            <groupId>org.testng</groupId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");

        final Model model = Model.readModel(pom);

        assertEquals(model.getModelVersion(), "4.0.0");
        assertEquals(model.getArtifactId(), "artifact-id");
        assertEquals(model.getGroupId(), "group-id");
        assertEquals(model.getVersion(), "x.x.x");
        assertEquals(model.getName(), "name");
        assertEquals(model.getDescription(), "description");
        assertEquals(model.getArtifactId(), "artifact-id");
        assertEquals(model.getDependencies().size(), 2);
    }

    //TODO MORE TEST!

    private String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void write(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

    private Path targetDir() throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }
}
