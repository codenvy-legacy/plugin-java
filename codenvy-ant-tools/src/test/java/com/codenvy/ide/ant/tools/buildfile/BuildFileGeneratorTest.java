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
package com.codenvy.ide.ant.tools.buildfile;

import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/** @author Vladyslav Zhukovskii */
public class BuildFileGeneratorTest {
    @Test
    public void testGenerateBuildFile() throws Exception {
        String genBuildFile = new BuildFileGenerator("test").getBuildFileContent();
        String testBuildFile = getTestBuildFileContent();

        assertEquals(genBuildFile, testBuildFile);
    }

    private String getTestBuildFileContent() throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(ClassLoader.getSystemResourceAsStream("test-build.xml"))) {
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine()).append("\n");
            }
        }

        return sb.toString();
    }
}
