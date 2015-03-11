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

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Event which fired by client (Builder) to send result of analysis work.
 *
 * @author Vladyslav Zhukovskii
 */
@EventOrigin("gradle_client")
public class AnalysisFinishedEvent {
    private AnalyzerResponse response;

    public AnalysisFinishedEvent() {
    }

    public AnalysisFinishedEvent(AnalyzerResponse response) {
        this.response = response;
    }

    /**
     * Get {@link AnalyzerResponse} object which represent response.
     *
     * @return {@link AnalyzerResponse} object.
     */
    public AnalyzerResponse getResponse() {
        return response;
    }

    public void setResponse(AnalyzerResponse response) {
        this.response = response;
    }
}
