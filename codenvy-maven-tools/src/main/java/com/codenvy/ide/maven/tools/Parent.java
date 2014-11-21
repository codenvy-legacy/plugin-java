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
 * The <code>&lt;parent&gt;</code> element contains
 * information required to locate the parent project from which
 * this project will inherit from.
 * <strong>Note:</strong> The children of this element are
 * not interpolated and must be given as literal values.
 */
public class Parent {

    /**
     * The group id of the parent project to inherit from.
     */
    private String groupId;

    /**
     * The artifact id of the parent project to inherit from.
     */
    private String artifactId;

    /**
     * The version of the parent project to inherit.
     */
    private String version;

    /**
     * The relative path of the parent
     * <code>pom.xml</code> file within the check out.
     * If not specified, it defaults to
     * <code>../pom.xml</code>.
     * Maven looks for the parent POM first in this
     * location on
     * the filesystem, then the local repository, and
     * lastly in the remote repo.
     * <code>relativePath</code> allows you to select a
     * different location,
     * for example when your structure is flat, or
     * deeper without an intermediate parent POM.
     * However, the group ID, artifact ID and version
     * are still required,
     * and must match the file in the location given or
     * it will revert to the repository for the POM.
     * This feature is only for enhancing the
     * development in a local checkout of that project.
     * Set the value to an empty string in case you
     * want to disable the feature and always resolve
     * the parent POM from the repositories.
     */
    private String relativePath = "../pom.xml";

    /**
     * Get the artifact id of the parent project to inherit from.
     */
    public String getArtifactId() {
        return this.artifactId;
    }

    /**
     * Get the group id of the parent project to inherit from.
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Get the relative path of the parent <code>pom.xml</code>
     * file within the check out.
     * If not specified, it defaults to
     * <code>../pom.xml</code>.
     * Maven looks for the parent POM first in this
     * location on
     * the filesystem, then the local repository, and
     * lastly in the remote repo.
     * <code>relativePath</code> allows you to select a
     * different location,
     * for example when your structure is flat, or
     * deeper without an intermediate parent POM.
     * However, the group ID, artifact ID and version
     * are still required,
     * and must match the file in the location given or
     * it will revert to the repository for the POM.
     * This feature is only for enhancing the
     * development in a local checkout of that project.
     * Set the value to an empty string in case you
     * want to disable the feature and always resolve
     * the parent POM from the repositories.
     */
    public String getRelativePath() {
        return this.relativePath;
    }

    /**
     * Get the version of the parent project to inherit.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Set the artifact id of the parent project to inherit from.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set the group id of the parent project to inherit from.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Set the relative path of the parent <code>pom.xml</code>
     * file within the check out.
     * If not specified, it defaults to
     * <code>../pom.xml</code>.
     * Maven looks for the parent POM first in this
     * location on
     * the filesystem, then the local repository, and
     * lastly in the remote repo.
     * <code>relativePath</code> allows you to select a
     * different location,
     * for example when your structure is flat, or
     * deeper without an intermediate parent POM.
     * However, the group ID, artifact ID and version
     * are still required,
     * and must match the file in the location given or
     * it will revert to the repository for the POM.
     * This feature is only for enhancing the
     * development in a local checkout of that project.
     * Set the value to an empty string in case you
     * want to disable the feature and always resolve
     * the parent POM from the repositories.
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    /**
     * Set the version of the parent project to inherit.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the id as <code>groupId:artifactId:version</code>
     */
    public String getId() {
        return getGroupId() + ':' + getArtifactId() + ":pom:" + getVersion();
    }

    @Override
    public String toString() {
        return getId();
    }
}
