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
 * The <code>&lt;project&gt;</code> element is the root of
 * the descriptor.
 * The following table lists all of the possible child elements.
 */
public class Model {

    /**
     * Declares to which version of project descriptor this POM conforms.
     */
    private String modelVersion;

    /**
     * The location of the parent project, if one exists. Values
     * from the parent
     * project will be the default for this project if
     * they are left unspecified. The location
     * is given as a group ID, artifact ID and version.
     */
    private Parent parent;

    /**
     * A universally unique identifier for a project.
     * It is normal to
     * use a fully-qualified package name to
     * distinguish it from other
     * projects with a similar name (eg.
     * <code>org.apache.maven</code>).
     */
    private String groupId;

    /**
     * The identifier for this artifact that is unique within the
     * group given by the
     * group ID. An artifact is something that is
     * either produced or used by a project.
     * Examples of artifacts produced by Maven for a
     * project include: JARs, source and binary
     * distributions, and WARs.
     */
    private String artifactId;

    /**
     * The current version of the artifact produced by this project.
     */
    private String version;

    /**
     * The type of artifact this project produces, for
     * example <code>jar</code>
     * <code>war</code>
     * <code>ear</code>
     * <code>pom</code>.
     * Plugins can create their own packaging, and
     * therefore their own packaging types,
     * so this list does not contain all possible
     * types.
     */
    private String packaging = "jar";

    /**
     * The full name of the project.
     */
    private String name;

    /**
     * A detailed description of the project, used by Maven
     * whenever it needs to
     * describe the project, such as on the web site.
     * While this element can be specified as
     * CDATA to enable the use of HTML tags within the
     * description, it is discouraged to allow
     * plain text representation. If you need to modify
     * the index page of the generated web
     * site, you are able to specify your own instead
     * of adjusting this text.
     */
    private String description;

    /**
     * Field modules.
     */
    private java.util.List<String> modules;

    private java.util.Properties properties;

    /**
     * Default dependency information for projects that inherit
     * from this one. The
     * dependencies in this section are not immediately
     * resolved. Instead, when a POM derived
     * from this one declares a dependency described by
     * a matching groupId and artifactId, the
     * version and other values from this section are
     * used for that dependency if they were not
     * already specified.
     */
    private DependencyManagement dependencyManagement;

    private java.util.List<Dependency> dependencies;

    /**
     * Get the identifier for this artifact that is unique within
     * the group given by the
     * group ID. An artifact is something that is
     * either produced or used by a project.
     * Examples of artifacts produced by Maven for a
     * project include: JARs, source and binary
     * distributions, and WARs.
     *
     * @return String
     */
    public String getArtifactId() {
        return this.artifactId;
    }

    /**
     * Get a detailed description of the project, used by Maven
     * whenever it needs to
     * describe the project, such as on the web site.
     * While this element can be specified as
     * CDATA to enable the use of HTML tags within the
     * description, it is discouraged to allow
     * plain text representation. If you need to modify
     * the index page of the generated web
     * site, you are able to specify your own instead
     * of adjusting this text.
     *
     * @return String
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get a universally unique identifier for a project. It is
     * normal to
     * use a fully-qualified package name to
     * distinguish it from other
     * projects with a similar name (eg.
     * <code>org.apache.maven</code>).
     *
     * @return String
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Get declares to which version of project descriptor this POM
     * conforms.
     *
     * @return String
     */
    public String getModelVersion() {
        return this.modelVersion;
    }

    /**
     * Get the full name of the project.
     *
     * @return String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the type of artifact this project produces, for example
     * <code>jar</code>
     * <code>war</code>
     * <code>ear</code>
     * <code>pom</code>.
     * Plugins can create their own packaging, and
     * therefore their own packaging types,
     * so this list does not contain all possible
     * types.
     *
     * @return String
     */
    public String getPackaging() {
        return this.packaging;
    }

    /**
     * Get the location of the parent project, if one exists.
     * Values from the parent
     * project will be the default for this project if
     * they are left unspecified. The location
     * is given as a group ID, artifact ID and version.
     *
     * @return Parent
     */
    public Parent getParent() {
        return this.parent;
    }

    /**
     * Get the current version of the artifact produced by this
     * project.
     *
     * @return String
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Method addDependency.
     */
    public void addDependency(Dependency dependency) {
        getDependencies().add(dependency);
    }

    /**
     * Method addModule.
     */
    public void addModule(String string) {
        getModules().add(string);
    }

    /**
     * Method addProperty.
     */
    public void addProperty(String key, String value) {
        getProperties().put(key, value);
    }

    /**
     * Method getDependencies.
     *
     * @return List
     */
    public java.util.List<Dependency> getDependencies() {
        if (this.dependencies == null) {
            this.dependencies = new java.util.ArrayList<>();
        }

        return this.dependencies;
    }

    /**
     * Get default dependency information for projects that inherit
     * from this one. The
     * dependencies in this section are not immediately
     * resolved. Instead, when a POM derived
     * from this one declares a dependency described by
     * a matching groupId and artifactId, the
     * version and other values from this section are
     * used for that dependency if they were not
     * already specified.
     *
     * @return DependencyManagement
     */
    public DependencyManagement getDependencyManagement() {
        return this.dependencyManagement;
    }

    /**
     * Method getModules.
     *
     * @return List
     */
    public java.util.List<String> getModules() {
        if (this.modules == null) {
            this.modules = new java.util.ArrayList<>();
        }

        return this.modules;
    }

    /**
     * Method getProperties.
     *
     * @return Properties
     */
    public java.util.Properties getProperties() {
        if (this.properties == null) {
            this.properties = new java.util.Properties();
        }
        return this.properties;
    }

    /**
     * Method removeDependency.
     */
    public void removeDependency(Dependency dependency) {
        getDependencies().remove(dependency);
    }

    /**
     * Method removeModule.
     */
    public void removeModule(String string) {
        getModules().remove(string);
    }

    /**
     * Set this element describes all of the dependencies
     * associated with a
     * project.
     * These dependencies are used to construct a
     * classpath for your
     * project during the build process. They are
     * automatically downloaded from the
     * repositories defined in this project.
     * See <a
     * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a> for more information.
     */
    public void setDependencies(java.util.List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Set default dependency information for projects that inherit
     * from this one. The
     * dependencies in this section are not immediately
     * resolved. Instead, when a POM derived
     * from this one declares a dependency described by
     * a matching groupId and artifactId, the
     * version and other values from this section are
     * used for that dependency if they were not
     * already specified.
     */
    public void setDependencyManagement(DependencyManagement dependencyManagement) {
        this.dependencyManagement = dependencyManagement;
    }

    /**
     * Set the modules (sometimes called subprojects) to build as a
     * part of this
     * project. Each module listed is a relative path
     * to the directory containing the module.
     */
    public void setModules(java.util.List<String> modules) {
        this.modules = modules;
    }

    /**
     * Set properties that can be used throughout the POM as a
     * substitution, and
     * are used as filters in resources if enabled.
     * The format is
     * <code>&lt;name&gt;value&lt;/name&gt;</code>.
     */
    public void setProperties(java.util.Properties properties) {
        this.properties = properties;
    }

    /**
     * Set the identifier for this artifact that is unique within
     * the group given by the
     * group ID. An artifact is something that is
     * either produced or used by a project.
     * Examples of artifacts produced by Maven for a
     * project include: JARs, source and binary
     * distributions, and WARs.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set a detailed description of the project, used by Maven
     * whenever it needs to
     * describe the project, such as on the web site.
     * While this element can be specified as
     * CDATA to enable the use of HTML tags within the
     * description, it is discouraged to allow
     * plain text representation. If you need to modify
     * the index page of the generated web
     * site, you are able to specify your own instead
     * of adjusting this text.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set a universally unique identifier for a project. It is
     * normal to
     * use a fully-qualified package name to
     * distinguish it from other
     * projects with a similar name (eg.
     * <code>org.apache.maven</code>).
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Set declares to which version of project descriptor this POM
     * conforms.
     */
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    /**
     * Set the full name of the project.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the type of artifact this project produces, for example
     * <code>jar</code>
     * <code>war</code>
     * <code>ear</code>
     * <code>pom</code>.
     * Plugins can create their own packaging, and
     * therefore their own packaging types,
     * so this list does not contain all possible
     * types.
     */
    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    /**
     * Set the location of the parent project, if one exists.
     * Values from the parent
     * project will be the default for this project if
     * they are left unspecified. The location
     * is given as a group ID, artifact ID and version.
     */
    public void setParent(Parent parent) {
        this.parent = parent;
    }

    /**
     * Set the current version of the artifact produced by this project.
     */
    public void setVersion(String version) {
        this.version = version;
    } //-- void setVersion( String )


    private void cloneHook(Model copy) {
        copy.pomFile = pomFile;
    }

    /**
     * The POM from which this model originated. This is transient runtime state and therefore not managed by Modello.
     */
    private java.io.File pomFile;

    /**
     * Gets the POM file for the corresponding project (if any).
     *
     * @return The POM file from which this model originated or {@code null} if this model does not belong to a local
     * project (e.g. describes the metadata of some artifact from the repository).
     */
    public java.io.File getPomFile() {
        return pomFile;
    }

    public void setPomFile(java.io.File pomFile) {
        this.pomFile = (pomFile != null) ? pomFile.getAbsoluteFile() : null;
    }

    /**
     * Gets the base directory for the corresponding project (if any).
     *
     * @return The base directory for the corresponding project or {@code null} if this model does not belong to a local
     * project (e.g. describes the metadata of some artifact from the repository).
     */
    public java.io.File getProjectDirectory() {
        return (pomFile != null) ? pomFile.getParentFile() : null;
    }

    /**
     * @return the model id as <code>groupId:artifactId:packaging:version</code>
     */
    public String getId() {
        return ((getGroupId() == null) ? "[inherited]" : getGroupId()) + ':' +
               getArtifactId() + ':' +
               getPackaging() + ':' +
               ((getVersion() == null) ? "[inherited]" : getVersion());
    }

    @Override
    public String toString() {
        return getId();
    }
}
