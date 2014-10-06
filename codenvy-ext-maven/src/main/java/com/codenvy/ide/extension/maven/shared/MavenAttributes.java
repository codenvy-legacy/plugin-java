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

    String GROUP_ID           = "maven.groupId";
    String VERSION            = "maven.version";
    String ARTIFACT_ID        = "maven.artifactId";
    String PACKAGING          = "maven.packaging";
    String PARENT_GROUP_ID    = "maven.parent.groupId";
    String PARENT_VERSION     = "maven.parent.version";
    String PARENT_ARTIFACT_ID = "maven.parent.artifactId";
    String SOURCE_FOLDER      = "maven.source.folder";
    String TEST_SOURCE_FOLDER = "maven.test.source.folder";
}
