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

import static java.lang.Boolean.parseBoolean;

/**
 * The <code>&lt;dependency&gt;</code> element contains
 * information about a dependency of the project.
 */
public class Dependency {

    /**
     * The project group that produced the dependency,
     * e.g.
     * <code>org.apache.maven</code>.
     */
    private String groupId;

    /**
     * The unique id for an artifact produced by the
     * project group, e.g.
     * <code>maven-artifact</code>.
     */
    private String artifactId;

    /**
     * The version of the dependency, e.g.
     * <code>3.2.1</code>. In Maven 2, this can also be
     * specified as a range of versions.
     */
    private String version;

    /**
     * The type of dependency. This defaults to
     * <code>jar</code>. While it
     * usually represents the extension on the filename
     * of the dependency,
     * that is not always the case. A type can be
     * mapped to a different
     * extension and a classifier.
     * The type often correspongs to the packaging
     * used, though this is also
     * not always the case.
     * Some examples are <code>jar</code>,
     * <code>war</code>, <code>ejb-client</code>
     * and <code>test-jar</code>.
     * New types can be defined by plugins that set
     * <code>extensions</code> to <code>true</code>, so
     * this is not a complete list.
     */
    private String type = "jar";

    /**
     * The classifier of the dependency. This allows
     * distinguishing two artifacts
     * that belong to the same POM but were built
     * differently, and is appended to
     * the filename after the version. For example,
     * <code>jdk14</code> and <code>jdk15</code>.
     */
    private String classifier;

    /**
     * The scope of the dependency -
     * <code>compile</code>, <code>runtime</code>,
     * <code>test</code>, <code>system</code>, and
     * <code>provided</code>. Used to
     * calculate the various classpaths used for
     * compilation, testing, and so on.
     * It also assists in determining which artifacts
     * to include in a distribution of
     * this project. For more information, see
     * <a
     * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a>.
     */
    private String scope;

    /**
     * FOR SYSTEM SCOPE ONLY. Note that use of this
     * property is <b>discouraged</b>
     * and may be replaced in later versions. This
     * specifies the path on the filesystem
     * for this dependency.
     * Requires an absolute path for the value, not
     * relative.
     * Use a property that gives the machine specific
     * absolute path,
     * e.g. <code>${java.home}</code>.
     */
    private String systemPath;

    /**
     * Field exclusions.
     */
    private java.util.List<Exclusion> exclusions;

    /**
     * Indicates the dependency is optional for use of
     * this library. While the
     * version of the dependency will be taken into
     * account for dependency calculation if the
     * library is used elsewhere, it will not be passed
     * on transitively. Note: While the type
     * of this field is <code>String</code> for
     * technical reasons, the semantic type is actually
     * <code>Boolean</code>. Default value is
     * <code>false</code>.
     */
    private String optional;

    /**
     * Method addExclusion.
     */
    public void addExclusion(Exclusion exclusion) {
        getExclusions().add(exclusion);
    }


    /**
     * Get the unique id for an artifact produced by the project
     * group, e.g.
     * <code>maven-artifact</code>.
     *
     * @return String
     */
    public String getArtifactId() {
        return this.artifactId;
    }

    /**
     * Get the classifier of the dependency. This allows
     * distinguishing two artifacts
     * that belong to the same POM but were built
     * differently, and is appended to
     * the filename after the version. For example,
     * <code>jdk14</code> and <code>jdk15</code>.
     *
     * @return String
     */
    public String getClassifier() {
        return this.classifier;
    }

    /**
     * Method getExclusions.
     */
    public java.util.List<Exclusion> getExclusions() {
        if (this.exclusions == null) {
            this.exclusions = new java.util.ArrayList<>();
        }

        return this.exclusions;
    }

    /**
     * Get the project group that produced the dependency, e.g.
     * <code>org.apache.maven</code>.
     *
     * @return String
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Get indicates the dependency is optional for use of this
     * library. While the
     * version of the dependency will be taken into
     * account for dependency calculation if the
     * library is used elsewhere, it will not be passed
     * on transitively. Note: While the type
     * of this field is <code>String</code> for
     * technical reasons, the semantic type is actually
     * <code>Boolean</code>. Default value is
     * <code>false</code>.
     *
     * @return String
     */
    public String getOptional() {
        return this.optional;
    }

    /**
     * Get the scope of the dependency - <code>compile</code>,
     * <code>runtime</code>,
     * <code>test</code>, <code>system</code>, and
     * <code>provided</code>. Used to
     * calculate the various classpaths used for
     * compilation, testing, and so on.
     * It also assists in determining which artifacts
     * to include in a distribution of
     * this project. For more information, see
     * <a
     * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a>.
     *
     * @return String
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Get fOR SYSTEM SCOPE ONLY. Note that use of this property is
     * <b>discouraged</b>
     * and may be replaced in later versions. This
     * specifies the path on the filesystem
     * for this dependency.
     * Requires an absolute path for the value, not
     * relative.
     * Use a property that gives the machine specific
     * absolute path,
     * e.g. <code>${java.home}</code>.
     *
     * @return String
     */
    public String getSystemPath() {
        return this.systemPath;
    }

    /**
     * Get the type of dependency. This defaults to
     * <code>jar</code>. While it
     * usually represents the extension on the filename
     * of the dependency,
     * that is not always the case. A type can be
     * mapped to a different
     * extension and a classifier.
     * The type often correspongs to the packaging
     * used, though this is also
     * not always the case.
     * Some examples are <code>jar</code>,
     * <code>war</code>, <code>ejb-client</code>
     * and <code>test-jar</code>.
     * New types can be defined by plugins that set
     * <code>extensions</code> to <code>true</code>, so
     * this is not a complete list.
     *
     * @return String
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the version of the dependency, e.g. <code>3.2.1</code>.
     * In Maven 2, this can also be
     * specified as a range of versions.
     *
     * @return String
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Method removeExclusion.
     */
    public void removeExclusion(Exclusion exclusion) {
        getExclusions().remove(exclusion);
    }

    /**
     * Set the unique id for an artifact produced by the project
     * group, e.g.
     * <code>maven-artifact</code>.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set the classifier of the dependency. This allows
     * distinguishing two artifacts
     * that belong to the same POM but were built
     * differently, and is appended to
     * the filename after the version. For example,
     * <code>jdk14</code> and <code>jdk15</code>.
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Set lists a set of artifacts that should be excluded from
     * this dependency's
     * artifact list when it comes to calculating
     * transitive dependencies.
     */
    public void setExclusions(java.util.List<Exclusion> exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * Set the project group that produced the dependency, e.g.
     * <code>org.apache.maven</code>.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Set indicates the dependency is optional for use of this
     * library. While the
     * version of the dependency will be taken into
     * account for dependency calculation if the
     * library is used elsewhere, it will not be passed
     * on transitively. Note: While the type
     * of this field is <code>String</code> for
     * technical reasons, the semantic type is actually
     * <code>Boolean</code>. Default value is
     * <code>false</code>.
     */
    public void setOptional(String optional) {
        this.optional = optional;
    }

    /**
     * Set the scope of the dependency - <code>compile</code>,
     * <code>runtime</code>,
     * <code>test</code>, <code>system</code>, and
     * <code>provided</code>. Used to
     * calculate the various classpaths used for
     * compilation, testing, and so on.
     * It also assists in determining which artifacts
     * to include in a distribution of
     * this project. For more information, see
     * <a
     * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a>.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Set fOR SYSTEM SCOPE ONLY. Note that use of this property is
     * <b>discouraged</b>
     * and may be replaced in later versions. This
     * specifies the path on the filesystem
     * for this dependency.
     * Requires an absolute path for the value, not
     * relative.
     * Use a property that gives the machine specific
     * absolute path,
     * e.g. <code>${java.home}</code>.
     */
    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

    /**
     * Set the type of dependency. This defaults to
     * <code>jar</code>. While it
     * usually represents the extension on the filename
     * of the dependency,
     * that is not always the case. A type can be
     * mapped to a different
     * extension and a classifier.
     * The type often correspongs to the packaging
     * used, though this is also
     * not always the case.
     * Some examples are <code>jar</code>,
     * <code>war</code>, <code>ejb-client</code>
     * and <code>test-jar</code>.
     * New types can be defined by plugins that set
     * <code>extensions</code> to <code>true</code>, so
     * this is not a complete list.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Set the version of the dependency, e.g. <code>3.2.1</code>.
     * In Maven 2, this can also be
     * specified as a range of versions.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isOptional() {
        return parseBoolean(optional);
    }

    public void setOptional(boolean optional) {
        this.optional = String.valueOf(optional);
    }

    /**
     * @return the management key as <code>groupId:artifactId:type</code>
     */
    public String getManagementKey() {
        return groupId + ":" + artifactId + ":" + type + (classifier != null ? ":" + classifier : "");
    }

    @Override
    public String toString() {
        return "Dependency {groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", type=" + type + "}";
    }
}
