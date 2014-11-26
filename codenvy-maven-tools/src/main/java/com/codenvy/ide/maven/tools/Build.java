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

/**
 * The <code>&lt;project&gt;</code> element is the root of
 * the descriptor.
 * The following table lists all of the possible child elements.
 */
public class Build {

    private String  sourceDirectory;
    private String  scriptSourceDirectory;
    private String  testSourceDirectory;
    private String  outputDirectory;
    private String  testOutputDirectory;
    private Element element;

    public Build() {}

    Build(Element element) {
        this.element = element;
    }

    /**
     * Get the directory where compiled application classes are placed.
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Get this element specifies a directory containing the script
     * sources of the
     * project. This directory is meant to be different
     * from the sourceDirectory, in that its
     * contents will be copied to the output directory
     * in most cases (since scripts are
     * interpreted rather than compiled).
     */
    public String getScriptSourceDirectory() {
        return scriptSourceDirectory;
    }

    /**
     * Get this element specifies a directory containing the source
     * of the project. The
     * generated build system will compile the source
     * in this directory when the project is
     * built. The path given is relative to the project
     * descriptor.
     */
    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Get the directory where compiled test classes are placed.
     */
    public String getTestOutputDirectory() {
        return testOutputDirectory;
    }

    /**
     * Get this element specifies a directory containing the unit
     * test source of the
     * project. The generated build system will compile
     * these directories when the project is
     * being tested. The path given is relative to the
     * project descriptor.
     */
    public String getTestSourceDirectory() {
        return testSourceDirectory;
    }

    /**
     * Set the directory where compiled application classes are placed.
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Set this element specifies a directory containing the script
     * sources of the
     * project. This directory is meant to be different
     * from the sourceDirectory, in that its
     * contents will be copied to the output directory
     * in most cases (since scripts are
     * interpreted rather than compiled).
     */
    public void setScriptSourceDirectory(String scriptSourceDirectory) {
        this.scriptSourceDirectory = scriptSourceDirectory;
    }

    /**
     * Set this element specifies a directory containing the source
     * of the project. The
     * generated build system will compile the source
     * in this directory when the project is
     * built. The path given is relative to the project
     * descriptor.
     */
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * Set the directory where compiled test classes are placed.
     */
    public void setTestOutputDirectory(String testOutputDirectory) {
        this.testOutputDirectory = testOutputDirectory;
    }

    /**
     * Set this element specifies a directory containing the unit
     * test source of the
     * project. The generated build system will compile
     * these directories when the project is
     * being tested. The path given is relative to the
     * project descriptor.
     */
    public void setTestSourceDirectory(String testSourceDirectory) {
        this.testSourceDirectory = testSourceDirectory;
    }
}
