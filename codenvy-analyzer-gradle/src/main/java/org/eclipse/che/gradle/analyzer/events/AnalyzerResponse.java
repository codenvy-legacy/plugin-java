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
package org.eclipse.che.gradle.analyzer.events;

/**
 * Response which handle built model and various information if analysis was successful.
 *
 * @author Vladyslav Zhukovskii
 */
public class AnalyzerResponse {
    private boolean successful;
    private String  model;
    private String  errorOutput;

    public AnalyzerResponse() {
    }

    /**
     * Indicates if analysis task was successful.
     *
     * @return true if task end successfully otherwise false
     */
    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public AnalyzerResponse withSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }

    /**
     * Get result analysis model.
     *
     * @return string represent simple json with model that can be de-serialized into complex DTO object
     */
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public AnalyzerResponse withModel(String model) {
        this.model = model;
        return this;
    }

    /**
     * Get error log if analysis was failed
     *
     * @return log represent by simple String
     */
    public String getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

    public AnalyzerResponse withErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
        return this;
    }
}
