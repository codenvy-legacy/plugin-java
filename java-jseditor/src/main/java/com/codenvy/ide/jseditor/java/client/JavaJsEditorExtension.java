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
package com.codenvy.ide.jseditor.java.client;

import javax.inject.Inject;

import com.codenvy.ide.api.editor.EditorRegistry;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.filetypes.FileType;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.jseditor.java.client.editor.JsJavaEditorProvider;
import com.google.inject.name.Named;

@Extension(title = "Java JS Editor", version = "3.1.0")
public class JavaJsEditorExtension {

    @Inject
    public JavaJsEditorExtension(final EditorRegistry editorRegistry,
                                 final @Named("JavaFileType") FileType javaFile,
                                 final JsJavaEditorProvider javaEditorProvider,
                                 final JavaResources javaResources) {
        // register editor provider
        editorRegistry.registerDefaultEditor(javaFile, javaEditorProvider);

        javaResources.css().ensureInjected();
    }
}
