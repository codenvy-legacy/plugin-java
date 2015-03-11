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
package com.codenvy.ide.extension.gradle.server.project.generator.impl;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.type.AttributeValue;
import com.codenvy.ide.extension.gradle.server.GradleTemplateHelper;
import com.codenvy.ide.extension.gradle.server.project.generator.ProjectGenerator;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.SIMPLE_GENERATION_STRATEGY;

/**
 * Simple project generator.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class SimpleProjectGenerator implements ProjectGenerator {

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getId() {
        return SIMPLE_GENERATION_STRATEGY;
    }

    /** {@inheritDoc} */
    @Override
    public void generateProject(@Nonnull FolderEntry baseFolder, @Nonnull Map<String, AttributeValue> attributes,
                                Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        try {
            GradleTemplateHelper.getInstance().createGradleBuildFile(baseFolder);
        } catch (NotFoundException e) {
            throw new ServerException(e.getServiceError());
        }
    }
}
