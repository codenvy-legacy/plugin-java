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
package org.eclipse.che.ide.ext.java;

import org.eclipse.che.ide.ext.java.server.JavadocFinder;
import org.eclipse.che.ide.ext.java.server.javadoc.JavaElementLinks;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class JavadocUrlTest extends BaseTest {

    private String urlPart = "http://localhost:8080/ws/java-ca?projectpath=/test&handle=";

    @Test
    public void binaryObjectUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        IType type = project.findType("java.lang.Object");
        String uri = JavaElementLinks.createURI(urlPart, type);
        String handle = uri.substring(urlPart.length());
        handle = URLDecoder.decode(handle, "UTF-8");
        IJavaElement element = JavaElementLinks.parseURI(handle, project);
        assertThat(element).isNotNull().isEqualTo(type);
    }

    @Test
    public void binaryFieldUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        IType type = project.findType("java.util.ArrayList");
        IField field = type.getField("size");
        String uri = JavaElementLinks.createURI(urlPart, field);
        String handle = uri.substring(urlPart.length());
        handle = URLDecoder.decode(handle, "UTF-8");
        IJavaElement element = JavaElementLinks.parseURI(handle, project);
        assertThat(element).isNotNull().isEqualTo(field);
    }

    @Test
    public void binaryMethodUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        IType type = project.findType("java.util.List");
        IMethod method = type.getMethod("add", new String[]{"TE;"});
        String uri = JavaElementLinks.createURI(urlPart, method);
        String handle = uri.substring(urlPart.length());
        handle = URLDecoder.decode(handle, "UTF-8");
        IJavaElement element = JavaElementLinks.parseURI(handle, project);
        assertThat(element).isNotNull().isEqualTo(method);
    }

    @Test
    public void methodHandleUri() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        JavadocFinder finder = new JavadocFinder("test");
        String javadoc = finder.findJavadoc(project, "Ljava/lang/String;.startsWith(Ljava.lang.String;I)");
        assertThat(javadoc).isNotNull().contains(
                "Tests if the substring of this string beginning");
    }

    @Test
    public void methodHandleUri2() throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
        JavadocFinder finder = new JavadocFinder("test");
        String javadoc = finder.findJavadoc(project, "Ljava/lang/String;.split(Ljava.lang.String;)");
        assertThat(javadoc).isNotNull().contains(
                "Splits this string around matches");
    }
}
