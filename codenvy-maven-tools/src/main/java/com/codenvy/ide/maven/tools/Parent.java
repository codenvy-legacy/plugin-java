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
import static com.codenvy.commons.xml.XMLTreeLocation.after;
import static com.codenvy.commons.xml.XMLTreeLocation.before;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheBegin;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheEnd;
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
 * <li>relativePath</li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Parent {

    private String groupId;
    private String artifactId;
    private String version;
    private String relativePath;

    Element element;

    public Parent() {
    }

    public Parent(String groupId, String artifactId, String version) {
        this.groupId = requireNonNull(groupId);
        this.artifactId = requireNonNull(artifactId);
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

    public String getRelativePath() {
        return relativePath == null ? "../pom.xml" : relativePath;
    }

    /**
     * Sets the artifact id of the parent project to inherit from
     */
    public Parent setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        if (!isNew()) {
            if (artifactId == null) {
                element.removeChild("artifactId");
            } else if (element.hasChild("artifactId")) {
                element.getSingleChild("artifactId").setText(artifactId);
            } else {
                element.insertChild(createElement("artifactId", artifactId),
                                    after("groupId").or(inTheBegin()));
            }
        }
        return this;
    }

    /**
     * Sets the group id of the parent project to inherit from
     */
    public Parent setGroupId(String groupId) {
        this.groupId = groupId;
        if (!isNew()) {
            if (groupId == null) {
                element.removeChild("groupId");
            } else if (element.hasChild("groupId")) {
                element.getSingleChild("groupId").setText(groupId);
            } else {
                element.insertChild(createElement("groupId", groupId), inTheBegin());
            }
        }
        return this;
    }

    /**
     * Sets the version of the parent project to inherit
     */
    public Parent setVersion(String version) {
        this.version = version;
        if (!isNew()) {
            if (version == null) {
                element.removeChild("version");
            } else if (element.hasChild("version")) {
                element.getSingleChild("version").setText(version);
            } else {
                element.insertChild(createElement("version", version), before("relativePath").or(inTheEnd()));
            }
        }
        return this;
    }

    /**
     * Sets parent relative path
     */
    public Parent setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        if (!isNew()) {
            if (relativePath == null) {
                element.removeChild("relativePath");
            } else if (element.hasChild("relativePath")) {
                element.getSingleChild("relativePath").setText(relativePath);
            } else {
                element.appendChild(createElement("relativePath", relativePath));
            }
        }
        return this;
    }

    /**
     * Returns the id as <i>groupId:artifactId:version</i>
     */
    public String getId() {
        return groupId + ':' + artifactId + ":pom:" + version;
    }

    @Override
    public String toString() {
        return getId();
    }

    void removeFromXML() {
        if (!isNew()) {
            element.remove();
            element = null;
        }
    }

    NewElement asXMLElement() {
        final NewElement newParent = createElement("parent");
        if (groupId != null) {
            newParent.appendChild(createElement("groupId", groupId));
        }
        if (artifactId != null) {
            newParent.appendChild(createElement("artifactId", artifactId));
        }
        if (version != null) {
            newParent.appendChild(createElement("version", version));
        }
        if (relativePath != null) {
            newParent.appendChild(createElement("relativePath", relativePath));
        }
        return newParent;
    }

    private boolean isNew() {
        return element == null;
    }
}
