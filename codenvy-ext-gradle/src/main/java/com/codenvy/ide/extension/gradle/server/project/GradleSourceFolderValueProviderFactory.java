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
package com.codenvy.ide.extension.gradle.server.project;

import com.codenvy.api.project.server.*;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
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
    public ValueProvider newInstance(Project project) {
        return new ValueProvider() {
            @Override
            public List<String> getValues() throws ValueStorageException {
                return null;
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {

            }
        };
    }
}
