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

package com.codenvy.ide.ext.java.client.editor;

import elemental.json.Json;
import elemental.json.JsonObject;

import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.projecttree.VirtualFile;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class OpenDeclarationFinder {

    private final JavaParserWorker      worker;
    private final EditorAgent           editorAgent;
    private final JavaNavigationService service;

    @Inject
    public OpenDeclarationFinder(JavaParserWorker worker, EditorAgent editorAgent, JavaNavigationService service) {
        this.worker = worker;
        this.editorAgent = editorAgent;
        this.service = service;
    }

    public void openDeclaration() {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor == null) {
            return;
        }

        if (!(activeEditor instanceof EmbeddedTextEditorPresenter)) {
            Log.error(getClass(), "Quick Document support only EmbeddedTextEditorPresenter as editor");
            return;
        }
        EmbeddedTextEditorPresenter editor = ((EmbeddedTextEditorPresenter)activeEditor);
        int offset = editor.getCursorOffset();
        final VirtualFile file = editor.getEditorInput().getFile();
        worker.computeJavadocHandle(offset, file.getPath(), new JavaParserWorker.Callback<String>() {
            @Override
            public void onCallback(String result) {
                if (result != null) {
                    sendRequest(result, file.getProject());
                }
            }
        });
    }

    private void sendRequest(String bindingKey, ProjectNode project) {
        service.findDeclaration(project.getPath(), bindingKey, new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            @Override
            protected void onSuccess(String result) {
                parseResult(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(OpenDeclarationFinder.class, exception);
            }
        });
    }

    private void parseResult(String result) {
        Log.error(getClass(), result);
        if(result == null){
            return;
        }
        JsonObject object = Json.parse(result);

    }
}
