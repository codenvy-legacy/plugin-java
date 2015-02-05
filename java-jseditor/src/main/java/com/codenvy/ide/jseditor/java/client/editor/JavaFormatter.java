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
package com.codenvy.ide.jseditor.java.client.editor;

import com.codenvy.ide.api.text.BadLocationException;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.ext.java.jdt.text.edits.TextEdit;
import com.codenvy.ide.ext.java.jdt.text.Document;
import com.codenvy.ide.jseditor.java.client.document.FormatterDocument;
import com.codenvy.ide.jseditor.client.formatter.ContentFormatter;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;

/**
 * ContentFormatter implementation
 *
 * @author Roman Nikitenko
 */
public class JavaFormatter implements ContentFormatter, JavaParserWorker.Callback<TextEdit> {

    private JavaParserWorker javaParserWorker;
    private Document document;

    @Inject
    public JavaFormatter(JavaParserWorker javaParserWorker) {
        this.javaParserWorker = javaParserWorker;
    }

    @Override
    public void format(com.codenvy.ide.jseditor.client.document.Document document) {
        this.document = new FormatterDocument(document);

        int offset = document.getSelectedLinearRange().getStartOffset();
        int length = document.getSelectedLinearRange().getLength();

        if (length > 0 && offset >= 0) {
            javaParserWorker.format(offset, length, document.getContents(), this);
        } else {
            javaParserWorker.format(0, document.getContentsCharCount(), document.getContents(), this);
        }
    }

    @Override
    public void onCallback(TextEdit edit) {
        try {
            edit.apply(document);
        } catch (BadLocationException e) {
            Log.error(getClass(), e);
        }
    }
}
