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

import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.event.ItemEvent;
import com.codenvy.ide.api.event.ItemHandler;
import com.codenvy.ide.api.parts.PartPresenter;
import com.codenvy.ide.api.parts.PropertyListener;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.collections.StringMap;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFileNode;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class FileWatcher {

    @Inject
    private JavaParserWorker worker;

    @Inject
    private EditorAgent editorAgent;

    @Inject
    private void handleFileOperations(EventBus eventBus) {
        eventBus.addHandler(ItemEvent.TYPE, new ItemHandler() {
            @Override
            public void onItem(ItemEvent event) {
                if (event.getOperation() == ItemEvent.ItemOperation.DELETE) {
                    if (event.getItem() instanceof SourceFileNode) {
                        String fqn = getFQN(((SourceFileNode)event.getItem()));
                        worker.removeFqnFromCache(fqn);
                        reparseAllOpenedFiles();
                    } else if (event.getItem() instanceof PackageNode) {
                        worker.removeFqnFromCache(((PackageNode)event.getItem()).getQualifiedName());
                        reparseAllOpenedFiles();
                    }

                }
            }
        });
    }


    public void editorOpened(final EditorPartPresenter editor) {
        final PropertyListener propertyListener = new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (propId == EditorPartPresenter.PROP_DIRTY) {
                    if (!editor.isDirty()) {
                        FileNode file = editor.getEditorInput().getFile();
                        String fqn = getFQN(file);
                        worker.removeFqnFromCache(fqn);
                        reparseAllOpenedFiles();
                    }
                }
            }
        };
        editor.addPropertyListener(propertyListener);
    }

    private String getFQN(FileNode file) {
        String packageName = "";
        if (file.getParent() instanceof PackageNode) {
            packageName = ((PackageNode)file.getParent()).getQualifiedName() + '.';
        }
        return packageName + file.getName().substring(0, file.getName().indexOf('.'));
    }

    private void reparseAllOpenedFiles() {
        editorAgent.getOpenedEditors().iterate(new StringMap.IterationCallback<EditorPartPresenter>() {
            @Override
            public void onIteration(final String s, final EditorPartPresenter editorPartPresenter) {
                if (editorPartPresenter instanceof EmbeddedTextEditorPresenter) {
                    final EmbeddedTextEditorPresenter editor = (EmbeddedTextEditorPresenter)editorPartPresenter;
                    editor.refreshEditor();
                }
            }
        });
    }
}
