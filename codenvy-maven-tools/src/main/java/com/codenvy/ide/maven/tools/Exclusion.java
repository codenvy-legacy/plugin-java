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
import com.codenvy.commons.xml.NewElement;

import static com.codenvy.commons.xml.NewElement.createElement;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheBegin;

/**
 * The <i>../dependency/exclusions/exclusion</i> element contains
 * information required to exclude an artifact from the project
 * <p/>
 * Supported next data:
 * <ul>
 * <li>artifactId</li>
 * <li>groupId</li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Exclusion {

    private String artifactId;
    private String groupId;

    Element exclusionElement;

    public Exclusion(String artifactId, String groupId) {
        this.artifactId = artifactId;
        this.groupId = groupId;
    }

    Exclusion(Element element) {
        exclusionElement = element;
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
    public Exclusion setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        if (!isNew()) {
            if (artifactId == null) {
                exclusionElement.removeChild("artifactId");
            } else if (exclusionElement.hasSingleChild("artifactId")) {
                exclusionElement.getSingleChild("artifactId").setText(artifactId);
            } else {
                exclusionElement.appendChild(createElement("artifactId", artifactId));
            }
        }
        return this;
    }

    /**
     * Set the group ID of the project to exclude.
     */
    public Exclusion setGroupId(String groupId) {
        this.groupId = groupId;
        if (!isNew()) {
            if (groupId == null) {
                exclusionElement.removeChild("groupId");
            } else if (exclusionElement.hasSingleChild("groupId")) {
                exclusionElement.getSingleChild("groupId").setText(groupId);
            } else {
                exclusionElement.insertChild(createElement("groupId", groupId), inTheBegin());
            }
        }
        return this;
    }

    void remove() {
        if (!isNew()) {
            exclusionElement.remove();
            exclusionElement = null;
        }
    }

    NewElement asXMLElement() {
        final NewElement newExclusion = createElement("exclusion");
        newExclusion.appendChild(createElement("groupId", groupId));
        newExclusion.appendChild(createElement("artifactId", artifactId));
        return newExclusion;
    }

    private boolean isNew() {
        return exclusionElement == null;
    }
}
