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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
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
 * to the right place of delegated xml file - when it is possible to do so.
 *
 * @author Eugene Voeovodin
 *
 * TODO doc
 */
public final class Model {

    public static Model readFrom(InputStream is) throws IOException {
        return fetchModel(XMLTree.from(is));
    }

    public static Model readFrom(File file) throws IOException {
        if (file.isDirectory()) {
            return readFrom(new File(file, "pom.xml"));
        }
        return fetchModel(XMLTree.from(file)).setPomFile(file);
    }

    public static Model readFrom(Path path) throws IOException {
        return fetchModel(XMLTree.from(path)).setPomFile(path.toFile());
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
    private Dependencies         dependencies;
    private File                 pom;

    private final XMLTree tree;
    private final Element root;

    private Model(XMLTree tree) {
        this.tree = tree;
        root = tree.getRoot();
    }

    /**
     * Get the identifier for this artifact that is unique within
     * the group given by the group ID. An artifact is something that is
     * either produced or used by a project. Examples of artifacts produced by
     * Maven for a project include: JARs, source and binary distributions, and WARs.
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
        return dependencies().get();
    }

    /**
     * Returns returns {@link Dependencies} instance which
     * helps to manage project dependencies
     */
    public Dependencies dependencies() {
        if (dependencies == null) {
            dependencies = new Dependencies(root);
        }
        return dependencies;
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
        if (root.hasSingleChild("modules")) {
            root.getSingleChild("modules")
                .appendChild(createElement("module", newModule));
        } else {
            root.insertChild(createElement("modules", createElement("module", newModule)),
                             beforeAnyOf("dependencyManagement",
                                         "dependencies",
                                         "build").or(inTheEnd()));
        }
        return this;
    }

    /**
     * Adds new property to the project.
     * If property with given key already exists its value
     * going to be changed with new one.
     */
    public Model addProperty(String key, String value) {
        requireNonNull(key, "Property key should not be null");
        requireNonNull(value, "Property value should not be null");
        addPropertyToXML(key, value);
        properties().put(key, value);
        return this;
    }

    /**
     * Removes property with given key from model.
     * If last property was removed properties will be removed as well
     */
    public Model removeProperty(String key) {
        if (properties().remove(requireNonNull(key, "Property key should not be null")) != null) {
            removePropertyFromXML(key);
        }
        return this;
    }

    /**
     * Removes module from the model.
     * If last module has been removed removes modules element as well
     */
    public Model removeModule(String module) {
        if (modules().remove(requireNonNull(module, "Required not null module"))) {
            removeModuleFromXML(module);
        }
        return this;
    }

    /**
     * Sets build settings for project
     */
    public Model setBuild(Build newBuild) {
        //disable current build
        if (build != null) {
            build.buildElement = null;
        }
        //set up new build
        build = newBuild;
        if (build == null) {
            root.removeChild("build");
        } else if (root.hasSingleChild("build")) {
            //replace build
            build.buildElement = root.getSingleChild("build").replaceWith(build.asXMLElement());
        } else {
            //add build
            root.appendChild(build.asXMLElement());
            build.buildElement = root.getSingleChild("build");
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
        //disable current parent
        if (parent != null) {
            parent.parentElement = null;
        }
        //set up new parent
        parent = newParent;
        if (parent == null) {
            root.removeChild("parent");
        } else if (root.hasSingleChild("parent")) {
            //replace parent
            parent.parentElement = root.getSingleChild("parent").replaceWith(parent.asXMLElement());
        } else {
            //add parent
            root.insertChild(parent.asXMLElement(), after("modelVersion").or(inTheBegin()));
            parent.parentElement = root.getSingleChild("parent");
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
        //disable current dependency management
        if (dependencyManagement != null) {
            dependencyManagement.dmElement = null;
        }
        //set up new dependency management
        dependencyManagement = newDM;
        if (dependencyManagement == null) {
            root.removeChild("dependencyManagement");
        } else if (root.hasSingleChild("dependencyManagement")) {
            dependencyManagement.dmElement = root.getSingleChild("dependencyManagement").replaceWith(newDM.asXMLElement());
        } else {
            root.insertChild(dependencyManagement.asXMLElement(), beforeAnyOf("dependencies",
                                                                              "build").or(inTheEnd()));
            dependencyManagement.dmElement = root.getSingleChild("dependencyManagement");
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
        if (modules == null || modules.isEmpty()) {
            removeModules();
        } else {
            setModules0(modules);
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
        if (properties == null || properties.isEmpty()) {
            removeProperties();
        } else {
            setProperties0(properties);
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
        } else if (!root.hasSingleChild("artifactId")) {
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
        } else if (!root.hasSingleChild("description")) {
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
        } else if (!root.hasSingleChild("groupId")) {
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
        } else if (!root.hasSingleChild("version")) {
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
     * Declares to which version of project descriptor this POM conforms.
     * If new modelVersion is {@code null} removes modelVersion element from xml
     */
    public Model setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        if (modelVersion == null) {
            root.removeChild("modelVersion");
        } else if (!root.hasSingleChild("modelVersion")) {
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
        } else if (!root.hasSingleChild("name")) {
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
        } else if (!root.hasSingleChild("packaging")) {
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
     * Directly sets pom file to model.
     *
     * @param pom
     *         pom file
     */
    public Model setPomFile(File pom) {
        this.pom = pom;
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

    /**
     * Returns model pom file if model has been created
     * with {@link #readFrom(Path)} or {@link #readFrom(File)} methods.
     * <p/>
     * This method doesn't guarantee to return actual model pom file
     * because it may be set directly with {@link #setPomFile(File)}
     *
     * @return model pom file or {@code null} if it was not associated yet
     */
    public File getPomFile() {
        return pom;
    }

    /**
     * Returns pom file parent if model is associated with any pom file.
     *
     * @return pom file parent or {@code null} if model has not been associated with any pom file
     */
    public File getProjectDirectory() {
        return pom == null ? null : pom.getParentFile();
    }

    /**
     * Writes model to output stream.
     * Doesn't close the stream
     *
     * @param os
     *         stream to write model in
     * @throws IOException
     *         when any i/o error occurs
     */
    public void writeTo(OutputStream os) throws IOException {
        tree.writeTo(os);
    }

    /**
     * Writes model to given file.
     *
     * @param file
     *         file to write model in
     * @throws IOException
     *         when any i/o error occurs
     */
    public void writeTo(File file) throws IOException {
        tree.writeTo(file);
    }

    /**
     * Updates virtual file content
     *
     * @param file
     *         virtual file which content should be updated
     */
    public void writeTo(VirtualFile file) throws ServerException, ForbiddenException {
        file.updateContent(new ByteArrayInputStream(tree.getBytes()), null);
    }

    /**
     * Updates associated with model pom file content
     */
    public void save() throws IOException {
        if (pom == null) {
            throw new IllegalStateException("Model is not associated with any pom file");
        }
        writeTo(pom);
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
            root.insertChild(createElement("properties", createElement(key, value)),
                             beforeAnyOf("dependencyManagement",
                                         "dependencies",
                                         "build").or(inTheEnd()));
        } else {
            root.getSingleChild("properties").appendChild(createElement(key, value));
        }
    }

    private void removeProperties() {
        root.removeChild("properties");
        this.properties = null;
    }

    private void setProperties0(Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
        //if properties element exists we should replace it children
        //with new set of properties, otherwise create element for it
        if (root.hasSingleChild("properties")) {
            final Element propertiesElement = root.getSingleChild("properties");
            for (Element property : propertiesElement.getChildren()) {
                property.remove();
            }
            for (Map.Entry<String, String> property : properties.entrySet()) {
                propertiesElement.appendChild(createElement(property.getKey(), property.getValue()));
            }
        } else {
            final NewElement newProperties = createElement("properties");
            for (Map.Entry<String, String> property : properties.entrySet()) {
                newProperties.appendChild(createElement(property.getKey(), property.getValue()));
            }
            //insert new properties to xml
            root.insertChild(newProperties,
                             beforeAnyOf("dependencyManagement",
                                         "dependencies",
                                         "build").or(inTheEnd()));
        }
    }

    private void removeModules() {
        root.removeChild("modules");
        this.modules = null;
    }

    private void setModules0(Collection<String> modules) {
        this.modules = new ArrayList<>(modules);
        //if modules element exists we should replace it children
        //with new set of modules, otherwise create element for it
        if (root.hasSingleChild("modules")) {
            final Element modulesElement = root.getSingleChild("modules");
            //remove all modules
            for (Element module : modulesElement.getChildren()) {
                module.remove();
            }
            //append each new module to "modules" element
            for (String module : modules) {
                modulesElement.appendChild(createElement("module", module));
            }
        } else {
            final NewElement newModules = createElement("modules");
            for (String module : modules) {
                newModules.appendChild(createElement("module", module));
            }
            root.insertChild(newModules, beforeAnyOf("properties",
                                                     "dependencyManagement",
                                                     "dependencies",
                                                     "build").or(inTheEnd()));
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
            root.getSingleChild("properties").removeChild(key);
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
        if (root.hasSingleChild("parent")) {
            model.parent = new Parent(root.getSingleChild("parent"));
        }
        if (root.hasSingleChild("dependencyManagement")) {
            final Element dm = tree.getSingleElement("/project/dependencyManagement");
            final List<Dependency> dependencies =
                    tree.getElements("/project/dependencyManagement/dependencies/dependency", TO_DEPENDENCY_MAPPER);
            model.dependencyManagement = new DependencyManagement(dm, dependencies);
        }
        if (root.hasSingleChild("build")) {
            model.build = new Build(root.getSingleChild("build"));
        }
        if (root.hasSingleChild("dependencies")) {
            final List<Dependency> dependencies = tree.getElements("/project/dependencies/dependency", TO_DEPENDENCY_MAPPER);
            model.dependencies = new Dependencies(root, dependencies);
        }
        if (root.hasSingleChild("modules")) {
            model.modules = tree.getElements("/project/modules/module", TO_MODULE_MAPPER);
        }
        if (root.hasSingleChild("properties")) {
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
