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

import com.codenvy.commons.xml.Element;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.codenvy.commons.xml.NewElement.createElement;
import static java.util.Objects.requireNonNull;

/**
 * Helps to manage dependencies.
 *
 * @author Eugene Voevodin
 */
public class Dependencies {

    public interface DependencyFilter {
        boolean accept(Dependency dependency);
    }

    Element element;

    private LinkedList<Dependency> selected;
    private List<Dependency>       dependencies;

    Dependencies(Element element, List<Dependency> dependencies) {
        this.dependencies = dependencies;
        this.element = element;
    }

    /**
     * Adds new dependency to the end of dependencies list.
     * <p/>
     * Creates dependencies tag if element doesn't have dependencies yet
     *
     * @param dependency
     *         new dependency which will be added to the end of dependencies list
     */
    public Dependencies add(Dependency dependency) {
        dependencies.add(requireNonNull(dependency));
        if (!isNew()) {
            addDependencyToXML(dependency);
        }
        return this;
    }

    /**
     * Removes dependency from the list of existing dependencies.
     *
     * @param dependency
     *         dependency which should be removed
     */
    public Dependencies remove(Dependency dependency) {
        if (dependencies.remove(requireNonNull(dependency)) && !isNew()) {
            removeDependencyFromXML(dependency);
        }
        return this;
    }

    /**
     * Sets dependencies associated with a project.
     * <p/>
     * These dependencies are used to construct a
     * classpath for your project during the build process.
     * They are automatically downloaded from the
     * repositories defined in this project.
     * See <a href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a> for more information.
     */
    public Dependencies set(Collection<Dependency> newDependencies) {
        requireNonNull(newDependencies);
        if (isNew()) {
            dependencies.clear();
            dependencies.addAll(newDependencies);
            return this;
        }
        //removing all dependencies from xml tree
        removeDependenciesFromXML();
        if (!newDependencies.isEmpty()) {
            //add and associate each new dependency with element in tree
            dependencies.clear();
            for (Dependency newDependency : newDependencies) {
                add(newDependency);
            }
        }
        return this;
    }

    /**
     * Filters dependencies by artifactId.
     * <p/>
     * This method doesn't affect existing dependencies!
     */
    public Dependencies byArtifactId(final String artifactId) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return artifactId.equals(dependency.getArtifactId());
            }
        });
    }

    /**
     * Filters dependencies by groupId.
     * <p/>
     * This method doesn't affect existing dependencies!
     */
    public Dependencies byGroupId(final String groupId) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return groupId.equals(dependency.getGroupId());
            }
        });
    }

    /**
     * Filters dependencies by scope.
     * <p/>
     * This method doesn't affect existing dependencies!
     */
    public Dependencies byScope(final String scope) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return scope.equals(dependency.getScope());
            }
        });
    }

    /**
     * Filters dependencies by classifier.
     * <p/>
     * This method doesn't affect existing dependencies!
     */
    public Dependencies byClassifier(final String classifier) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return classifier.equals(dependency.getClassifier());
            }
        });
    }

    /**
     * Filters dependencies by given filter.
     * <p/>
     * This method doesn't affect existing dependencies!
     */
    public Dependencies filter(DependencyFilter filter) {
        for (Iterator<Dependency> depIt = selected().iterator(); depIt.hasNext(); ) {
            if (!filter.accept(depIt.next())) {
                depIt.remove();
            }
        }
        return this;
    }

    /**
     * Returns selected dependencies or {@code null} if nothing was selected
     */
    public Dependency first() {
        return selected().isEmpty() ? null : selected.getFirst();
    }

    /**
     * Returns last selected dependency or {@code null} if nothing was selected
     */
    public Dependency last() {
        return selected().isEmpty() ? null : selected.getLast();
    }

    /**
     * Removes selected dependencies from source list of dependencies
     */
    public void remove() {
        for (Dependency dependency : selected()) {
            remove(dependency);
        }
    }

    private LinkedList<Dependency> selected() {
        return selected == null ? selected = new LinkedList<>(dependencies) : selected;
    }

    private void removeDependenciesFromXML() {
        if (dependencies == null) return;
        //remove element references
        for (Dependency dependency : dependencies) {
            dependency.element = null;
        }
        //remove dependencies element from tree
        element.removeChild("dependencies");
    }

    private void addDependencyToXML(Dependency dependency) {
        if (element.hasChild("dependencies")) {
            element.getSingleChild("dependencies")
                   .appendChild(dependency.asXMLElement());
        } else {
            element.appendChild(createElement("dependencies", dependency.asXMLElement()));
        }
        dependency.element = element.getSingleChild("dependencies").getLastChild();
    }

    private void removeDependencyFromXML(Dependency dependency) {
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