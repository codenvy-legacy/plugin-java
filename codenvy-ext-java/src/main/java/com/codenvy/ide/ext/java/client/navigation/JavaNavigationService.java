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

package com.codenvy.ide.ext.java.client.navigation;

import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.codenvy.ide.rest.AsyncRequestCallback;

/**
 * @author Evgen Vidolob
 */
public interface JavaNavigationService {

    /**
     * Find declaration of the binding key
     * @param projectPath path to the project
     * @param keyBinding binding key
     * @param callback
     */
    void findDeclaration(String projectPath, String keyBinding, AsyncRequestCallback<String> callback);


    /**
     * Receive all jar dependency's
     * @param projectPath path to the project
     * @param callback
     */
    void getExternalLibraries(String projectPath, AsyncRequestCallback<Array<Jar>> callback);

    void getLibraryChildren(String projectPath, int libId, AsyncRequestCallback<Array<JarEntry>> callback);

    void getChildren(String projectPath, int libId, String path, AsyncRequestCallback<Array<JarEntry>> callback);

}
