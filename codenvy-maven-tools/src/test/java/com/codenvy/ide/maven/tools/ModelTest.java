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
import static org.testng.Assert.assertTrue;

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
             .dependencies()
             .set(asList(new Dependency("junit", "org.junit", "x.x.x")))
             .add(new Dependency("testng", "org.testng", "x.x.x"));
        final File pom = targetDir().resolve("test-pom.xml").toFile();

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                                "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
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
                   "    <modules>\n" +
                   "        <module>first</module>\n" +
                   "        <module>second</module>\n" +
                   "    </modules>\n" +
                   "    <dependencyManagement>\n" +
                   "        <dependencies>\n" +
                   "            <dependency>\n" +
                   "                <groupId>junit</groupId>\n" +
                   "                <artifactId>junit</artifactId>\n" +
                   "                <version>3.8</version>\n" +
                   "            </dependency>\n" +
                   "        </dependencies>\n" +
                   "    </dependencyManagement>\n" +
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

        final Model model = Model.readFrom(pom);

        assertEquals(model.getModelVersion(), "4.0.0");
        assertEquals(model.getArtifactId(), "artifact-id");
        assertEquals(model.getGroupId(), "group-id");
        assertEquals(model.getVersion(), "x.x.x");
        assertEquals(model.getName(), "name");
        assertEquals(model.getDescription(), "description");
        assertEquals(model.getArtifactId(), "artifact-id");
        assertEquals(model.getPackaging(), "jar");
        assertEquals(model.getModules(), asList("first", "second"));
        assertEquals(model.dependencies().count(), 2);
        assertEquals(model.getDependencyManagement().dependencies().count(), 1);
    }

    @Test
    public void shouldRemoveDependenciesWhenLastDependencyWasRemoved() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <artifactId>junit</artifactId>\n" +
                   "            <groupId>org.junit</groupId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        final Dependency junit = model.dependencies().first();
        model.dependencies().remove(junit);

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    @Test
    public void shouldRemoveModulesIfLastModuleWasRemoved() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <modules>\n" +
                   "        <module>first</module>\n" +
                   "        <module>second</module>\n" +
                   "    </modules>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.removeModule("first");
        model.removeModule("second");

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
        assertTrue(model.getModules().isEmpty());
    }

    @Test
    public void shouldAddCreateModuleParentElementWhenAddingNewModule() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.addModule("new-module");

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <modules>\n" +
                                "        <module>new-module</module>\n" +
                                "    </modules>\n" +
                                "</project>");
        assertEquals(model.getModules(), asList("new-module"));
    }

    @Test
    public void shouldCreateDependenciesParentElementWhenAddingNewDependency() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.dependencies().add(new Dependency("artifact-id", "group-id", "version"));

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <artifactId>artifact-id</artifactId>\n" +
                                "            <groupId>group-id</groupId>\n" +
                                "            <version>version</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToSetParent() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setParent(new Parent("parent-artifact", "parent-group", "parent-version"));

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <artifactId>parent-artifact</artifactId>\n" +
                                "        <groupId>parent-group</groupId>\n" +
                                "        <version>parent-version</version>\n" +
                                "    </parent>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToSetBuild() throws Exception {
        final File pomFile = targetDir().resolve("test-pom.xml").toFile();
        write(pomFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                       "<project>\n" +
                       "    <modelVersion>4.0.0</modelVersion>\n" +
                       "    <artifactId>artifact-id</artifactId>\n" +
                       "    <groupId>group-id</groupId>\n" +
                       "    <version>x.x.x</version>\n" +
                       "</project>");
        final Model pom = Model.readFrom(pomFile);

        pom.setBuild(new Build().setSourceDirectory("src/main/java")
                                .setTestSourceDirectory("src/main/test"))
           .writeTo(pomFile);

        assertEquals(read(pomFile), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
    public void shouldBeAbleToUpdateExistingDependency() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <artifactId>junit</artifactId>\n" +
                   "            <groupId>org.junit</groupId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.dependencies()
             .first()
             .setVersion("new-version")
             .addExclusion(new Exclusion("artifact-id", "group-id"));

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <artifactId>junit</artifactId>\n" +
                                "            <groupId>org.junit</groupId>\n" +
                                "            <version>new-version</version>\n" +
                                "            <exclusions>\n" +
                                "                <exclusion>\n" +
                                "                    <artifactId>artifact-id</artifactId>\n" +
                                "                    <groupId>group-id</groupId>\n" +
                                "                </exclusion>\n" +
                                "            </exclusions>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToSelectDependencies() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <artifactId>junit</artifactId>\n" +
                   "            <groupId>org.junit</groupId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "            <scope>test</scope>\n" +
                   "        </dependency>\n" +
                   "        <dependency>\n" +
                   "            <groupId>org.testng</groupId>\n" +
                   "            <artifactId>testng</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "            <scope>test</scope>\n" +
                   "        </dependency>\n" +
                   "        <dependency>\n" +
                   "            <groupId>test-group-id</groupId>\n" +
                   "            <artifactId>test-artifact-id</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        final Dependencies dependencies = model.dependencies()
                                               .byScope("test");

        assertEquals(dependencies.first().getArtifactId(), "junit");
        assertEquals(dependencies.last().getArtifactId(), "testng");
    }

    @Test
    public void shouldBeAbleToUpdateProperties() throws Exception {
        final File pomFile = targetDir().resolve("test-pom.xml").toFile();
        write(pomFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                       "<project>\n" +
                       "    <modelVersion>4.0.0</modelVersion>\n" +
                       "    <artifactId>artifact-id</artifactId>\n" +
                       "    <groupId>group-id</groupId>\n" +
                       "    <version>x.x.x</version>\n" +
                       "    <properties>\n" +
                       "        <childKey>child</childKey>\n" +
                       "        <parentKey>parent</parentKey>\n" +
                       "    </properties>\n" +
                       "</project>");
        final Model pom = Model.readFrom(pomFile);

        pom.addProperty("childKey", "new-child");
        pom.addProperty("newProperty", "new-property");

        pom.writeTo(pomFile);
        assertEquals(read(pomFile), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<project>\n" +
                                    "    <modelVersion>4.0.0</modelVersion>\n" +
                                    "    <artifactId>artifact-id</artifactId>\n" +
                                    "    <groupId>group-id</groupId>\n" +
                                    "    <version>x.x.x</version>\n" +
                                    "    <properties>\n" +
                                    "        <childKey>new-child</childKey>\n" +
                                    "        <parentKey>parent</parentKey>\n" +
                                    "        <newProperty>new-property</newProperty>\n" +
                                    "    </properties>\n" +
                                    "</project>");
    }

    @Test
    public void shouldRemovePropertiesWhenLastPropertyWasRemoved() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <properties>\n" +
                   "        <childKey>child</childKey>\n" +
                   "    </properties>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.removeProperty("childKey");

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToSetDependencyManagement() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        final DependencyManagement dm = new DependencyManagement();
        dm.dependencies().add(new Dependency("artifact-id", "group-id", "version"));
        model.setDependencyManagement(dm);

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <dependencyManagement>\n" +
                                "        <dependencies>\n" +
                                "            <dependency>\n" +
                                "                <artifactId>artifact-id</artifactId>\n" +
                                "                <groupId>group-id</groupId>\n" +
                                "                <version>version</version>\n" +
                                "            </dependency>\n" +
                                "        </dependencies>\n" +
                                "    </dependencyManagement>\n" +
                                "</project>");
    }

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
