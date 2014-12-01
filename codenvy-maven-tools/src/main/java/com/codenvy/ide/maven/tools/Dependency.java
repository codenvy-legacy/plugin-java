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
import com.codenvy.commons.xml.FromElementFunction;
import com.codenvy.commons.xml.NewElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.codenvy.commons.xml.NewElement.createElement;
import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.requireNonNull;

/**
 * The {@code <dependency>} element contains information about project's dependency.
 * <p/>
 * Supported next data:
 * <ul>
 * <li>artifactId</li>
 * <li>groupId</li>
 * <li>version</li>
 * <li>scope</li>
 * <li>classifier</li>
 * <li>type</li>
 * <li>optional</li>
 * <li>systemPath</li>
 * <li>exclusions</li>
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Dependency {

    private static final ToExclusionFunction TO_EXCLUSION_FUNCTION = new ToExclusionFunction();

    private String          groupId;
    private String          artifactId;
    private String          version;
    private String          type;
    private String          classifier;
    private String          scope;
    private String          optional;
    private List<Exclusion> exclusions;

    Element element;

    public Dependency() {
    }

    Dependency(Element element) {
        this.element = element;
        artifactId = element.getChildText("artifactId");
        groupId = element.getChildText("groupId");
        version = element.getChildText("version");
        classifier = element.getChildText("classifier");
        scope = element.getChildText("scope");
        optional = element.getChildText("optional");
        type = element.getChildTextOrDefault("type", "jar");
        //if dependency has exclusions fetch it!
        if (element.hasSingleChild("exclusions")) {
            exclusions = element.getSingleChild("exclusions")
                                .getChildren(TO_EXCLUSION_FUNCTION);
        }
    }

    /**
     * Returns the unique id for an artifact produced by
     * the project group, e.g. {@code maven-artifact}.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the classifier of the dependency.
     * <p/>
     * This allows distinguishing two artifacts
     * that belong to the same POM but were built
     * differently, and is appended to the filename after the version.
     * For example, {@code jdk14} and {@code jdk15}.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Returns dependency exclusions if dependency has it or empty set if doesn't
     */
    public List<Exclusion> getExclusions() {
        if (exclusions == null) {
            exclusions = new ArrayList<>();
        }
        return exclusions;
    }

    /**
     * Returns the project group that produced the dependency,
     * e.g. {@code org.apache.maven}.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the dependency is optional or not.
     * If it is optional then {@code true} will be returned otherwise {@code false}
     */
    public String getOptional() {
        return optional;
    }

    /**
     * Returns the scope of the dependency:
     * <ul>
     * <li>compile</li>
     * <li>runtime</li>
     * <li>test</li>
     * <li>system</li>
     * <li>provided</li>
     * </ul>
     * Used to calculate the various classpath used for
     * compilation, testing, and so on.
     * It also assists in determining which artifacts
     * to include in a distribution of
     * this project. For more information, see
     * <a href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a>.
     */

    public String getScope() {
        return scope;
    }

    /**
     * Returns the type of dependency.
     * <p/>
     * This defaults to <code>jar</code>.
     * While it usually represents the extension on
     * the filename of the dependency,
     * that is not always the case. A type can be
     * mapped to a different
     * extension and a classifier.
     * The type often corresponds to the packaging
     * used, though this is also
     * not always the case.
     * Some examples are {@code jar, war, ejb-client}.
     * New types can be defined by plugins that set
     * {@code extensions} to {@code true}, so
     * this is not a complete list.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the version of the dependency
     */
    public String getVersion() {
        return version;
    }

    /**
     * Adds new exclusion to the list of dependency exclusions
     */
    public Dependency addExclusion(Exclusion exclusion) {
        requireNonNull(exclusion);
        getExclusions().add(exclusion);
        //add exclusion to xml tree
        if (!isNew()) {
            if (element.hasSingleChild("exclusions")) {
                element.getSingleChild("exclusions")
                       .appendChild(exclusion.toNewElement());
            } else {
                element.appendChild(createElement("exclusions", exclusion.toNewElement()));
            }
            exclusion.setElement(element.getSingleChild("exclusions")
                                        .getLastChild());
        }
        return this;
    }

    /**
     * Removes exclusion from the dependency exclusions.
     * If last exclusion has been removed removes exclusions element as well.
     */
    public Dependency removeExclusion(Exclusion exclusion) {
        requireNonNull(exclusion);
        getExclusions().remove(exclusion);
        //remove dependency from xml tree
        if (!isNew() && exclusions.isEmpty()) {
            element.removeChild("exclusions");
        } else {
            exclusion.remove();
        }
        return this;
    }

    /**
     * Sets list of artifacts that should be excluded from this dependency's
     * artifact list when it comes to calculating transitive dependencies.
     */
    public Dependency setExclusions(Collection<Exclusion> newExclusions) {
        if (isNew()) {
            exclusions = new ArrayList<>(newExclusions);
            return this;
        }
        if (element.hasChild("exclusions")) {
            element.removeChild("exclusions");
        }
        exclusions = new ArrayList<>(newExclusions.size());
        //use addExclusion to add and associate each new exclusion with tree element
        for (Exclusion exclusion : newExclusions) {
            addExclusion(exclusion);
        }
        return this;
    }

    /**
     * Sets the unique id for an artifact produced by
     * the project group, e.g. {@code maven-artifact}.
     */
    public Dependency setArtifactId(String artifactId) {
        this.artifactId = requireNonNull(artifactId);
        if (!isNew()) {
            element.setChildText("artifactId", artifactId, true);
        }
        return this;
    }

    /**
     * Sets the classifier of the dependency.
     */
    public Dependency setClassifier(String classifier) {
        this.classifier = requireNonNull(classifier);
        if (!isNew()) {
            element.setChildText("classifier", classifier, true);
        }
        return this;
    }

    /**
     * Sets the project group that produced the dependency,
     * e.g. {@code org.apache.maven}.
     */
    public Dependency setGroupId(String groupId) {
        this.groupId = requireNonNull(groupId);
        if (!isNew()) {
            element.setChildText("groupId", groupId, true);
        }
        return this;
    }

    /**
     * Sets indicates the dependency is optional for use of this library.
     *
     * @see #setOptional(boolean)
     */
    public Dependency setOptional(String optional) {
        this.optional = requireNonNull(optional);
        if (!isNew()) {
            element.setChildText("optional", optional, true);
        }
        return this;
    }

    /**
     * Sets the scope of the dependency:
     * <ul>
     * <li>compile</li>
     * <li>runtime</li>
     * <li>test</li>
     * <li>system</li>
     * <li>provided</li>
     * </ul>
     * Used to calculate the various classpath used for
     * compilation, testing, and so on.
     * It also assists in determining which artifacts
     * to include in a distribution of
     * this project. For more information, see
     * <a href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
     * dependency mechanism</a>.
     */
    public Dependency setScope(String scope) {
        this.scope = requireNonNull(scope);
        if (!isNew()) {
            element.setChildText("scope", scope, true);
        }
        return this;
    }

    /**
     * Sets the type of dependency.
     * <p/>
     * This defaults to <code>jar</code>.
     * While it usually represents the extension on
     * the filename of the dependency,
     * that is not always the case. A type can be
     * mapped to a different
     * extension and a classifier.
     * The type often corresponds to the packaging
     * used, though this is also
     * not always the case.
     * Some examples are {@code jar, war, ejb-client}.
     * New types can be defined by plugins that set
     * {@code extensions} to {@code true}, so
     * this is not a complete list.
     */
    public Dependency setType(String type) {
        this.type = requireNonNull(type);
        if (!isNew()) {
            element.setChildText("type", type, true);
        }
        return this;
    }

    /**
     * Set the version of the dependency, e.g. <code>3.2.1</code>.
     * In Maven 2, this can also be
     * specified as a range of versions.
     */
    public Dependency setVersion(String version) {
        this.version = requireNonNull(version);
        if (!isNew()) {
            element.setChildText("version", version, true);
        }
        return this;
    }

    /**
     * Returns {@code true} if dependency is optional otherwise returns {@code false}
     */
    public boolean isOptional() {
        return parseBoolean(optional);
    }

    /**
     * Sets indicates the dependency is optional for use of this library.
     *
     * @see #setOptional(String)
     */
    public Dependency setOptional(boolean optional) {
        return setOptional(String.valueOf(optional));
    }

    /**
     * @return the management key as {@code groupId:artifactId:type}
     */
    public String getManagementKey() {
        return groupId + ":" + artifactId + ":" + type + (classifier != null ? ":" + classifier : "");
    }

    @Override
    public String toString() {
        return "Dependency {groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", type=" + type + "}";
    }

    void remove() {
        if (!isNew()) {
            element.remove();
            element = null;
        }
    }


    NewElement toNewElement() {
        final NewElement dependencyEl = createElement("dependency");
        dependencyEl.appendChild(createElement("artifactId", artifactId));
        dependencyEl.appendChild(createElement("groupId", groupId));
        dependencyEl.appendChild(createElement("version", version));
        if (scope != null) {
            dependencyEl.appendChild(createElement("scope", scope));
        }
        if (type != null) {
            dependencyEl.appendChild(createElement("type", type));
        }
        if (classifier != null) {
            dependencyEl.appendChild(createElement("classifier", classifier));
        }
        if (optional != null) {
            dependencyEl.appendChild(createElement("optional", optional));
        }
        if (exclusions != null) {
            final NewElement exclusionsEl = createElement("exclusions");
            for (Exclusion exclusion : exclusions) {
                exclusionsEl.appendChild(exclusion.toNewElement());
            }
            exclusionsEl.appendChild(exclusionsEl);
        }
        return dependencyEl;
    }

    private boolean isNew() {
        return element == null;
    }

    private static class ToExclusionFunction implements FromElementFunction<Exclusion> {

        @Override
        public Exclusion apply(Element element) {
            return new Exclusion(element);
        }
    }
}
