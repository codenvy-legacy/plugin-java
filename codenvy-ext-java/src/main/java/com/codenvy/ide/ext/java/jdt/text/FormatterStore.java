/**
 * Interface for storing and managing text.
 * <p>
 * Provides access to the stored text and allows to manipulate it.
 * </p>
 * <p>
 * Clients may implement this interface or use {@link com.codenvy.ide.ext.java.jdt.text.GapTextStore} or
 * {@link com.codenvy.ide.ext.java.jdt.text.CopyOnWriteTextStore}.
 * </p>
 */
package com.codenvy.ide.ext.java.jdt.text;

import com.codenvy.ide.jseditor.client.document.Document;

/**
 * Provides access to the stored text into document and allows to manipulate it.
 *
 * @author Andrienko Alexander
 */
public class FormatterStore implements TextStore {

    public Document document;

    public FormatterStore(Document document) {
        this.document = document;
    }

    /** {@inheritDoc} */
    @Override
    public char get(int offset) {
        return document.getContents().charAt(offset);
    }

    /** {@inheritDoc} */
    @Override
    public String get(int offset, int length) {
        return document.getContentRange(offset, length);
    }

    /** {@inheritDoc} */
    @Override
    public int getLength() {
        return document.getContentsCharCount();
    }

    /** {@inheritDoc} */
    @Override
    public void replace(int offset, int length, String text) {
        document.replace(offset, length, text);
    }

    /** {@inheritDoc} */
    @Override
    public void set(String text) {
        throw new UnsupportedOperationException("This operation is unsupported!");
    }
}
