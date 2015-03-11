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
package org.eclipse.che.gradle;

/** @author Vladyslav Zhukovskii */
public enum DistributionType {
    /**
     * Gradle builder distributed by Codenvy.
     */
    BUNDLED("bundled"),

    /**
     * Gradle builder distributed by user project.
     * <pre>
     * project_dir/
     *    gradlew
     *    gradlew.bat
     *    gradle/wrapper/
     *        gradle-wrapper.jar
     *        <b>gradle-wrapper.properties</b>
     * </pre>
     */
    DEF_WRAPPED("def_wrapped"),

    /**
     * Wrapped task
     */
    WRAPPED("wrapped"),

    /**
     * If there is no Gradle distribution specified
     */
    NONE("none");

    public boolean isWrapped() {
        return this == DEF_WRAPPED || this == WRAPPED;
    }

    private String value;

    DistributionType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static DistributionType fromValueOrDefault(String value) {
        for (DistributionType v : DistributionType.values()) {
            if (v.value.equals(value)) {
                return v;
            }
        }

        throw new IllegalArgumentException("Failed to resolve '" + value + "'");
    }
}
