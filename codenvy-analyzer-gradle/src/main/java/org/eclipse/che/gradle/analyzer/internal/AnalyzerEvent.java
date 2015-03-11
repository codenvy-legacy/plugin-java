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
package org.eclipse.che.gradle.analyzer.internal;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * @author Vladyslav Zhukovskyi
 */
@EventOrigin("gradle")
public class AnalyzerEvent {
    public enum EventType {
        STARTED("started"),
        STOPPED("stopped"),
        ERROR("error"),
        MESSAGE_LOGGED("message logged");

        private String value;

        private EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class LoggedMessage {
        private String message;
        private int    lineNum;

        public LoggedMessage(String message, int lineNum) {
            this.message = message;
            this.lineNum = lineNum;
        }

        public LoggedMessage() {
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getLineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }

        @Override
        public String toString() {
            return "LoggedMessage{" +
                   "message='" + message + '\'' +
                   ", lineNum=" + lineNum +
                   '}';
        }
    }

    public static AnalyzerEvent startedEvent(long processId, String workspace, String project) {
        return new AnalyzerEvent(EventType.STARTED, processId, workspace, project);
    }

    public static AnalyzerEvent stoppedEvent(long processId, String workspace, String project) {
        return new AnalyzerEvent(EventType.STOPPED, processId, workspace, project);
    }

    public static AnalyzerEvent errorEvent(long processId, String workspace, String project, String message) {
        return new AnalyzerEvent(EventType.ERROR, processId, workspace, project, message);
    }

    public static AnalyzerEvent messageLoggedEvent(long processId, String workspace, String project, LoggedMessage message) {
        return new AnalyzerEvent(EventType.MESSAGE_LOGGED, processId, workspace, project, message);
    }

    /** Event type. */
    private EventType     type;
    /** Id of application process that produces the event. */
    private long          processId;
    /** Id of workspace that produces the event. */
    private String        workspace;
    /** Name of project that produces the event. */
    private String        project;
    /** Error message. */
    private String        error;
    /** Message associated with this event. Makes sense only for {@link EventType#MESSAGE_LOGGED} or {@link EventType#ERROR} events. */
    private LoggedMessage message;

    AnalyzerEvent(EventType type, long processId, String workspace, String project, LoggedMessage message) {
        this.type = type;
        this.processId = processId;
        this.workspace = workspace;
        this.project = project;
        this.message = message;
    }

    AnalyzerEvent(EventType type, long processId, String workspace, String project, String error) {
        this.type = type;
        this.processId = processId;
        this.workspace = workspace;
        this.project = project;
        this.error = error;
    }

    AnalyzerEvent(EventType type, long processId, String workspace, String project) {
        this.type = type;
        this.processId = processId;
        this.workspace = workspace;
        this.project = project;
    }

    public AnalyzerEvent() {
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LoggedMessage getMessage() {
        return message;
    }

    public void setMessage(LoggedMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AnalyzerEvent{" +
               "type=" + type +
               ", processId=" + processId +
               ", workspace='" + workspace + '\'' +
               ", project='" + project + '\'' +
               ", error='" + error + '\'' +
               ", message=" + message +
               '}';
    }
}
