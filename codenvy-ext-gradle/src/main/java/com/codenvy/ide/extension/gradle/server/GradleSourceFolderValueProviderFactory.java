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
package com.codenvy.ide.extension.gradle.server;

import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.codenvy.ide.gradle.tools.GradleUtils;
import com.google.inject.Singleton;

import java.util.List;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GradleSourceFolderValueProviderFactory implements ValueProviderFactory {
    @Override
    public String getName() {
        return GradleAttributes.SOURCE_FOLDER;
    }

    @Override
    public ValueProvider newInstance(final Project project) {
        return new ValueProvider() {
            @Override
            public List<String> getValues() throws ValueStorageException {
                return GradleUtils.getSourceDirectories(project, GradleUtils.SRC_DIR_TYPE.MAIN);
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {
                //TODO need to be implemented
            }
        };
    }
}
