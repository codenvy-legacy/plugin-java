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
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Build {

    private String sourceDirectory;
    private String testSourceDirectory;
    private String scriptSourceDirectory;
    private String outputDirectory;
    private String testOutputDirectory;

    Element element;

    public Build() {
    }

    Build(Element element) {
        this.element = element;
        sourceDirectory = element.getChildText("sourceDirectory");
        testSourceDirectory = element.getChildText("testSourceDirectory");
        scriptSourceDirectory = element.getChildText("scriptSourceDirectory");
        outputDirectory = element.getChildText("outputDirectory");
        testOutputDirectory = element.getChildText("testOutputDirectory");
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
                element.removeChild("outputDirectory");
            } else if (element.hasChild("outputDirectory")) {
                element.getSingleChild("outputDirectory").setText(outputDirectory);
            } else {
                element.appendChild(createElement("outputDirectory", outputDirectory));
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
                element.removeChild("scriptSourceDirectory");
            } else if (element.hasChild("scriptSourceDirectory")) {
                element.getSingleChild("scriptSourceDirectory").setText(scriptSourceDirectory);
            } else {
                element.appendChild(createElement("scriptSourceDirectory", scriptSourceDirectory));
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
                element.removeChild("sourceDirectory");
            } else if (element.hasChild("sourceDirectory")) {
                element.getSingleChild("sourceDirectory").setText(sourceDirectory);
            } else {
                element.appendChild(createElement("sourceDirectory", sourceDirectory));
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
                element.removeChild("testOutputDirectory");
            } else if (element.hasChild("testOutputDirectory")) {
                element.getSingleChild("testOutputDirectory").setText(testOutputDirectory);
            } else {
                element.appendChild(createElement("testOutputDirectory", testOutputDirectory));
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
                element.removeChild("testSourceDirectory");
            } else if (element.hasChild("testSourceDirectory")) {
                element.getSingleChild("testSourceDirectory").setText(testSourceDirectory);
            } else {
                element.appendChild(createElement("testSourceDirectory", testSourceDirectory));
            }
        }
        return this;
    }

    void removeFromXML() {
        if (!isNew()) {
            element.remove();
            element = null;
        }
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
        return buildEl;
    }

    private boolean isNew() {
        return element == null;
    }
}
