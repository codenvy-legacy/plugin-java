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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertNotNull;

/**
 * @author Eugene Voevodin
 */
public abstract class ModelTestBase {

    protected File getTestPomFile() throws URISyntaxException {
        return targetDir().resolve("test-pom.xml").toFile();
    }

    protected String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    protected void write(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes());
    }

    protected Path targetDir() throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }
}
