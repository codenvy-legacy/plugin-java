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

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.commons.xml.Element;
import com.codenvy.commons.xml.FromElementFunction;
import com.codenvy.commons.xml.XMLTree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO update tree in setters
 * The <code>&lt;project&gt;</code> element is the root of
 * the descriptor.
 * The following table lists all of the possible child elements.
 */
public class Model {

    public static Model readModel(File file) throws IOException {
        return fetchModel(XMLTree.from(file));
    }

    public static Model readModel(VirtualFile file) throws ServerException, ForbiddenException, IOException {
        return fetchModel(XMLTree.from(file.getContent().getStream()));
    }

    private static final ToDependencyFunction TO_DEPENDENCY_FUNCTION = new ToDependencyFunction();
    private static final ToExclusionFunction  TO_EXCLUSION_FUNCTION  = new ToExclusionFunction();
    private static final ToParentFunction     TO_PARENT_FUNCTION     = new ToParentFunction();
    private static final ToModuleFunction     TO_MODULE_FUNCTION     = new ToModuleFunction();
    private static final ToBuildFunction      TO_BUILD_FUNCTION      = new ToBuildFunction();
    private static final ToPropertiesFunction TO_PROPERTIES_FUNCTION = new ToPropertiesFunction();

    private String               modelVersion;
    private String               groupId;
    private String               artifactId;
    private String               version;
    private String               packaging;
    private String               name;
    private String               description;
    private Parent               parent;
    private Build                build;
    private DependencyManagement dependencyManagement;
    private Map<String, String>  properties;
    private List<String>         modules;
    private List<Dependency>     dependencies;

    private final XMLTree tree;

    public Model(XMLTree tree) {
        this.tree = tree;
        packaging = "jar";
    }

    /**
     * Get the identifier for this artifact that is unique within
     * the group given by the
     * group ID. An artifact is something that is
     * either produced or used by a project.
     * Examples of artifacts produced by Maven for a
     * project include: JARs, source and binary
     * distributions, and WARs.
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
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Get declares to which version of project descriptor this POM conforms.
     */
    public String getModelVersion() {
        return this.modelVersion;
    }

    /**
     * Get the full name of the project.
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
     */
    public Parent getParent() {
        return this.parent;
    }

    /**
     * Get the current version of the artifact produced by this
     * project.
     */
    public String getVersion() {
        return this.version;
    }

    public Build getBuild() {
        return build;
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
            this.dependencies = new
                    ArrayList<>();
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
     */
    public DependencyManagement getDependencyManagement() {
        return dependencyManagement;
    }

    /**
     * Method getModules.
     *
     * @return List
     */
    public java.util.List<String> getModules() {
        if (this.modules == null) {
            this.modules = new ArrayList<>();
        }

        return this.modules;
    }

    /**
     * Method getProperties.
     *
     * @return Properties
     */
    public Map<String, String> getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap<>();
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


    public void setBuild(Build build) {
        this.build = build;
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
    public void setProperties(Map<String, String> properties) {
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

    public void save(File file) throws IOException {
        tree.writeTo(file);
    }

    @Override
    public String toString() {
        return getId();
    }

    private static Model fetchModel(XMLTree tree) {
        final Model model = new Model(tree);
        //required
        model.setModelVersion(tree.getSingleText("/project/modelVersion"));
        model.setArtifactId(tree.getSingleText("/project/artifactId"));
        model.setGroupId(tree.getSingleText("/project/groupId"));
        model.setVersion(tree.getSingleText("/project/version"));
        //not required
        final Element root = tree.getRoot();
        if (root.hasChild("parent")) {
            model.setParent(root.getSingleChild("parent").mapTo(TO_PARENT_FUNCTION));
        }
        if (root.hasChild("name")) {
            model.setName(root.getSingleChild("name").getText());
        }
        if (root.hasChild("description")) {
            model.setDescription(root.getSingleChild("description").getText());
        }
        if (root.hasChild("packaging")) {
            model.setPackaging(root.getSingleChild("packaging").getText());
        }
        if (root.hasChild("dependencies")) {
            model.setDependencies(tree.getElements("/project/dependencies/dependency", TO_DEPENDENCY_FUNCTION));
        }
        if (root.hasChild("dependencyManagement")) {
            final DependencyManagement dm = new DependencyManagement();
            model.setDependencyManagement(dm);
            if (root.getSingleChild("dependencyManagement").hasChildren()) {
                dm.setDependencies(tree.getElements("/project/dependencyManagement/dependency", TO_DEPENDENCY_FUNCTION));
            }
        }
        if (root.hasChild("modules")) {
            final Element modules = root.getSingleChild("modules");
            model.setModules(modules.getChildren(TO_MODULE_FUNCTION));
        }
        if (root.hasChild("properties")) {
            model.setProperties(root.getSingleChild("properties").mapTo(TO_PROPERTIES_FUNCTION));
        }
        if (root.hasChild("build")) {
            model.setBuild(root.getSingleChild("build").mapTo(TO_BUILD_FUNCTION));
        }
        return model;
    }

    private static class ToDependencyFunction implements FromElementFunction<Dependency> {

        @Override
        public Dependency apply(Element element) {
            final Dependency dependency = new Dependency();
            //required
            dependency.setArtifactId(element.getSingleChild("artifactId").getText());
            dependency.setGroupId(element.getSingleChild("groupId").getText());
            dependency.setVersion(element.getSingleChild("version").getText());
            //not required
            if (element.hasChild("scope")) {
                dependency.setScope(element.getSingleChild("scope").getText());
            }
            if (element.hasChild("classifier")) {
                dependency.setClassifier(element.getSingleChild("classifier").getText());
            }
            if (element.hasChild("optional")) {
                dependency.setOptional(element.getSingleSibling("optional").getText());
            }
            if (element.hasChild("systemPath")) {
                dependency.setSystemPath(element.getSingleChild("systemPath").getText());
            }
            if (element.hasChild("type")) {
                dependency.setType(element.getSingleChild("type").getText());
            }
            if (element.hasChild("exclusions")) {
                final Element exclusions = element.getSingleChild("exclusions");
                dependency.setExclusions(exclusions.getChildren(TO_EXCLUSION_FUNCTION));
            }
            return dependency;
        }
    }

    private static class ToParentFunction implements FromElementFunction<Parent> {

        @Override
        public Parent apply(Element element) {
            final Parent parent = new Parent();
            parent.setArtifactId(element.getSingleChild("artifactId").getText());
            parent.setGroupId(element.getSingleChild("groupId").getText());
            parent.setVersion(element.getSingleChild("version").getText());
            if (element.hasChild("relativePath")) {
                parent.setRelativePath(element.getSingleChild("relativePath").getText());
            }
            return parent;
        }
    }

    private static class ToExclusionFunction implements FromElementFunction<Exclusion> {

        @Override
        public Exclusion apply(Element element) {
            final Exclusion exclusion = new Exclusion();
            exclusion.setArtifactId(element.getSingleChild("artifactId").getText());
            exclusion.setGroupId(element.getSingleChild("groupId").getText());
            return exclusion;
        }
    }

    private static class ToBuildFunction implements FromElementFunction<Build> {

        @Override
        public Build apply(Element element) {
            final Build build = new Build();
            if (element.hasChild("sourceDirectory")) {
                build.setSourceDirectory(element.getSingleChild("sourceDirectory").getText());
            }
            if (element.hasChild("testSourceDirectory")) {
                build.setSourceDirectory(element.getSingleChild("testSourceDirectory").getText());
            }
            if (element.hasChild("outputDirectory")) {
                build.setOutputDirectory(element.getSingleChild("outputDirectory").getText());
            }
            if (element.hasChild("testOutputDirectory")) {
                build.setTestOutputDirectory(element.getSingleChild("testOutputDirectory").getText());
            }
            if (element.hasChild("scriptSourceDirectory")) {
                build.setScriptSourceDirectory(element.getSingleChild("scriptSourceDirectory").getText());
            }
            return build;
        }
    }

    private static class ToModuleFunction implements FromElementFunction<String> {

        @Override
        public String apply(Element element) {
            return element.getText();
        }
    }

    private static class ToPropertiesFunction implements FromElementFunction<Map<String, String>> {

        @Override
        public Map<String, String> apply(Element propertiesElement) {
            final Map<String, String> properties = new HashMap<>();
            for (Element property : propertiesElement.getChildren()) {
                properties.put(property.getName(), property.getText());
            }
            return properties;
        }
    }
}
