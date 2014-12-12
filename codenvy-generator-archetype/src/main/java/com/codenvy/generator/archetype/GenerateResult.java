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

import java.io.File;

/**
 * Represents result of a project generating.
 *
 * @author Artem Zatsarynnyy
 */
public class GenerateResult {
    private final boolean success;

    private File artifact;
    private File report;

    GenerateResult(boolean success, File artifact, File report) {
        this.success = success;
        this.artifact = artifact;
        this.report = report;
    }

    GenerateResult(boolean success, File report) {
        this(success, null, report);
    }

    /**
     * Reports whether project generating process successful or failed.
     *
     * @return {@code true} if project was successfully generated and {@code false} otherwise
     */
    public boolean isSuccessful() {
        return success;
    }

    /** Gets zipped file with generated project. */
    public File getResult() {
        return artifact;
    }

    void setResult(File artifact) {
        this.artifact = artifact;
    }

    /**
     * Reports whether project generating report is available or not. In case if this method
     * returns {@code false} method {@link #getGenerateReport()} always returns {@code null}.
     *
     * @return {@code true} if project generating report is available and {@code false} otherwise
     */
    public boolean hasGenerateReport() {
        return null != report;
    }

    /**
     * Provides report about project generating process. If report is not available  this method always returns {@code null}.
     *
     * @return report about project generating or {@code null}
     */
    public File getGenerateReport() {
        return report;
    }
}
