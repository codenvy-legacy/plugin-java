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

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ModelDependenciesTest extends ModelTestBase {

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

        model.dependencies().remove();

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
    public void shouldBeAbleToSelectDependenciesByCondition() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
                   "    <dependencies>\n" +
                   "        <dependency>\n" +
                   "            <groupId>group1</groupId>\n" +
                   "            <artifactId>artifact1</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "            <scope>test</scope>\n" +
                   "        </dependency>\n" +
                   "        <dependency>\n" +
                   "            <groupId>group1</groupId>\n" +
                   "            <artifactId>artifact2</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "            <scope>test</scope>\n" +
                   "        </dependency>\n" +
                   "        <dependency>\n" +
                   "            <groupId>group2</groupId>\n" +
                   "            <artifactId>artifact1</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "            <scope>test</scope>\n" +
                   "        </dependency>\n" +
                   "        <dependency>\n" +
                   "            <groupId>group1</groupId>\n" +
                   "            <artifactId>artifact3</artifactId>\n" +
                   "            <version>x.x.x</version>\n" +
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        final Dependencies dependencies = model.dependencies()
                                               .byGroupId("group1")
                                               .byScope("test");

        assertEquals(dependencies.first().getArtifactId(), "artifact1");
        assertEquals(dependencies.last().getArtifactId(), "artifact2");
    }

    @Test
    public void shouldBeAbleToRemoveSelectedDependencies() throws Exception {
        final File pom = getTestPomFile();
        write(pom, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<project>\n" +
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
                   "        </dependency>\n" +
                   "    </dependencies>\n" +
                   "</project>");
        final Model model = Model.readFrom(pom);

        model.dependencies()
             .byScope("test")
             .remove();

        model.writeTo(pom);

        assertEquals(model.getDependencies().size(), 1);
        assertEquals(read(pom), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<project>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>test-group-id</groupId>\n" +
                                "            <artifactId>test-artifact-id</artifactId>\n" +
                                "            <version>x.x.x</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
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
}
