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
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;

import org.eclipse.jdt.core.JavaModelException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Path("navigation/{ws-id}")
public class JavaNavigationService {

    @PathParam("ws-id")
    private String wsId;

    @Inject
    private JavaProjectService service;

    @Inject
    private JavaNavigation navigation;

    @GET
    @Path("find-declaration")
    @Produces("application/json")
    public String findDeclaration(@QueryParam("projectpath")String projectPath, @QueryParam("bindingkey")String bindingKey)
            throws JavaModelException {
        JavaProject project = service.getOrCreateJavaProject(wsId, projectPath);
        return navigation.findDeclaration(project, bindingKey);
    }

    @GET
    @Path("libraries")
    @Produces("application/json")
    public List<Jar> getExternalLibraries( @QueryParam("projectpath")String projectPath) throws JavaModelException {
        JavaProject project = service.getOrCreateJavaProject(wsId, projectPath);
        return navigation.getProjectDepandecyJars(project);
    }

    @GET
    @Path("lib/children")
    @Produces("application/json")
    public List<JarEntry> getLibraryChildren( @QueryParam("projectpath")String projectPath, @QueryParam("root") int rootId) throws JavaModelException {
        JavaProject project = service.getOrCreateJavaProject(wsId, projectPath);
        return navigation.getPackageFragmentRootContent(project, rootId);
    }

    @GET
    @Path("children")
    @Produces("application/json")
    public List<JarEntry> getChildren( @QueryParam("projectpath")String projectPath, @QueryParam("path") String path, @QueryParam("root") int rootId) throws JavaModelException {
        JavaProject project = service.getOrCreateJavaProject(wsId, projectPath);
        return navigation.getChildren(project, rootId, path);
    }
}
