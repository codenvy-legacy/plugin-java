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
package com.codenvy.ide.maven.tools;

/**
 * The <code>&lt;exclusion&gt;</code> element contains
 * information required to exclude an artifact to the project.
 */
public class Exclusion {

    /**
     * The artifact ID of the project to exclude.
     */
    private String artifactId;

    /**
     * The group ID of the project to exclude.
     */
    private String groupId;

    /**
     * Get the artifact ID of the project to exclude.
     */
    public String getArtifactId() {
        return this.artifactId;
    }

    /**
     * Get the group ID of the project to exclude.
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Set the artifact ID of the project to exclude.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set the group ID of the project to exclude.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
