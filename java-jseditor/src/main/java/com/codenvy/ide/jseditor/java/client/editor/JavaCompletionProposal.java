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

import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.ext.java.messages.Change;
import com.codenvy.ide.ext.java.messages.ProposalAppliedMessage;
import com.codenvy.ide.jseditor.client.codeassist.Completion;
import com.codenvy.ide.jseditor.client.codeassist.CompletionProposal;
import com.codenvy.ide.jseditor.client.document.EmbeddedDocument;
import com.codenvy.ide.jseditor.client.text.LinearRange;

import elemental.dom.Element;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 */
public class JavaCompletionProposal implements CompletionProposal {

    private final String id;
    private final String display;
    private final Icon icon;
    private final JavaParserWorker worker;

    public JavaCompletionProposal(final String id, final String display, final Icon icon, final JavaParserWorker worker) {
        this.id = id;
        this.display = display;
        this.icon = icon;
        this.worker = worker;
    }

    /** {@inheritDoc} */
    @Override
    public Element getAdditionalProposalInfo() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayString() {
        return display;
    }

    /** {@inheritDoc} */
    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void getCompletion(final CompletionCallback callback) {
        worker.applyCAProposal(id, new JavaParserWorker.ApplyCallback() {
            @Override
            public void onApply(final ProposalAppliedMessage message) {
                callback.onCompletion(new CompletionImpl(message.changes(), message.selectionRegion()));
            }
        });
    }

    private class CompletionImpl implements Completion {

        private final Array<Change> changes;
        private final com.codenvy.ide.ext.java.messages.Region region;

        private CompletionImpl(final Array<Change> changes, final com.codenvy.ide.ext.java.messages.Region region) {
            this.changes = changes;
            this.region = region;
        }

        /** {@inheritDoc} */
        @Override
        public void apply(final EmbeddedDocument document) {
            for (final Change change : changes.asIterable()) {
                document.replace(change.offset(), change.length(), change.text());
            }
        }

        /** {@inheritDoc} */
        @Override
        public LinearRange getSelection(final EmbeddedDocument document) {
            if (region == null) {
                return null;
            } else {
                return LinearRange.createWithStart(region.getOffset()).andLength(region.getLength());
            }
        }
    }
}
