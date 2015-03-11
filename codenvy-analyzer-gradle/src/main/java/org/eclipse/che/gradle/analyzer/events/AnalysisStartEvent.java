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
import org.eclipse.che.gradle.analyzer.ModelRequest;

/**
 * Event which fired by server (API) to start project analysis.
 *
 * @author Vladyslav Zhukovskii
 */
@EventOrigin("gradle_server")
public class AnalysisStartEvent {

    private String       workspace;
    private String       project;
    private String       sourcesZipBallLink;
    private ModelRequest request;

    public AnalysisStartEvent() {
    }

    public AnalysisStartEvent(String workspace, String project, String sourcesZipBallLink, ModelRequest request) {
        this.workspace = workspace;
        this.project = project;
        this.sourcesZipBallLink = sourcesZipBallLink;
        this.request = request;
    }

    /**
     * Get http url link to user's project source.
     *
     * @return http url link
     */
    public String getSourcesZipBallLink() {
        return sourcesZipBallLink;
    }

    /**
     * Get user workspace name.
     *
     * @return workspace name
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Get user project path
     *
     * @return project path
     */
    public String getProject() {
        return project;
    }

    /**
     * Get type of request. Only project model building supports at this time.
     *
     * @return model type
     */
    public ModelRequest getRequest() {
        return request;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setSourcesZipBallLink(String sourcesZipBallLink) {
        this.sourcesZipBallLink = sourcesZipBallLink;
    }

    public void setRequest(ModelRequest request) {
        this.request = request;
    }
}
