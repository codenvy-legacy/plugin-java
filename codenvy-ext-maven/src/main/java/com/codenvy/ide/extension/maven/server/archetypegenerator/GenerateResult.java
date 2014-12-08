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
package com.codenvy.ide.extension.maven.server.archetypegenerator;

import java.io.File;

/**
 * Represents result of generating a project.
 *
 * @author Artem Zatsarynnyy
 */
public class GenerateResult {
    private final boolean success;

    private File         artifact;
    private java.io.File report;

    public GenerateResult(boolean success, File artifact, File report) {
        this.success = success;
        this.artifact = artifact;
        this.report = report;
    }

    public GenerateResult(boolean success, java.io.File report) {
        this(success, null, report);
    }

    /**
     * Reports whether build process successful or failed.
     *
     * @return {@code true} if build successful and {@code false} otherwise
     */
    public boolean isSuccessful() {
        return success;
    }

    /** Gets generated project. */
    public java.io.File getResult() {
        return artifact;
    }

    public void setResult(File artifact) {
        this.artifact = artifact;
    }

    /**
     * Reports whether build report is available or not. In case if this method returns {@code false} method {@link #getGenerateReport()}
     * always returns {@code null}.
     *
     * @return {@code true} if build report is available and {@code false} otherwise
     */
    public boolean hasGenerateReport() {
        return null != report;
    }

    /**
     * Provides report about build process. If {@code Builder} does not support reports or report for particular build is not available
     * this method always returns {@code null}.
     *
     * @return report about build or {@code null}
     */
    public java.io.File getGenerateReport() {
        return report;
    }
}
