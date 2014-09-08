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

package com.codenvy.ide.ext.java.server;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

/**
 * @author Evgen Vidolob
 */
@Path("/project-java-watcher/{ws-id}")
public class JavaProjectWatcherService {

    @Inject
    @PathParam("ws-id")
    private String wsId;

    @Inject
    private JavaProjectWatcher watcher;

    @POST
    @Path("open/{path:.*}")
    public void openProject(@PathParam("path")String path, @Context HttpServletRequest request) {
        watcher.projectOpened(request.getSession().getId(), wsId, normalizePath(path));
    }

    @POST
    @Path("close/{path:.*}")
    public void closeProject(@PathParam("path")String path, @Context HttpServletRequest request) {
        watcher.projectClosed(request.getSession().getId(), wsId, normalizePath(path));
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

}
