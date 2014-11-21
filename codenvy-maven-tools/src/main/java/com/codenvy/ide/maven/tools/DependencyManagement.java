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
 * Section for management of default dependency information for use in a group of POMs.
 */
public class DependencyManagement {

    private java.util.List<Dependency> dependencies;

    /**
     * Method addDependency.
     */
    public void addDependency(Dependency dependency) {
        getDependencies().add(dependency);
    }

    /**
     * Method getDependencies.
     */
    public java.util.List<Dependency> getDependencies() {
        if (this.dependencies == null) {
            this.dependencies = new java.util.ArrayList<>();
        }

        return this.dependencies;
    }

    /**
     * Method removeDependency.
     */
    public void removeDependency(Dependency dependency) {
        getDependencies().remove(dependency);
    }

    /**
     * Set the dependencies specified here are not used until they
     * are referenced in a
     * POM within the group. This allows the
     * specification of a "standard" version for a
     * particular dependency.
     */
    public void setDependencies(java.util.List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
