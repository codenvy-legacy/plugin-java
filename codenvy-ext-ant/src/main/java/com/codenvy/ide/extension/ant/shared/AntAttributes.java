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
package com.codenvy.ide.extension.ant.shared;

/** @author Vladyslav Zhukovskii */
public interface AntAttributes {
    String ANT_PROJECT_CONTENT     = "ant.project.content";
    String ANT_DEF_PROJECT_CONTENT = "default";

    String BUILDER_SOURCE_FOLDERS = "builder.ant.source_folders";
    String SOURCE_FOLDER          = "ant.source.folder";
    String TEST_SOURCE_FOLDER     = "ant.test.source.folder";

    String DEF_SRC_PATH      = "src";
    String DEF_TEST_SRC_PATH = "test";
}
