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
import java.util.Collection;
import java.util.List;

import static com.codenvy.commons.xml.NewElement.createElement;
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
    }

    DependencyManagement(Element element, List<Dependency> dependencies) {
        this.element = element;
        this.dependencies = dependencies;
    }

    public List<Dependency> getDependencies() {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        return dependencies;
    }

    public DependencyManagement addDependency(Dependency newDependency) {
        requireNonNull(newDependency);
        getDependencies().add(newDependency);
        if (!isNew()) {
            addToTree(newDependency);
        }
        return this;
    }

    public DependencyManagement removeDependency(Dependency dependency) {
        getDependencies().remove(dependency);
        if (!isNew()) {
            removeFromTree(dependency);
        }
        return this;
    }

    /**
     * Set the dependencies specified here are not used until they
     * are referenced in a
     * POM within the group. This allows the
     * specification of a "standard" version for a
     * particular dependency.
     */
    public DependencyManagement setDependencies(Collection<Dependency> newDependencies) {
        this.dependencies = new ArrayList<>(newDependencies);
        //add and associate each new dependency with element in tree
        newDependencies = new ArrayList<>(newDependencies.size());
        for (Dependency newDependency : newDependencies) {
            addDependency(newDependency);
        }
        return this;
    }

    void remove() {
        element.remove();
        element = null;
    }

    void setElement(Element element) {
        this.element = element;
    }

    NewElement asNewElement() {
        final NewElement newDM = createElement("dependencyManagement");
        final NewElement newDependencies = createElement("dependencies");
        for (Dependency dependency : getDependencies()) {
            newDependencies.appendChild(dependency.asNewElement());
        }
        return newDM.appendChild(newDependencies);
    }

    private void addToTree(Dependency newDependency) {
        if (element.hasChild("dependencies")) {
            element.getSingleChild("dependencies")
                   .appendChild(newDependency.asNewElement());
        } else {
            element.appendChild(createElement("dependencies", newDependency.asNewElement()));
        }
        newDependency.element = element.getSingleChild("dependencies").getLastChild();
    }

    private void removeFromTree(Dependency dependency) {
        if (dependencies.isEmpty()) {
            element.removeChild("dependencies");
            dependency.element = null;
        } else {
            dependency.remove();
        }
    }

    private boolean isNew() {
        return element == null;
    }
}
