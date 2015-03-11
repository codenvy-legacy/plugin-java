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
package com.codenvy.ide.gradle.analyzer.internal;

import com.codenvy.api.core.util.LineConsumer;

import java.io.IOException;

/**
 * @author Vladyslav Zhukovskyi
 */
public interface AnalysisLogger extends LineConsumer {
    String getContentType();

    void getLogs(Appendable output) throws IOException;

    AnalysisLogger DUMMY = new AnalysisLogger() {
        @Override
        public void getLogs(Appendable output) {
        }

        @Override
        public String getContentType() {
            return "text/plain";
        }

        @Override
        public void writeLine(String line) {
        }

        @Override
        public void close() {
        }
    };
}
