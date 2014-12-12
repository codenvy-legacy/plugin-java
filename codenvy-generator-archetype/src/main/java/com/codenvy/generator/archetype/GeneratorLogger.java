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
package com.codenvy.generator.archetype;

import com.codenvy.api.core.util.LineConsumer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Logger that will write to file all the logs of the project generating process.
 *
 * @author Artem Zatsarynnyy
 */
class GeneratorLogger implements LineConsumer {
    private final java.io.File file;
    private final Writer       writer;
    private final boolean      autoFlush;

    GeneratorLogger(java.io.File file) throws IOException {
        this.file = file;
        autoFlush = true;
        writer = Files.newBufferedWriter(file.toPath(), Charset.defaultCharset());
    }

    /**
     * Get Reader of generator's log.
     *
     * @return reader
     * @throws java.io.IOException
     *         if any i/o errors occur
     */
    Reader getReader() throws IOException {
        return Files.newBufferedReader(file.toPath(), Charset.defaultCharset());
    }

    /** Get {@code File} where logs stored. */
    java.io.File getFile() {
        return file;
    }

    @Override
    public void writeLine(String line) throws IOException {
        if (line != null) {
            writer.write(line);
        }
        writer.write('\n');
        if (autoFlush) {
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public String toString() {
        return "GeneratorLogger{" +
               "file=" + file +
               '}';
    }
}