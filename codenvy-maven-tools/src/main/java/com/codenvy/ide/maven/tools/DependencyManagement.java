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

import java.util.List;

import static com.codenvy.commons.xml.NewElement.createElement;

/**
 * Section for management of default dependency information for use in a group of POMs.
 *
 * @author Eugene Voevodin
 */
public class DependencyManagement {

    private Dependencies dependencies;

    Element dmElement;

    public DependencyManagement() {
    }

    DependencyManagement(Element element, List<Dependency> dependencies) {
        this.dmElement = element;
        this.dependencies = new Dependencies(element, dependencies);
    }

    public List<Dependency> getDependencies() {
        return dependencies().get();
    }

    public Dependencies dependencies() {
        if (dependencies == null) {
            dependencies = new Dependencies(dmElement);
        }
        return dependencies;
    }

    //TODO
    void remove() {
        if (dmElement != null) {
            dmElement.remove();
            dependencies = null;
        }
    }

    NewElement asXMLElement() {
        return createElement("dependencyManagement", dependencies.asXMLElement());
    }
}
