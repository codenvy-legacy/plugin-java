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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.codenvy.commons.xml.NewElement.createElement;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * Section for management of default dependency information for use in a group of POMs.
 *
 * @author Eugene Voevodin
 */
public class DependencyManagement {

    private List<Dependency> dependencies;

    Element element;

    public DependencyManagement() {
        dependencies = new ArrayList<>();
    }

    DependencyManagement(Element element, List<Dependency> dependencies) {
        this.element = element;
        this.dependencies = dependencies;
    }

    public List<Dependency> getDependencies() {
        if (dependencies == null) {
            return emptyList();
        }
        return new ArrayList<>(dependencies);
    }

    public Dependencies dependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return new Dependencies(element, dependencies);
    }

    void remove() {
        if (element != null) {
            element.remove();
            //disable of using dependencies
            for (Dependency dependency : dependencies) {
                dependency.element = null;
            }
            element = null;
        }
    }

    NewElement asXMLElement() {
        final NewElement newDM = createElement("dependencyManagement");
        final NewElement newDependencies = createElement("dependencies");
        for (Dependency dependency : dependencies) {
            newDependencies.appendChild(dependency.asXMLElement());
        }
        return newDM.appendChild(newDependencies);
    }
}
