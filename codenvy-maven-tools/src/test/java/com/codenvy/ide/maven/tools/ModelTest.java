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
package com.codenvy.ide.maven.tools;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Eugene Voevodin
 */
public class ModelTest extends ModelTestBase {

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
        final File pom = getTestPomFile();

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" " +
                                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                                " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 " +
                                "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <name>name</name>\n" +
                                "    <description>description</description>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>junit</groupId>\n" +
                                "            <artifactId>org.junit</artifactId>\n" +
                                "            <version>x.x.x</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>testng</groupId>\n" +
                                "            <artifactId>org.testng</artifactId>\n" +
                                "            <version>x.x.x</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToReadModelFromFile() throws Exception {
        final File pom = getTestPomFile();
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
        assertEquals(model.getDependencies().size(), 2);
        assertEquals(model.getDependencyManagement().getDependencies().size(), 1);
    }

    @Test
    public void shouldRemoveModulesIfLastModuleWasRemoved() throws Exception {
        final File pom = getTestPomFile();
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
    public void shouldBeAbleToRemoveModelMembers() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "    <packaging>war</packaging>\n" +
                   "    <name>name</name>\n" +
                   "    <description>description</description>\n" +
                   "    <modules>\n" +
                   "        <module>first</module>\n" +
                   "        <module>second</module>\n" +
                   "    </modules>\n" +
                   "    <properties>\n" +
                   "        <childKey>child</childKey>\n" +
                   "        <parentKey>parent</parentKey>\n" +
                   "    </properties>\n" +
                   "    <dependencyManagement>\n" +
                   "        <dependencies>\n" +
                   "            <dependency>\n" +
                   "                <groupId>artifact-id</groupId>\n" +
                   "                <artifactId>group-id</artifactId>\n" +
                   "                <version>version</version>\n" +
                   "            </dependency>\n" +
                   "        </dependencies>\n" +
                   "    </dependencyManagement>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <groupId>junit</groupId>\n" +
                   "            <artifactId>org.junit</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setModelVersion(null)
             .setGroupId(null)
             .setArtifactId(null)
             .setVersion(null)
             .setPackaging(null)
             .setName(null)
             .setDescription(null)
             .setDependencyManagement(null)
             .setModules(null)
             .setProperties(null)
             .dependencies()
             .set(null);

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "</project>");
    }

    @Test
    public void shouldAddCreateModuleParentElementWhenAddingNewModule() throws Exception {
        final File pom = getTestPomFile();
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
    public void shouldBeAbleToUpdateProperties() throws Exception {
        final File pomFile = getTestPomFile();
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
        final File pom = getTestPomFile();
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
        final File pom = getTestPomFile();
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
                                "                <groupId>artifact-id</groupId>\n" +
                                "                <artifactId>group-id</artifactId>\n" +
                                "                <version>version</version>\n" +
                                "            </dependency>\n" +
                                "        </dependencies>\n" +
                                "    </dependencyManagement>\n" +
                                "</project>");
    }
}
