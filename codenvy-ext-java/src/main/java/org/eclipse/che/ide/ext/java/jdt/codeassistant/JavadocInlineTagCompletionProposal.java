/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdt.codeassistant;

import org.eclipse.che.ide.ext.java.jdt.core.CompletionProposal;
import org.eclipse.che.ide.ext.java.jdt.text.Document;
import org.eclipse.che.ide.runtime.Assert;


/** Completions of inline tags such as &#x7b;&#x40;link &#x7d;. See {@link CompletionProposal#JAVADOC_INLINE_TAG}. */
public final class JavadocInlineTagCompletionProposal extends LazyJavaCompletionProposal {
    /** Triggers for types in javadoc. Do not modify. */
    protected static final char[] JDOC_INLINE_TAG_TRIGGERS = new char[]{'#', '}', ' '};

    public JavadocInlineTagCompletionProposal(CompletionProposal proposal, JavaContentAssistInvocationContext context) {
        super(proposal, context);
        Assert.isTrue(isInJavadoc());
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeReplacementString()
     */
    @Override
    protected String computeReplacementString() {
        String replacement = super.computeReplacementString();
        // TODO respect the auto-close preference, but do so consistently with method completions
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=113544
        //      if (!autocloseBrackets() && replacement.endsWith("}")) //$NON-NLS-1$
        // return replacement.substring(0, replacement.length() - 1);
        return replacement;
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal#apply(org.eclipse.jface.text.IDocument, char, int)
     */
    @Override
    public void apply(Document document, char trigger, int offset) {
        // TODO respect the auto-close preference, but do so consistently with method completions
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=113544
        // boolean needsLinkedMode= autocloseBrackets();
        boolean needsLinkedMode = true;
        if (needsLinkedMode)
            setCursorPosition(getCursorPosition() - 1); // before the closing curly brace

        super.apply(document, trigger, offset);

        if (needsLinkedMode)
            setUpLinkedMode(document, '}');
    }
}
