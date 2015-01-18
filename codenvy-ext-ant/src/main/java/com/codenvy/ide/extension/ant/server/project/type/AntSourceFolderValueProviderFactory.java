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
package com.codenvy.ide.extension.ant.server.project.type;

import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.google.inject.Singleton;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/** @author Vladyslav Zhukovskii */
@Singleton
public class AntSourceFolderValueProviderFactory extends AbstractAntValueProviderFactory {
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AntAttributes.SOURCE_FOLDER;
    }

    /** {@inheritDoc} */
    @Override
    public ValueProvider newInstance(Project project) {
        return new AntValueProvider(project) {
            /** {@inheritDoc} */
            @Override
            public List<String> getValues(org.apache.tools.ant.Project antProject) {
                String srcDir = antProject.getProperty("src.dir");
                if (srcDir == null) {
                    srcDir = AntAttributes.DEF_TEST_SRC_PATH;
                } else {
                    // Don't show absolute path (seems Ant parser resolves it automatically). User shouldn't know any absolute paths on our
                    // file system. This is temporary solution, this shouldn't be actual when get rid form ant parsers for build.xml files.
                    final java.nio.file.Path relPath = antProject.getBaseDir().toPath().relativize(Paths.get(srcDir));
                    srcDir = relPath.toString();
                }
                return Arrays.asList(srcDir);
            }

            /** {@inheritDoc} */
            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {
            }
        };
    }
}
