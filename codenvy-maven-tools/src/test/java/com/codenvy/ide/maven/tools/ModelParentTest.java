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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Eugene Voevodin
 */
public class ModelParentTest extends ModelTestBase {

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
}
