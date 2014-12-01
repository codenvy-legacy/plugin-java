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

import java.util.GregorianCalendar;
import java.util.Objects;

import static com.codenvy.commons.xml.NewElement.createElement;
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

    public Exclusion() {
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
            element.setChildText("artifactId", artifactId, true);
        }
        return this;
    }

    /**
     * Set the group ID of the project to exclude.
     */
    public Exclusion setGroupId(String groupId) {
        this.groupId = requireNonNull(groupId);
        if (!isNew()) {
            element.setChildText("groupId", groupId, true);
        }
        return this;
    }

    void remove() {
        if (!isNew()) {
            element.remove();
            element = null;
        }
    }

    NewElement asNewElement() {
        final NewElement newExclusion = createElement("exclusion");
        newExclusion.appendChild(createElement("artifactId", artifactId));
        newExclusion.appendChild(createElement("groupId", groupId));
        return newExclusion;
    }

    private boolean isNew() {
        return element == null;
    }
}
