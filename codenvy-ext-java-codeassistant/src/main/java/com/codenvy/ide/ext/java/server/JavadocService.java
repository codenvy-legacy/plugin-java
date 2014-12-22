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

import com.codenvy.ide.ext.java.server.internal.core.JavaProject;
import com.google.inject.name.Named;

import org.eclipse.jdt.core.JavaModelException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * @author Evgen Vidolob
 */
@Path("javadoc/{ws-id}")
public class JavadocService {


    @PathParam("ws-id")
    private String wsId;

    @Inject
    private JavaProjectService service;

    @Inject
    @Named("javadoc.prefix.url")
    private String urlRefix;

    @Inject
    @Named("javadoc.css.url")
    private String cssUrl;

    @Path("find")
    @GET
    @Produces("text/html")
    public String findJavadoc(@QueryParam("fqn") String fqn, @QueryParam("projectpath") String projectPath) throws JavaModelException {
        JavaProject project = service.getOrCreateJavaProject(wsId, projectPath);
        String urlPart = getUrlPart(projectPath);
        return new JavadocFinder(urlPart, cssUrl).findJavadoc(project, fqn);
    }

    @Path("get")
    @Produces("text/html")
    @GET
    public String get(@QueryParam("handle") String handle, @QueryParam("projectpath") String projectPath) {
        JavaProject project = service.getOrCreateJavaProject(wsId, projectPath);
        String urlPart = getUrlPart(projectPath);
        return new JavadocFinder(urlPart, cssUrl).findJavadoc4Handle(project, handle);
    }

    private String getUrlPart(String projectPath) {
        return urlRefix + wsId + "/get"+ "?projectpath=" + projectPath + "&handle=";
    }

}
