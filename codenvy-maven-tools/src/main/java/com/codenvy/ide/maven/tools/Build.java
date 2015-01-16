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
import com.codenvy.commons.xml.ElementMapper;
import com.codenvy.commons.xml.NewElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.commons.xml.NewElement.createElement;
import static java.util.Collections.emptyList;

/**
 * The {@literal <build>} element contains project build settings.
 * <p/>
 * Supported next data:
 * <ul>
 * <li>sourceDirectory</li>
 * <li>testSourceDirectory</li>
 * <li>scriptSourceDirectory</li>
 * <li>outputDirectory</li>
 * <li>testOutputDirectory</li>
 * <li>resources</li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Build {

    private static final ElementMapper<Resource> RESOURCE_MAPPER = new ResourceMapper();

    private String         sourceDirectory;
    private String         testSourceDirectory;
    private String         scriptSourceDirectory;
    private String         outputDirectory;
    private String         testOutputDirectory;
    private List<Resource> resources;
    private List<Plugin>   plugins;

    Element buildElement;

    public Build() {
    }

    Build(Element buildElement) {
        this.buildElement = buildElement;
        sourceDirectory = buildElement.getChildText("sourceDirectory");
        testSourceDirectory = buildElement.getChildText("testSourceDirectory");
        scriptSourceDirectory = buildElement.getChildText("scriptSourceDirectory");
        outputDirectory = buildElement.getChildText("outputDirectory");
        testOutputDirectory = buildElement.getChildText("testOutputDirectory");
        if (buildElement.hasSingleChild("resources")) {
            resources = buildElement.getSingleChild("resources").getChildren(RESOURCE_MAPPER);
        }
    }

    /**
     * Returns path to directory where compiled application classes are placed.
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns path to directory containing the script sources of the project.
     * <p/>
     * This directory is meant to be different
     * from the sourceDirectory, in that its contents
     * will be copied to the output directory
     * in most cases (since scripts are interpreted rather than compiled).
     */
    public String getScriptSourceDirectory() {
        return scriptSourceDirectory;
    }

    /**
     * Returns path to directory containing the source of the project.
     * <p/>
     * The generated build system will compile the source
     * in this directory when the project is built.
     * The path given is relative to the project descriptor.
     */
    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Returns path to directory where compiled test classes are placed.
     */
    public String getTestOutputDirectory() {
        return testOutputDirectory;
    }

    /**
     * Returns path to  directory containing the unit test source of the project.
     * <p/>
     * The generated build system will compile
     * these directories when the project is
     * being tested. The path given is relative to the
     * project descriptor.
     */
    public String getTestSourceDirectory() {
        return testSourceDirectory;
    }

    /**
     * Sets the path to directory where compiled application classes are placed.
     */
    public Build setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        if (!isNew()) {
            if (outputDirectory == null) {
                buildElement.removeChild("outputDirectory");
            } else if (buildElement.hasSingleChild("outputDirectory")) {
                buildElement.getSingleChild("outputDirectory").setText(outputDirectory);
            } else {
                buildElement.appendChild(createElement("outputDirectory", outputDirectory));
            }
        }
        return this;
    }

    /**
     * Sets the path to directory containing the script sources of the project
     */
    public Build setScriptSourceDirectory(String scriptSourceDirectory) {
        this.scriptSourceDirectory = scriptSourceDirectory;
        if (!isNew()) {
            if (scriptSourceDirectory == null) {
                buildElement.removeChild("scriptSourceDirectory");
            } else if (buildElement.hasSingleChild("scriptSourceDirectory")) {
                buildElement.getSingleChild("scriptSourceDirectory").setText(scriptSourceDirectory);
            } else {
                buildElement.appendChild(createElement("scriptSourceDirectory", scriptSourceDirectory));
            }
        }
        return this;
    }

    /**
     * Sets the path to directory containing the source of the project.
     */
    public Build setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        if (!isNew()) {
            if (sourceDirectory == null) {
                buildElement.removeChild("sourceDirectory");
            } else if (buildElement.hasSingleChild("sourceDirectory")) {
                buildElement.getSingleChild("sourceDirectory").setText(sourceDirectory);
            } else {
                buildElement.appendChild(createElement("sourceDirectory", sourceDirectory));
            }
        }
        return this;
    }

    /**
     * Sets the path to directory where compiled test classes are placed.
     */
    public Build setTestOutputDirectory(String testOutputDirectory) {
        this.testOutputDirectory = testOutputDirectory;
        if (!isNew()) {
            if (testOutputDirectory == null) {
                buildElement.removeChild("testOutputDirectory");
            } else if (buildElement.hasSingleChild("testOutputDirectory")) {
                buildElement.getSingleChild("testOutputDirectory").setText(testOutputDirectory);
            } else {
                buildElement.appendChild(createElement("testOutputDirectory", testOutputDirectory));
            }
        }
        return this;
    }

    /**
     * Sets the path to directory containing the unit test source of the project.
     */
    public Build setTestSourceDirectory(String testSourceDirectory) {
        this.testSourceDirectory = testSourceDirectory;
        if (!isNew()) {
            if (testSourceDirectory == null) {
                buildElement.removeChild("testSourceDirectory");
            } else if (buildElement.hasSingleChild("testSourceDirectory")) {
                buildElement.getSingleChild("testSourceDirectory").setText(testSourceDirectory);
            } else {
                buildElement.appendChild(createElement("testSourceDirectory", testSourceDirectory));
            }
        }
        return this;
    }

    /**
     * Returns list of resource elements which contains information
     * about where associated with project files should be included
     */
    public List<Resource> getResources() {
        if (resources == null) {
            return emptyList();
        }
        return new ArrayList<>(resources);
    }

    /**
     * Sets build resources, each resource contains information about where
     * associated with project files should be included.
     */
    public Build setResources(Collection<? extends Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            removeResources();
        } else {
            setResources0(resources);
        }
        return this;
    }

    public List<Plugin> getPlugins() {
        if (plugins == null) {
            return emptyList();
        }
        return new ArrayList<>(plugins);
    }

    //mapping getId() -> Plugin
    public Map<String, Plugin> getPluginsAsMap() {
        final Map<String, Plugin> pluginsMap = new HashMap<>();
        for (Plugin plugin : plugins()) {
            pluginsMap.put(plugin.getId(), plugin);
        }
        return pluginsMap;
    }

    public Build setPlugins() {
        //TODO
        return this;
    }

    private List<Plugin> plugins() {
        return plugins == null ? plugins = new ArrayList<>() : null;
    }

    NewElement asXMLElement() {
        final NewElement buildEl = createElement("build");
        if (sourceDirectory != null) {
            buildEl.appendChild(createElement("sourceDirectory", sourceDirectory));
        }
        if (testSourceDirectory != null) {
            buildEl.appendChild(createElement("testSourceDirectory", testSourceDirectory));
        }
        if (scriptSourceDirectory != null) {
            buildEl.appendChild(createElement("scriptSourceDirectory", scriptSourceDirectory));
        }
        if (outputDirectory != null) {
            buildEl.appendChild(createElement("outputDirectory", outputDirectory));
        }
        if (testOutputDirectory != null) {
            buildEl.appendChild(createElement("testOutputDirectory", testOutputDirectory));
        }
        if (resources != null && !resources.isEmpty()) {
            buildEl.appendChild(newResourcesElement(resources));
        }
        return buildEl;
    }

    private boolean isNew() {
        return buildElement == null;
    }

    private NewElement newResourcesElement(List<Resource> resources) {
        final NewElement resourcesElement = createElement("resources");
        for (Resource resource : resources) {
            resourcesElement.appendChild(resource.asXMLElement());
        }
        return resourcesElement;
    }

    private void setResources0(Collection<? extends Resource> resources) {
        this.resources = new ArrayList<>(resources);

        if (isNew()) return;
        //if resources element exists we should replace it children
        //with new set of resources, otherwise create element for it
        if (buildElement.hasSingleChild("resources")) {
            //remove "resources" element children
            final Element resourcesElement = buildElement.getSingleChild("resources");
            for (Element resource : resourcesElement.getChildren()) {
                resource.remove();
            }
            //append each new resource to "resources" element
            for (Resource resource : resources) {
                resourcesElement.appendChild(resource.asXMLElement());
                resource.resourceElement = resourcesElement.getLastChild();
            }
        } else {
            buildElement.appendChild(newResourcesElement(this.resources));
        }
    }

    private void removeResources() {
        if (!isNew()) {
            buildElement.removeChild("resources");
        }
        this.resources = null;
    }

    private static class ResourceMapper implements ElementMapper<Resource> {

        @Override
        public Resource map(Element element) {
            return new Resource(element);
        }
    }
}
