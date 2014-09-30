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
package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;

/**
 * @author Evgen Vidolob
 */
public class MavenProjectGenerator {

    public static void generateProjectStructure(FolderEntry baseFolder) throws ConflictException, ForbiddenException, ServerException {
        FolderEntry src = baseFolder.createFolder("src");
        FolderEntry main = src.createFolder("main");
        FolderEntry mainJava = main.createFolder("java");
        FolderEntry test = src.createFolder("test");
        FolderEntry testJava = test.createFolder("java");
    }

}
