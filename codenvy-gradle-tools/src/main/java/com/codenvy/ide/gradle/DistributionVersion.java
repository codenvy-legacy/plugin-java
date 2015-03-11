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
package com.codenvy.ide.gradle;

/** @author Vladyslav Zhukovskii */
public enum DistributionVersion {
    V2_3("2.3"),
    V2_3_RC_4("2.3-rc-4"),
    V2_3_RC_3("2.3-rc-3"),
    V2_3_RC_2("2.3-rc-2"),
    V2_3_RC_1("2.3-rc-1"),

    V2_2_1("2.2.1"),
    V2_2_1_RC_1("2.2.1-rc-1"),

    V2_2("2.2"),
    V2_2_RC_2("2.2-rc-2"),
    V2_2_RC_1("2.2-rc-1"),

    V2_1("2.1"),
    V2_1_RC_4("2.1-rc-4"),
    V2_1_RC_3("2.1-rc-3"),
    V2_1_RC_2("2.1-rc-2"),
    V2_1_RC_1("2.1-rc-1"),

    V2_0("2.0"),
    V2_0_RC_2("2.0-rc-2"),
    V2_0_RC_1("2.0-rc-1"),

    V1_12("1.12"),
    V1_12_RC_2("1.12-rc-2"),
    V1_12_RC_1("1.12-rc-1"),

    V1_11("1.11"),
    V1_11_RC_1("1.11-rc-1"),

    V1_10("1.10"),
    V1_10_RC_2("1.10-rc-2"),
    V1_10_RC_1("1.10-rc-1"),

    V1_9("1.9"),
    V1_9_RC_4("1.9-rc-4"),
    V1_9_RC_3("1.9-rc-3"),
    V1_9_RC_2("1.9-rc-2"),
    V1_9_RC_1("1.9-rc-1"),

    V1_8("1.8"),
    V1_8_RC_2("1.8-rc-2"),
    V1_8_RC_1("1.8-rc-1"),

    V1_7("1.7"),
    V1_7_RC_2("1.7-rc-2"),
    V1_7_RC_1("1.7-rc-1"),

    V1_6("1.6"),
    V1_6_RC_1("1.6-rc-1"),

    V1_5("1.5"),
    V1_5_RC_3("1.5-rc-3"),
    V1_5_RC_2("1.5-rc-2"),
    V1_5_RC_1("1.5-rc-1"),

    V1_4("1.4"),
    V1_4_RC_3("1.4-rc-3"),
    V1_4_RC_2("1.4-rc-2"),
    V1_4_RC_1("1.4-rc-1"),

    V1_3("1.3"),
    V1_3_RC_2("1.3-rc-2"),
    V1_3_RC_1("1.3-rc-1"),

    V1_2("1.2"),
    V1_2_RC_1("1.2-rc-1"),

    V1_1("1.1"),
    V1_1_RC_2("1.1-rc-2"),
    V1_1_RC_1("1.1-rc-1"),

    V1_0("1.0"),
    V1_0_RC_3("1.0-rc-3"),
    V1_0_RC_2("1.0-rc-2"),
    V1_0_RC_1("1.0-rc-1");

    private String version;

    DistributionVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getDistributionUrl() {
        return "http://services.gradle.org/distributions/gradle-" + version + "-all.zip";
    }

    public static DistributionVersion fromVersion(String version) {
        for (DistributionVersion v : DistributionVersion.values()) {
            if (v.version.equals(version)) {
                return v;
            }
        }

        throw new IllegalArgumentException("Failed to resolve version '" + version + "'");
    }
}
