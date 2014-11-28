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
 * <li>relativePath</li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Parent {

    private String  groupId;
    private String  artifactId;
    private String  version;
    private String  relativePath;
    private Element element;

    public Parent() {
    }

    Parent(Element element) {
        this.element = element;
        groupId = element.getChildText("groupId");
        artifactId = element.getChildText("artifactId");
        version = element.getChildText("version");
        relativePath = element.getChildTextOrDefault("relativePath", "../pom.xml");
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
     * Get the relative path of the parent {@literal pom.xml} file
     * within the check out. If not specified,
     * it defaults to {@literal ../pom.xml}.
     * <p/>
     * Maven looks for the parent POM first
     * in this location on the filesystem, then the
     * local repository, and  lastly in the remote repo.
     * {@code relativePath} allows you to select a different location,
     * For example when your structure is flat,
     * or deeper without an intermediate parent POM.
     * However, the group ID, artifact ID and
     * version are still required, and must match
     * the file in the location given or it will
     * revert to the repository for the POM.
     * This feature is only for enhancing the
     * development in a local checkout of that project.
     * Set the value to an empty string in case you
     * want to disable the feature and always resolve
     * the parent POM from the repositories.
     */
    public String getRelativePath() {
        return relativePath;
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
    public void setArtifactId(String artifactId) {
        this.artifactId = requireNonNull(artifactId);
        if (!isNew()) {
            element.setChildText("artifactId", artifactId, true);
        }
    }

    /**
     * Sets the group id of the parent project to inherit from
     */
    public void setGroupId(String groupId) {
        this.groupId = requireNonNull(groupId);
        if (!isNew()) {
            element.setChildText("groupId", groupId, true);
        }
    }

    /**
     * Sets the relative path of the parent {@literal pom.xml}
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = requireNonNull(relativePath);
        if (!isNew()) {
            element.setChildText("relativePath", relativePath, true);
        }
    }

    /**
     * Sets the version of the parent project to inherit
     */
    public void setVersion(String version) {
        this.version = requireNonNull(version);
        if (!isNew()) {
            element.setChildText("version", version, true);
        }
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

    void setElement(Element element) {
        this.element = element;
    }

    //TODO check for required children
    NewElement toNewElement() {
        final NewElement parentEl = createElement("parent");
        parentEl.appendChild(createElement("artifactId", artifactId));
        parentEl.appendChild(createElement("groupId", groupId));
        parentEl.appendChild(createElement("version", version));
        if (relativePath != null && !"../pom.xml".equals(relativePath)) {
            parentEl.appendChild(createElement("artifactId", artifactId));
        }
        return parentEl;
    }

    private boolean isNew() {
        return element == null;
    }
}
