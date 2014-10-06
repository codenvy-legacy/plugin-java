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
package com.codenvy.ide.jseditor.java.client.editor;

import java.util.logging.Logger;

import javax.inject.Inject;

import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.editor.EditorProvider;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.ext.java.client.editor.FileSaveWatcher;
import com.codenvy.ide.jseditor.client.defaulteditor.DefaultEditorProvider;
import com.codenvy.ide.jseditor.client.editorconfig.TextEditorConfiguration;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.codenvy.ide.texteditor.TextEditorPresenter;

/** EditorProvider that provides a text editor configured for java source files. */
public class JsJavaEditorProvider implements EditorProvider {

    private static final Logger LOG = Logger.getLogger(JsJavaEditorProvider.class.getName());

    private final DefaultEditorProvider editorProvider;
    private final FileSaveWatcher watcher;
    private final JsJavaEditorConfigurationFactory jsJavaEditorConfigurationFactory;
    private final NotificationManager notificationManager;


    @Inject
    public JsJavaEditorProvider(final DefaultEditorProvider editorProvider,
                                final FileSaveWatcher watcher,
                                final JsJavaEditorConfigurationFactory jsJavaEditorConfigurationFactory,
                                final NotificationManager notificationManager) {
        this.editorProvider = editorProvider;
        this.watcher = watcher;
        this.jsJavaEditorConfigurationFactory = jsJavaEditorConfigurationFactory;
        this.notificationManager = notificationManager;
    }

    @Override
    public String getId() {
        return "JavaEditor";
    }

    @Override
    public String getDescription() {
        return "Java Editor";
    }

    @Override
    public EditorPartPresenter getEditor() {
        LOG.fine("JsJavaEditor instance creation.");

        final EditorPartPresenter textEditor = editorProvider.getEditor();

        if (textEditor instanceof TextEditorPresenter) {
            //could chain with JavaEditorProvider ?
            LOG.fine("\t classic implmeentation, no dedicated configuration available.");

        } else if (textEditor instanceof EmbeddedTextEditorPresenter) {
            LOG.fine("\t jseditor implementation, configuring editor");
            final EmbeddedTextEditorPresenter editor = (EmbeddedTextEditorPresenter) textEditor;
            final TextEditorConfiguration configuration =
                                    this.jsJavaEditorConfigurationFactory.create(editor);
            editor.initialize(configuration, this.notificationManager);
        }
        watcher.editorOpened(textEditor);
        return textEditor;
    }

}
