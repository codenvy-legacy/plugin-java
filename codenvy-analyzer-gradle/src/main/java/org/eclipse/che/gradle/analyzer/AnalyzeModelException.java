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
package org.eclipse.che.gradle.analyzer;

/**
 * Represent exception which can be throwed during project model building.
 * Wraps Gradle's few default runtime exceptions.
 *
 * @author Vladyslav Zhukovskii
 */
public class AnalyzeModelException extends Exception {
    private String errStdOutput;

    public AnalyzeModelException(String errStdOutput) {
        this.errStdOutput = errStdOutput;
    }

    /**
     * Get Gradle build log.
     *
     * @return log contained string
     */
    public String getErrStdOutput() {
        return errStdOutput;
    }
}
