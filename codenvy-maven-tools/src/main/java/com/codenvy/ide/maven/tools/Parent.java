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
import static java.util.Objects.requireNonNull;

/**
 * The {@literal <parent>} element contains information required to
 * locate the parent project which this project will inherit from.
 * <p/>
 * Supports next data:
 * <ul>
 * <li>artifactId</li>
 * <li>groupId</li>
 * <li>version</li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Parent {

    private String groupId;
    private String artifactId;
    private String version;

    Element element;

    public Parent(String artifactId, String groupId, String version) {
        this.artifactId = requireNonNull(artifactId);
        this.groupId = requireNonNull(groupId);
        this.version = requireNonNull(version);
    }

    Parent(Element element) {
        this.element = element;
        groupId = element.getChildText("groupId");
        artifactId = element.getChildText("artifactId");
        version = element.getChildText("version");
    }

    /**
     * Returns the artifact id of the parent project to inherit from.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the group id of the parent project to inherit from
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the version of the parent project to inherit
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the artifact id of the parent project to inherit from
     */
    public Parent setArtifactId(String artifactId) {
        this.artifactId = requireNonNull(artifactId);
        if (!isNew()) {
            element.setChildText("artifactId", artifactId, true);
        }
        return this;
    }

    /**
     * Sets the group id of the parent project to inherit from
     */
    public Parent setGroupId(String groupId) {
        this.groupId = requireNonNull(groupId);
        if (!isNew()) {
            element.setChildText("groupId", groupId, true);
        }
        return this;
    }

    /**
     * Sets the version of the parent project to inherit
     */
    public Parent setVersion(String version) {
        this.version = requireNonNull(version);
        if (!isNew()) {
            element.setChildText("version", version, true);
        }
        return this;
    }

    /**
     * Returns the id as {@literal groupId:artifactId:version}
     */
    public String getId() {
        return groupId + ':' + artifactId + ":pom:" + version;
    }

    @Override
    public String toString() {
        return getId();
    }

    void remove() {
        element.remove();
        element = null;
    }

    NewElement asNewElement() {
        final NewElement parentEl = createElement("parent");
        parentEl.appendChild(createElement("artifactId", artifactId));
        parentEl.appendChild(createElement("groupId", groupId));
        parentEl.appendChild(createElement("version", version));
        return parentEl;
    }

    private boolean isNew() {
        return element == null;
    }
}
