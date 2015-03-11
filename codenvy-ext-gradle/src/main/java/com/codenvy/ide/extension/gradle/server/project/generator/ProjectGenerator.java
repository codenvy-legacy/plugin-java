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
package com.codenvy.ide.extension.gradle.server.project.generator;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.type.AttributeValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Project generation service.
 *
 * @author Vladyslav Zhukovskii
 */
public interface ProjectGenerator {
    /**
     * Get generation ID.
     *
     * @return generation ID
     */
    @Nonnull
    public String getId();

    /**
     * Generate project.
     *
     * @param baseFolder
     *         future project folder
     * @param attributes
     *         attributes need to generate the project
     * @param options
     *         options need to generate the project
     * @throws ForbiddenException
     *         if folder to generate isn't available due to forbidden access
     * @throws ConflictException
     *         in any conflict exception
     * @throws ServerException
     *         in any server error
     */
    public void generateProject(@Nonnull FolderEntry baseFolder,
                                @Nonnull Map<String, AttributeValue> attributes,
                                @Nullable Map<String, String> options) throws ForbiddenException, ConflictException, ServerException;
}
