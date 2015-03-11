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
package org.eclipse.che.gradle.shared;

/** @author Vladyslav Zhukovskii */
public final class GradleAttributes {
    /** Gradle builder ID. */
    public static final String GRADLE_ID = "gradle";

    /** Display name for new project wizard. */
    public static final String GRADLE_NAME = "Gradle Project";

    /** Set of provided properties. */
    public static final String SOURCE_FOLDER     = "gradle.source.folder";
    public static final String TEST_FOLDER       = "gradle.test.source.folder";
    public static final String DISTRIBUTION_TYPE = "gradle.distribution";


    /**
     * Files Configuration
     */
    public static final String GRADLEW_EXEC            = "gradlew";
    public static final String DEFAULT_SETTINGS_GRADLE = "settings.gradle";
    public static final String DEFAULT_BUILD_GRADLE    = "build.gradle";
    public static final String GRADLE_JSON             = "gradle.json";
    public static final String GRADLE_CA_JSON          = "gradle_ca.json";
    public static final String CODENVY_DIR             = ".codenvy";
    public static final String REL_GRADLE_JSON_PATH    = CODENVY_DIR + "/" + GRADLE_JSON;
    public static final String REL_GRADLE_CA_JSON_PATH = CODENVY_DIR + "/" + GRADLE_CA_JSON;

    /**
     * Mime Type Configuration
     */
    public static final String GROOVY_MIME_TYPE = "text/x-groovy";

    /**
     * Project Generation
     */
    public static final String GENERATION_STRATEGY_OPTION    = "type";
    public static final String GENERATION_NEW_MODULE_OPTION  = "new_module";
    public static final String WRAPPER_VERSION_OPTION        = "version";
    public static final String SIMPLE_GENERATION_STRATEGY    = "simple";
    public static final String WRAPPED_GENERATION_STRATEGY   = "wrapped";
    public static final String PROJECT_CONTENT_MODIFY_OPTION = "modifyProjectContent";

    /**
     * Websocket configuration
     * type:task:workspace:path
     */
    public static final String WEBSOCKET_ANALYZER_CHANNEL = "gradle:analyzer:%s:%s";

    public static final String DEFAULT_SOURCE_FOLDER = "src/main/java";
    public static final String DEFAULT_TEST_FOLDER   = "src/test/java";
}
