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

package com.codenvy.ide.ext.java.client.watcher;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.rest.AsyncRequest;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.AsyncRequestFactory;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ProjectWatcherService {

    private static final String BASE_URL = "/project-java-watcher/";

    private String          watcherUrl;
    private AsyncRequestCallback<Object> emptyCallback =new AsyncRequestCallback<Object>() {
        @Override
        protected void onSuccess(Object result) {

        }

        @Override
        protected void onFailure(Throwable exception) {
            Log.error(ProjectWatcherService.class, exception);
        }
    } ;

    private AsyncRequestFactory asyncRequestFactory;

    @Inject
    public ProjectWatcherService(@Named("workspaceId") String workspaceId, @Named("javaCA") String javaCAPath,
                                 AsyncRequestFactory asyncRequestFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        watcherUrl = javaCAPath + BASE_URL + workspaceId;
    }

    public void projectOpened(ProjectDescriptor project) {
        String url = watcherUrl + "/open" + project.getPath();
        AsyncRequest asyncRequest = asyncRequestFactory.createPostRequest(url, null);
        asyncRequest.send(emptyCallback);

    }

    public void projectClosed(ProjectDescriptor project) {
        String url = watcherUrl + "/close" + project.getPath();
        AsyncRequest asyncRequest = asyncRequestFactory.createPostRequest(url, null);
        asyncRequest.send(emptyCallback);
    }
}
