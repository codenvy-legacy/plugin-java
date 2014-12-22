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
package com.codenvy.ide.extension.maven.shared;

/**
 * @author Evgen Vidolob
 */
public interface MavenAttributes {
    final String MAVEN_ID   = "maven";
    final String MAVEN_NAME = "Maven Project";

    final String ARCHETYPE_GENERATOR_ID = "archetype";
    final String SIMPLE_GENERATOR_ID    = "simple";

    final String GROUP_ID           = "maven.groupId";
    final String VERSION            = "maven.version";
    final String ARTIFACT_ID        = "maven.artifactId";
    final String PACKAGING          = "maven.packaging";
    final String PARENT_GROUP_ID    = "maven.parent.groupId";
    final String PARENT_VERSION     = "maven.parent.version";
    final String PARENT_ARTIFACT_ID = "maven.parent.artifactId";
    final String SOURCE_FOLDER      = "maven.source.folder";
    final String TEST_SOURCE_FOLDER = "maven.test.source.folder";
}
