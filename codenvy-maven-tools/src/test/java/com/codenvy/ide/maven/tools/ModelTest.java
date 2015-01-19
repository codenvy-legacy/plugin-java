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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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
                   "        <resources>\n" +
                   "            <resource>\n" +
                   "                 <directory>${basedir}/src/main/temp</directory>\n" +
                   "            </resource>\n" +
                   "         </resources>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.getBuild()
             .setOutputDirectory(null)
             .setSourceDirectory(null)
             .setScriptSourceDirectory(null)
             .setTestOutputDirectory(null)
             .setTestSourceDirectory(null)
             .setResources(null);

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <build>\n" +
                                "    </build>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToGetBuildResources() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <build>\n" +
                   "        <resources>\n" +
                   "             <resource>\n" +
                   "                 <targetPath>META-INF/temp</targetPath>\n" +
                   "                 <filtering>true</filtering>\n" +
                   "                 <directory>${basedir}/src/main/temp</directory>\n" +
                   "                 <includes>\n" +
                   "                     <include>configuration.xml</include>\n" +
                   "                 </includes>\n" +
                   "                 <excludes>\n" +
                   "                     <exclude>**/*.properties</exclude>\n" +
                   "                 </excludes>\n" +
                   "            </resource>\n" +
                   "         </resources>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        final List<Resource> resources = model.getBuild().getResources();

        assertEquals(resources.size(), 1);
        final Resource resource = resources.get(0);
        assertEquals(resource.getTargetPath(), "META-INF/temp");
        assertTrue(resource.isFiltering());
        assertEquals(resource.getDirectory(), "${basedir}/src/main/temp");
        assertEquals(resource.getIncludes(), asList("configuration.xml"));
        assertEquals(resource.getExcludes(), asList("**/*.properties"));
    }

    @Test
    public void shouldBeAbleToAddResourcesToBuild() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <build>\n" +
                   "        <resources>\n" +
                   "            <resource>\n" +
                   "                 <directory>${basedir}/src/main/temp</directory>\n" +
                   "            </resource>\n" +
                   "         </resources>\n" +
                   "    </build>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        final List<Resource> resources = new ArrayList<>(model.getBuild().getResources());

        resources.add(new Resource().setDirectory("${basedir}/src/main/fake"));

        model.getBuild()
             .setResources(resources);
        model.save();

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <build>\n" +
                                "        <resources>\n" +
                                "            <resource>\n" +
                                "                <directory>${basedir}/src/main/temp</directory>\n" +
                                "            </resource>\n" +
                                "            <resource>\n" +
                                "                <directory>${basedir}/src/main/fake</directory>\n" +
                                "            </resource>\n" +
                                "         </resources>\n" +
                                "    </build>\n" +
                                "</project>");
        assertEquals(model.getBuild().getResources(), resources);
    }

    @Test
    public void shouldBeAbleToUpdateResources() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <build>\n" +
                   "        <resources>\n" +
                   "            <resource>\n" +
                   "                 <directory>${basedir}/src/main/temp</directory>\n" +
                   "            </resource>\n" +
                   "         </resources>\n" +
                   "    </build>\n" +
                   "</project>");


    }


    @Test
    public void shouldCreateDependenciesParentElementWhenAddingFirstDependency() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.dependencies().add(new Dependency("group-id", "artifact-id", "version"));

        model.writeTo(pom);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>group-id</groupId>\n" +
                                "            <artifactId>artifact-id</artifactId>\n" +
                                "            <version>version</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
        assertEquals(model.getDependencies().size(), 1);
    }

    @Test
    public void shouldReplaceExistingDependenciesWithNew() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <artifactId>artifact-id</artifactId>\n" +
                   "            <groupId>group-id</groupId>\n" +
                   "            <version>version</version>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.dependencies().set(asList(new Dependency("a", "b", "c")));

        model.writeTo(pom);

        assertEquals(model.getDependencies().size(), 1);
        final Dependency inserted = model.dependencies().first();
        assertEquals(inserted.getGroupId(), "a");
        assertEquals(inserted.getArtifactId(), "b");
        assertEquals(inserted.getVersion(), "c");
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>a</groupId>\n" +
                                "            <artifactId>b</artifactId>\n" +
                                "            <version>c</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }

    @Test
    public void shouldRemoveDependenciesWhenLastExistedDependencyWasRemoved() throws Exception {
        final File pom = getTestPomFile();
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

        model.dependencies().remove(model.dependencies().first());

        model.writeTo(pom);

        assertTrue(model.getDependencies().isEmpty());
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToUpdateExistingDependency() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <groupId>org.junit</groupId>\n" +
                   "            <artifactId>junit</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "            <scope>test</scope>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.dependencies()
             .first()
             .setVersion("new-version")
             .setScope(null)
             .addExclusion(new Exclusion("artifact-id", "group-id"));

        model.writeTo(pom);

        final Dependency junit = model.dependencies().first();
        assertEquals(junit.getGroupId(), "org.junit");
        assertEquals(junit.getArtifactId(), "junit");
        assertEquals(junit.getVersion(), "new-version");
        assertEquals(junit.getScope(), "compile");
        assertEquals(junit.getType(), "jar");
        assertFalse(junit.isOptional());
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.junit</groupId>\n" +
                                "            <artifactId>junit</artifactId>\n" +
                                "            <version>new-version</version>\n" +
                                "            <exclusions>\n" +
                                "                <exclusion>\n" +
                                "                    <groupId>group-id</groupId>\n" +
                                "                    <artifactId>artifact-id</artifactId>\n" +
                                "                </exclusion>\n" +
                                "            </exclusions>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }

    @Test
    public void shouldRemoveExclusionsIfLastExclusionWasRemoved() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
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
        final Model model = Model.readFrom(pom);

        final Dependency test = model.dependencies().first();

        test.removeExclusion(test.getExclusions().get(0));

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <artifactId>junit</artifactId>\n" +
                                "            <groupId>org.junit</groupId>\n" +
                                "            <version>new-version</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>");
    }


    @Test
    public void shouldBeAbleToSetParentToModelWhichDoesNotHaveIt() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setParent(new Parent("parent-group", "parent-artifact", "parent-version"));

        model.writeTo(pom);

        assertNotNull(model.getParent());
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>parent-group</groupId>\n" +
                                "        <artifactId>parent-artifact</artifactId>\n" +
                                "        <version>parent-version</version>\n" +
                                "    </parent>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToSetParentToModelWhichAlreadyHasIt() throws Exception {
        final File pom = targetDir().resolve("test-pom.xml").toFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <modelVersion>4.0.0</modelVersion>\n" +
                   "    <parent>\n" +
                   "        <groupId>new-parent-artifact</groupId>\n" +
                   "        <artifactId>new-parent-group</artifactId>\n" +
                   "        <version>new-parent-version</version>\n" +
                   "    </parent>\n" +
                   "    <artifactId>artifact-id</artifactId>\n" +
                   "    <groupId>group-id</groupId>\n" +
                   "    <version>x.x.x</version>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.setParent(new Parent("new-parent-artifact", "new-parent-group", "new-parent-version"));

        model.writeTo(pom);

        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>new-parent-artifact</groupId>\n" +
                                "        <artifactId>new-parent-group</artifactId>\n" +
                                "        <version>new-parent-version</version>\n" +
                                "    </parent>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    @Test
    public void shouldBeAbleToUpdateParent() throws Exception {
        final File pomFile = getTestPomFile();
        write(pomFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
        final Model pom = Model.readFrom(pomFile);

        final Parent parent = pom.getParent();

        parent.setArtifactId("new-parent-artifact-id")
              .setGroupId("new-parent-group-id")
              .setVersion("new-parent-version");

        pom.writeTo(pomFile);

        assertEquals(parent.getArtifactId(), "new-parent-artifact-id");
        assertEquals(parent.getGroupId(), "new-parent-group-id");
        assertEquals(parent.getVersion(), "new-parent-version");
        assertEquals(read(pomFile), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<project>\n" +
                                    "    <modelVersion>4.0.0</modelVersion>\n" +
                                    "    <parent>\n" +
                                    "        <artifactId>new-parent-artifact-id</artifactId>\n" +
                                    "        <groupId>new-parent-group-id</groupId>\n" +
                                    "        <version>new-parent-version</version>\n" +
                                    "    </parent>\n" +
                                    "    <artifactId>artifact-id</artifactId>\n" +
                                    "    <groupId>group-id</groupId>\n" +
                                    "    <version>x.x.x</version>\n" +
                                    "</project>");
    }

    @Test
    public void shouldBeAbleToRemoveParent() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
        final Model model = Model.readFrom(pom);

        model.setParent(null);

        model.writeTo(pom);

        assertNull(model.getParent());
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <artifactId>artifact-id</artifactId>\n" +
                                "    <groupId>group-id</groupId>\n" +
                                "    <version>x.x.x</version>\n" +
                                "</project>");
    }

    private File getTestPomFile() throws URISyntaxException {
        return targetDir().resolve("test-pom.xml").toFile();
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
