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
package com.codenvy.ide.ext.java.client.editor;

import com.codenvy.ide.api.text.BadLocationException;
import com.codenvy.ide.api.text.Document;
import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.api.text.edits.TextEdit;
import com.codenvy.ide.api.texteditor.ContentFormatter;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;

/**
 * ContentFormatter implementation
 *
 * @author Roman Nikitenko
 */
public class JavaFormatter implements ContentFormatter, JavaParserWorker.Callback<TextEdit> {

    private JavaParserWorker javaParserWorker;
    private Document         document;

    @Inject
    public JavaFormatter(JavaParserWorker javaParserWorker) {
        this.javaParserWorker = javaParserWorker;
    }

    @Override
    public void format(Document doc, Region region) {
        document = doc;
        int offset = region.getOffset();
        int length = region.getLength();
        try {
            javaParserWorker.format(offset, length, doc.get(0, doc.getLength()), this);
        } catch (BadLocationException e) {
            Log.error(getClass(), e);
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
