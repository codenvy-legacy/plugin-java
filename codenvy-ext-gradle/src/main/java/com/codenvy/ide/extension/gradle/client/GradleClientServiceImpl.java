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
package com.codenvy.ide.extension.gradle.client;

import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link com.codenvy.ide.extension.gradle.client.GradleClientService}.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleClientServiceImpl implements GradleClientService {
    public static final String ANALYZE     = "/analyze";
    public static final String SYNCHRONIZE = "/synchronize";
    public static final String MODEL       = "/model";

    private final String              baseUrl;
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    protected GradleClientServiceImpl(@Named("restContext") String baseUrl,
                                      @Named("workspaceId") String workspace,
                                      AsyncRequestFactory asyncRequestFactory) {
        this.baseUrl = baseUrl + "/gradle/" + workspace;
        this.asyncRequestFactory = asyncRequestFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void analyzeProject(@Nonnull String projectPath,
                               AsyncRequestCallback<Void> callback) {
        String url = baseUrl + ANALYZE;
        String params = "?project=" + projectPath;
        asyncRequestFactory.createPostRequest(url + params, null).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void synchronizeProject(@Nonnull String projectPath,
                                   AsyncRequestCallback<Void> callback) {
        String url = baseUrl + SYNCHRONIZE;
        String params = "?project=" + projectPath;
        asyncRequestFactory.createPostRequest(url + params, null).send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getProjectModel(@Nonnull String projectPath,
                                AsyncRequestCallback<GrdConfiguration> callback) {
        String url = baseUrl + MODEL;
        String params = "?project=" + projectPath;
        asyncRequestFactory.createGetRequest(url + params).send(callback);
    }
}
