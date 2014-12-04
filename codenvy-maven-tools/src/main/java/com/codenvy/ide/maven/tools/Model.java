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
import com.codenvy.commons.xml.ElementMapper;
import com.codenvy.commons.xml.NewElement;
import com.codenvy.commons.xml.XMLTree;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.commons.xml.NewElement.createElement;
import static com.codenvy.commons.xml.XMLTreeLocation.after;
import static com.codenvy.commons.xml.XMLTreeLocation.afterAnyOf;
import static com.codenvy.commons.xml.XMLTreeLocation.beforeAnyOf;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheBegin;
import static com.codenvy.commons.xml.XMLTreeLocation.inTheEnd;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

/**
 * The {@code <project>} element is the root of the descriptor.
 * <p/>
 * Supported next data:
 * <ul>
 * <li>modelVersion</li>
 * <li>artifactId</li>
 * <li>groupId</li>
 * <li>version</li>
 * <li>name</li>
 * <li>description</li>
 * <li>packaging</li>
 * <li>parent</li>
 * <li>build</li>
 * <li>dependencyManagement</li>
 * <li>properties</li>
 * <li>modules</li>
 * <li>dependencies</li>
 * </ul>
 * Order of elements in model based on
 * <a href="http://maven.apache.org/developers/conventions/code.html"> official recommended order</a>.
 * It means that each newly added element will be added
 * to the right place of delegated xml file - if it is possible to do so.
 *
 * @author Eugene Voeovodin
 */
public final class Model {

    public static Model readFrom(File file) throws IOException {
        return fetchModel(XMLTree.from(file));
    }

    public static Model readFrom(VirtualFile file) throws ServerException, ForbiddenException, IOException {
        return fetchModel(XMLTree.from(file.getContent().getStream()));
    }

    public static Model createModel() {
        final XMLTree tree = XMLTree.create("project");
        tree.getRoot()
            .setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0")
            .setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .setAttribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
        return new Model(tree);
    }

    private static final ToModuleMapper     TO_MODULE_MAPPER     = new ToModuleMapper();
    private static final ToDependencyMapper TO_DEPENDENCY_MAPPER = new ToDependencyMapper();

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
    private final Element root;

    private Model(XMLTree tree) {
        this.tree = tree;
        root = tree.getRoot();
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
        return artifactId;
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
        return description;
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
        return groupId;
    }

    /**
     * Get declares to which version of project descriptor this POM conforms.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Get the full name of the project.
     */
    public String getName() {
        return name;
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
        return packaging == null ? "jar" : packaging;
    }

    /**
     * Get the location of the parent project, if one exists.
     * Values from the parent
     * project will be the default for this project if
     * they are left unspecified. The location
     * is given as a group ID, artifact ID and version.
     */
    public Parent getParent() {
        return parent;
    }

    /**
     * Get the current version of the artifact produced by this project.
     */
    public String getVersion() {
        return version;
    }

    public Build getBuild() {
        return build;
    }

    public List<Dependency> getDependencies() {
        if (dependencies == null) {
            return emptyList();
        }
        return new ArrayList<>(dependencies);
    }

    /**
     * Returns returns {@link Dependencies} instance which
     * helps to manage project dependencies
     */
    public Dependencies dependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return new Dependencies(root, dependencies);
    }

    /**
     * Get default dependency information for projects that inherit
     * from this one. The
     * dependencies in this sect
     * ion are not immediately
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
     * Returns project modules
     */
    public List<String> getModules() {
        if (modules == null) {
            return emptyList();
        }
        return new ArrayList<>(modules);
    }

    /**
     * Returns project properties
     */
    public Map<String, String> getProperties() {
        if (properties == null) {
            return emptyMap();
        }
        return new HashMap<>(properties);
    }

    /**
     * Adds new module to the project
     */
    public Model addModule(String newModule) {
        requireNonNull(newModule);
        modules().add(newModule);
        //add module to xml tree
        if (root.hasChild("modules")) {
            root.getSingleChild("modules")
                .appendChild(createElement("module", newModule));
        } else {
            root.appendChild(createElement("modules", createElement("module", newModule)));
        }
        return this;
    }

    /**
     * Adds new property to the project.
     * If property with given key already exists its value
     * going to be changed with new one.
     */
    public Model addProperty(String key, String value) {
        requireNonNull(key);
        requireNonNull(value);
        addPropertyToXML(key, value);
        properties().put(key, value);
        return this;
    }

    /**
     * Removes property with given key from model.
     * If last property was removed properties will be removed as well
     */
    public Model removeProperty(String key) {
        if (properties().remove(requireNonNull(key)) != null) {
            removePropertyFromXML(key);
        }
        return this;
    }

    /**
     * Removes module from the model.
     * If last module has been removed removes modules element as well
     */
    public Model removeModule(String module) {
        if (modules().remove(requireNonNull(module))) {
            removeModuleFromXML(module);
        }
        return this;
    }

    /**
     * Sets build settings for project
     */
    public Model setBuild(Build newBuild) {
        if (build != null) {
            build.removeFromXML();
        }
        build = newBuild;
        if (newBuild != null) {
            root.appendChild(newBuild.asXMLElement());
            //associate tree element with newly added build
            build.element = root.getSingleChild("build");
        }
        return this;
    }

    /**
     * Sets the location of the parent project, if one exists.
     * <p/>
     * Values from the parent project will be
     * the default for this project if they are left unspecified.
     * The location is given as a group ID, artifact ID and version.
     */
    public Model setParent(Parent newParent) {
        if (parent != null) {
            parent.removeFromXML();
        }
        parent = newParent;
        if (newParent != null) {
            //add parent to xml tree
            root.insertChild(newParent.asXMLElement(), after("modelVersion").or(inTheBegin()));
            parent.element = root.getSingleChild("parent");
        }
        return this;
    }

    /**
     * Sets default dependency information for projects that inherit from this one.
     * If new dependency management is {@code null} removes old dependency management
     * <p/>
     * The dependencies in this section are not immediately
     * resolved. Instead, when a POM derived
     * from this one declares a dependency described by
     * a matching groupId and artifactId, the
     * version and other values from this section are
     * used for that dependency if they were not
     * already specified.
     */
    public Model setDependencyManagement(DependencyManagement newDM) {
        if (dependencyManagement != null) {
            dependencyManagement.remove();
        }
        dependencyManagement = newDM;
        if (newDM != null) {
            //insert new dependency management to tree
            root.insertChild(newDM.asXMLElement(),
                             beforeAnyOf("dependencies", "build").or(inTheEnd()));
            //associate tree element with newly added dependency management
            dependencyManagement.element = root.getSingleChild("dependencyManagement");
        }
        return this;
    }

    /**
     * Sets the modules (sometimes called sub projects) to build as a
     * part of this project. Each module listed is a relative path
     * to the directory containing the module.
     * If new modules list is an empty removes modules element from xml
     */
    public Model setModules(Collection<String> modules) {
        requireNonNull(modules);
        this.modules = new ArrayList<>(modules);
        //remove modules from tree if exist
        root.removeChild("modules");
        if (!modules.isEmpty()) {
            //insert modules to xml tree
            final NewElement newModules = createElement("modules");
            for (String module : modules) {
                newModules.appendChild(createElement("module", module));
            }
            //insert new modules to tree
            root.insertChild(newModules,
                             beforeAnyOf("properties",
                                         "dependencyManagement",
                                         "dependencies",
                                         "build").or(inTheEnd()));
        }
        return this;
    }

    /**
     * Sets properties that can be used throughout the POM as a
     * substitution, and are used as filters in resources if enabled.
     * The format is {@code <name>value</name>}.
     * If new modules list is an empty removes properties element from xml
     */
    public Model setProperties(Map<String, String> properties) {
        this.properties = new HashMap<>(requireNonNull(properties));
        //remove properties from tree if exist
        root.removeChild("properties");
        //set properties to xml tree
        if (!properties.isEmpty()) {
            final NewElement newProperties = createElement("properties");
            for (Map.Entry<String, String> property : properties.entrySet()) {
                newProperties.appendChild(createElement(property.getKey(), property.getValue()));
            }
            //insert new properties to tree
            root.insertChild(newProperties,
                             beforeAnyOf("dependencyManagement",
                                         "dependencies",
                                         "build").or(inTheEnd()));
        }
        return this;
    }

    /**
     * Sets the identifier for this artifact that is unique within the group given by the group ID.
     * If new artifactId is {@code null} removes existing artifact element from xml
     * <p/>
     * An artifact is something that is
     * either produced or used by a project.
     * Examples of artifacts produced by Maven for a
     * project include: JARs, source and binary
     * distributions, and WARs.
     */
    public Model setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        if (artifactId == null) {
            root.removeChild("artifactId");
        } else if (!root.hasChild("artifactId")) {
            root.insertChild(createElement("artifactId", artifactId),
                             afterAnyOf("groupId",
                                        "parent",
                                        "modelVersion").or(inTheBegin()));
        } else {
            tree.updateText("/project/artifactId", artifactId);
        }
        return this;
    }

    /**
     * Sets a detailed description of the project., used by Maven
     * whenever it needs to describe the project, such as on the web site.
     * If new description is {@code null} removes description element from xml
     * <p/>
     * While this element can be specified as
     * CDATA to enable the use of HTML tags within the
     * description, it is discouraged to allow
     * plain text representation. If you need to modify
     * the index page of the generated web
     * site, you are able to specify your own instead
     * of adjusting this text.
     */
    public Model setDescription(String description) {
        this.description = description;
        if (description == null) {
            root.removeChild("description");
        } else if (!root.hasChild("description")) {
            root.insertChild(createElement("description", description),
                             afterAnyOf("name",
                                        "version",
                                        "artifactId",
                                        "groupId",
                                        "parent",
                                        "modelVersion").or(inTheBegin()));
        } else {
            tree.updateText("/project/artifactId", artifactId);
        }
        return this;
    }

    /**
     * Sets a universally unique identifier for a project. It is
     * normal to use a fully-qualified package name to
     * distinguish it from other projects with a similar name (eg. <i>org.apache.maven</i>).
     * If new groupId is {@code null} removes groupId element from xml
     */
    public Model setGroupId(String groupId) {
        this.groupId = groupId;
        if (groupId == null) {
            root.removeChild("groupId");
        } else if (!root.hasChild("groupId")) {
            root.insertChild(createElement("groupId", groupId),
                             afterAnyOf("parent", "modelVersion").or(inTheBegin()));
        } else {
            tree.updateText("/project/groupId", groupId);
        }
        return this;
    }

    /**
     * Sets the current version of the artifact produced by this project.
     * If new version is {@code null} removes version element from xml
     */
    public Model setVersion(String version) {
        this.version = version;
        if (version == null) {
            root.removeChild("version");
        } else if (!root.hasChild("version")) {
            root.insertChild(createElement("version", version),
                             afterAnyOf("artifactId",
                                        "groupId",
                                        "parent",
                                        "modelVersion").or(inTheBegin()));
        } else {
            tree.updateText("/project/version", version);
        }
        return this;
    }

    /**
     * Sets declares to which version of project descriptor this POM conforms.
     * If new modelVersion is {@code null} removes modelVersion element from xml
     */
    public Model setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        if (modelVersion == null) {
            root.removeChild("modelVersion");
        } else if (!root.hasChild("modelVersion")) {
            root.insertChild(createElement("modelVersion", modelVersion), inTheBegin());
        } else {
            tree.updateText("/project/modelVersion", modelVersion);
        }
        return this;
    }

    /**
     * Sets the full name of the project.
     * If new name is {@code null} removes name element from xml
     */
    public Model setName(String name) {
        this.name = name;
        if (name == null) {
            root.removeChild("name");
        } else if (!root.hasChild("name")) {
            root.insertChild(createElement("name", name),
                             afterAnyOf("packaging",
                                        "version",
                                        "artifactId",
                                        "groupId",
                                        "parent",
                                        "modelVersion").or(inTheBegin()));
        } else {
            tree.updateText("/project/name", name);
        }
        return this;
    }

    /**
     * Set the type of artifact this project produces
     * If new packaging is {@code null} removes packaging element from xml
     * <p/>
     * For example:
     * <code>jar</code>
     * <code>war</code>
     * <code>ear</code>
     * <code>pom</code>.
     * Plugins can create their own packaging, and
     * therefore their own packaging types,
     * so this list does not contain all possible types.
     */
    public Model setPackaging(String packaging) {
        this.packaging = packaging;
        if (packaging == null) {
            root.removeChild("packaging");
        } else if (!root.hasChild("packaging")) {
            root.insertChild(createElement("packaging", packaging),
                             afterAnyOf("version",
                                        "artifactId",
                                        "groupId",
                                        "parent",
                                        "modelVersion").or(inTheBegin()));
        } else {
            tree.updateText("/project/packaging", packaging);
        }
        return this;
    }

    /**
     * @return the model id as <code>groupId:artifactId:packaging:version</code>
     */
    public String getId() {
        return (version == null ? "[inherited]" : groupId) +
               ':' +
               artifactId +
               ':' +
               packaging +
               ':' +
               (version == null ? "[inherited]" : version);
    }

    public void writeTo(File file) throws IOException {
        tree.writeTo(file);
    }

    public void writeTo(VirtualFile file) throws ServerException, ForbiddenException {
        file.updateContent(new ByteArrayInputStream(tree.getBytes()), null);
    }

    @Override
    public String toString() {
        return getId();
    }

    private Map<String, String> properties() {
        return properties == null ? properties = new HashMap<>() : properties;
    }

    private List<String> modules() {
        return modules == null ? modules = new ArrayList<>() : modules;
    }

    private void addPropertyToXML(String key, String value) {
        if (properties().containsKey(key)) {
            root.getSingleChild("properties")
                .getSingleChild(key)
                .setText(value);
        } else if (properties.isEmpty()) {
            root.appendChild(createElement("properties", createElement(key, value)));
        } else {
            root.getSingleChild("properties").appendChild(createElement(key, value));
        }
    }

    private void removeModuleFromXML(String module) {
        if (modules.isEmpty()) {
            root.removeChild("modules");
        } else {
            for (Element element : root.getChildren()) {
                if (module.equals(element.getText())) {
                    element.remove();
                }
            }
        }
    }

    private void removePropertyFromXML(String key) {
        if (properties.isEmpty()) {
            root.removeChild("properties");
        } else {
            root.getSingleChild("properties")
                .removeChild(key);
        }
    }

    private static Model fetchModel(XMLTree tree) {
        final Model model = new Model(tree);
        final Element root = tree.getRoot();
        model.modelVersion = root.getChildText("modelVersion");
        model.groupId = root.getChildText("groupId");
        model.artifactId = root.getChildText("artifactId");
        model.version = root.getChildText("version");
        model.name = root.getChildText("name");
        model.description = root.getChildText("description");
        model.packaging = root.getChildText("packaging");
        if (root.hasChild("parent")) {
            model.parent = new Parent(root.getSingleChild("parent"));
        }
        if (root.hasChild("dependencyManagement")) {
            final Element dm = tree.getSingleElement("/project/dependencyManagement");
            final List<Dependency> dependencies =
                    tree.getElements("/project/dependencyManagement/dependencies/dependency", TO_DEPENDENCY_MAPPER);
            model.dependencyManagement = new DependencyManagement(dm, dependencies);
        }
        if (root.hasChild("build")) {
            model.build = new Build(root.getSingleChild("build"));
        }
        if (root.hasChild("dependencies")) {
            model.dependencies = tree.getElements("/project/dependencies/dependency", TO_DEPENDENCY_MAPPER);
        }
        if (root.hasChild("modules")) {
            model.modules = tree.getElements("/project/modules/module", TO_MODULE_MAPPER);
        }
        if (root.hasChild("properties")) {
            model.properties = fetchProperties(root.getSingleChild("properties"));
        }
        return model;
    }

    private static Map<String, String> fetchProperties(Element propertiesElement) {
        final Map<String, String> properties = new HashMap<>();
        for (Element property : propertiesElement.getChildren()) {
            properties.put(property.getName(), property.getText());
        }
        return properties;
    }

    private static class ToDependencyMapper implements ElementMapper<Dependency> {

        @Override
        public Dependency map(Element element) {
            return new Dependency(element);
        }
    }

    private static class ToModuleMapper implements ElementMapper<String> {

        @Override
        public String map(Element element) {
            return element.getText();
        }
    }
}
