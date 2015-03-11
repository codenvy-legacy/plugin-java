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


import com.codenvy.api.runner.RunnerException;

/**
 * @author Vladyslav Zhukovskyi
 */
public interface AnalysisProcess {
    interface Callback {
        void started(AnalysisProcess process);

        void stopped(AnalysisProcess process);

        void error(AnalysisProcess process, Throwable t);
    }

    Long getId();

    AnalysisProcess getAnalysisProcess();

    void stop(boolean cancelled) throws AnalyzerException;

    void setError(Throwable error);

    Throwable getError();

    AnalysisLogger getLogger() throws AnalyzerException;

    AnalyzeRequest getRequest();

    boolean isCancelled();

    void start() throws AnalyzerException;

}
