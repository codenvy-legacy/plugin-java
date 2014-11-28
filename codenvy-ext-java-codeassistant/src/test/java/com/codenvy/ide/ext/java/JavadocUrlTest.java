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

package com.codenvy.ide.ext.java;

import com.codenvy.ide.ext.java.server.internal.core.JavaProject;
import com.codenvy.ide.ext.java.server.javadoc.JavaElementLinks;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * @author Evgen Vidolob
 */
public class JavadocUrlTest extends BaseTest {

    private String urlPart = "http://localhost:8080/ws/java-ca?projectpath=/test&handle=";
    private JavaProject project;

    @Before
    public void prepare() {
        project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
    }

    @Test
    public void binaryObjectUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        IType type = project.findType("java.lang.Object");
        String uri = JavaElementLinks.createURI(urlPart, type);
        String handle = uri.substring(urlPart.length());
        handle = URLDecoder.decode(handle, "UTF-8");
        IJavaElement element = JavaElementLinks.parseURI(handle, project);
        Assertions.assertThat(element).isNotNull().isEqualTo(type);
    }

    @Test
    public void binaryFieldUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        IType type = project.findType("java.util.ArrayList");
        IField field = type.getField("size");
        String uri = JavaElementLinks.createURI(urlPart, field);
        String handle = uri.substring(urlPart.length());
        handle = URLDecoder.decode(handle, "UTF-8");
        IJavaElement element = JavaElementLinks.parseURI(handle, project);
        Assertions.assertThat(element).isNotNull().isEqualTo(field);
    }

    @Test
    public void binaryMethodUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        IType type = project.findType("java.util.List");
        IMethod method = type.getMethod("add", new String[]{"TE;"});
        String uri = JavaElementLinks.createURI(urlPart, method);
        String handle = uri.substring(urlPart.length());
        handle = URLDecoder.decode(handle, "UTF-8");
        IJavaElement element = JavaElementLinks.parseURI(handle, project);
        Assertions.assertThat(element).isNotNull().isEqualTo(method);
    }
}
