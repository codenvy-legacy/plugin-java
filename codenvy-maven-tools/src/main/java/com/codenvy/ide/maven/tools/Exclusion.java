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
package com.codenvy.ide.maven.tools;

import com.codenvy.commons.xml.Element;
import com.codenvy.commons.xml.NewElement;

import static com.codenvy.commons.xml.NewElement.createElement;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheBegin;
import static java.util.Objects.requireNonNull;

/**
 * The {@literal <exclusion>} element contains information required to exclude an artifact from the project
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

    Element element;

    public Exclusion(String artifactId, String groupId) {
        this.artifactId = requireNonNull(artifactId);
        this.groupId = requireNonNull(groupId);
    }

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
    public Exclusion setArtifactId(String artifactId) {
        this.artifactId = requireNonNull(artifactId);
        if (!isNew()) {
            if (element.hasChild("artifactId")) {
                element.getSingleChild("artifactId").setText(artifactId);
            } else {
                element.appendChild(createElement("artifactId", artifactId));
            }
        }
        return this;
    }

    /**
     * Set the group ID of the project to exclude.
     */
    public Exclusion setGroupId(String groupId) {
        this.groupId = requireNonNull(groupId);
        if (!isNew()) {
            if (element.hasChild("groupId")) {
                element.getSingleChild("groupId").setText(groupId);
            } else {
                element.insertChild(createElement("groupId", groupId), inTheBegin());
            }
        }
        return this;
    }

    void remove() {
        if (!isNew()) {
            element.remove();
            element = null;
        }
    }

    NewElement asXMLElement() {
        final NewElement newExclusion = createElement("exclusion");
        newExclusion.appendChild(createElement("groupId", groupId));
        newExclusion.appendChild(createElement("artifactId", artifactId));
        return newExclusion;
    }

    private boolean isNew() {
        return element == null;
    }
}
