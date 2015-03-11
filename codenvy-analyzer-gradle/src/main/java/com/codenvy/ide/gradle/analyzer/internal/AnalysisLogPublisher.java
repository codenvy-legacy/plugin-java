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

import com.codenvy.api.core.notification.EventService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Vladyslav Zhukovskyi
 */
public class AnalysisLogPublisher extends OutputStream {

    private final AtomicInteger lineCounter;
    private final EventService  eventService;
    private final long          processId;
    private final String        workspace;
    private final String        project;

    private StringBuilder sb = new StringBuilder();

    public AnalysisLogPublisher(EventService eventService,
                                long processId,
                                String workspace,
                                String project) {
        lineCounter = new AtomicInteger(1);
        this.eventService = eventService;
        this.processId = processId;
        this.workspace = workspace;
        this.project = project;
    }

    @Override
    public void write(int b) throws IOException {
        if ((char)b != '\n') {
            sb.append((char)b);
            return;
        }

        String line = sb.toString();
        if (line.isEmpty()) {
            return;
        }

        AnalyzerEvent.LoggedMessage message = new AnalyzerEvent.LoggedMessage(line, lineCounter.getAndIncrement());
        eventService.publish(AnalyzerEvent.messageLoggedEvent(processId, workspace, project, message));
        sb.setLength(0);
    }
}
