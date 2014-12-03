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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.codenvy.commons.xml.NewElement.createElement;
import static java.util.Objects.requireNonNull;

/**
 * TODO do we need to filter deps here?
 * Helps to manage dependencies.
 *
 * @author Eugene Voevodin
 */
public class Dependencies {

    public interface DependencyFilter {
        boolean accept(Dependency dependency);
    }

    Element element;

    private LinkedList<Dependency> filtered;
    private List<Dependency>       dependencies;

    Dependencies(Element element, List<Dependency> dependencies) {
        this.dependencies = dependencies;
        this.element = element;
    }

    public Dependencies add(Dependency dependency) {
        requireNonNull(dependency);
        dependencies.add(dependency);
        if (!isNew()) {
            addDependencyToTree(dependency);
        }
        return this;
    }

    public Dependencies remove(Dependency dependency) {
        if (dependencies.remove(requireNonNull(dependency)) && !isNew()) {
            removeDependencyFromTree(dependency);
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
        if (isNew()) {
            dependencies = new ArrayList<>(newDependencies);
            return this;
        }
        //removing all dependencies from xml tree
        removeDependenciesFromTree();
        //add and associate each new dependency with element in tree
        dependencies = new ArrayList<>(newDependencies.size());
        for (Dependency newDependency : newDependencies) {
            add(newDependency);
        }
        return this;
    }

    public Dependencies byArtifactId(final String artifactId) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return artifactId.equals(dependency.getArtifactId());
            }
        });
    }

    public Dependencies byGroupId(final String groupId) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return groupId.equals(dependency.getGroupId());
            }
        });
    }

    public Dependencies byScope(final String scope) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return scope.equals(dependency.getScope());
            }
        });
    }

    public Dependencies byClassifier(final String classifier) {
        return filter(new DependencyFilter() {
            @Override
            public boolean accept(Dependency dependency) {
                return classifier.equals(dependency.getClassifier());
            }
        });
    }

    public Dependencies filter(DependencyFilter filter) {
        checkNotNew();
        for (Iterator<Dependency> depIt = filtered().iterator(); depIt.hasNext(); ) {
            if (!filter.accept(depIt.next())) {
                depIt.remove();
            }
        }
        return this;
    }

    public Dependency first() {
        checkNotNew();
        return filtered().isEmpty() ? null : filtered.getFirst();
    }

    public Dependency last() {
        checkNotNew();
        return filtered.isEmpty() ? null : filtered.getLast();
    }

    public List<Dependency> get() {
        checkNotNew();
        return filtered();
    }

    public int count() {
        return dependencies.size();
    }

    public void remove() {
        for (Dependency dependency : filtered()) {
            remove(dependency);
        }
    }

    private LinkedList<Dependency> filtered() {
        return filtered == null ? filtered = new LinkedList<>(dependencies) : filtered;
    }

    private void removeDependenciesFromTree() {
        if (dependencies == null) return;
        //remove element references
        for (Dependency dependency : dependencies) {
            dependency.element = null;
        }
        //remove dependencies element from tree
        element.removeChild("dependencies");
    }

    private void addDependencyToTree(Dependency dependency) {
        if (element.hasChild("dependencies")) {
            element.getSingleChild("dependencies")
                   .appendChild(dependency.asNewElement());
        } else {
            element.appendChild(createElement("dependencies", dependency.asNewElement()));
        }
        dependency.element = element.getSingleChild("dependencies").getLastChild();
    }

    private void removeDependencyFromTree(Dependency dependency) {
        if (dependencies.isEmpty()) {
            element.removeChild("dependencies");
            dependency.element = null;
        } else {
            dependency.remove();
        }
    }

    private void checkNotNew() {
        if (isNew()) {
            throw new RuntimeException("Should not be used on new element");
        }
    }

    private boolean isNew() {
        return element == null;
    }
}