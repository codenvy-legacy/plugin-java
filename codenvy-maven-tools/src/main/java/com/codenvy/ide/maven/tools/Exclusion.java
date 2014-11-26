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

import com.codenvy.commons.xml.Element;

/**
 * The <code>&lt;exclusion&gt;</code> element contains
 * information required to exclude an artifact to the project.
 */
public class Exclusion {

    private String  artifactId;
    private String  groupId;
    private Element element;

    public Exclusion() {}

    Exclusion(Element element) {
        this.element = element;
        artifactId = element.getChildText("artifactId");
        groupId = element.getChildText("groupId");
    }

    /**
     * Get the artifact ID of the project to exclude.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the group ID of the project to exclude.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set the artifact ID of the project to exclude.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        //TODO use element
    }

    /**
     * Set the group ID of the project to exclude.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
        //TODO use element
    }
}
