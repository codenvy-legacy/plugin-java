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

import com.codenvy.ide.ext.java.server.JavadocFinder;
import com.codenvy.ide.ext.java.server.internal.core.JavaProject;
import com.codenvy.ide.ext.java.server.javadoc.JavadocContentAccess2;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class JavaDocTest extends BaseTest {
    private String urlPart = "http://localhost:8080/ws/java-ca?projectpath=/test&handle=";


    @Test
    public void binaryObjectDoc() throws JavaModelException {
        JavaProject project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
        IType type = project.findType("java.lang.Object");
        assertThat(type).isNotNull();
        String htmlContent = JavadocContentAccess2.getHTMLContent(type, true,urlPart);
        Assert.assertNotNull(htmlContent);
        assertThat(htmlContent).isNotNull().isNotEmpty().contains("Class <code>Object</code> is the root of the class hierarchy.");
    }

    @Test
    public void findObjectDoc() throws JavaModelException {
        JavaProject project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
        JavadocFinder finder = new JavadocFinder(urlPart);
        String javadoc = finder.findJavadoc(project, "java.lang.Object");
        Assert.assertNotNull(javadoc);
        assertThat(javadoc).isNotNull().isNotEmpty().contains("Class <code>Object</code> is the root of the class hierarchy.");
    }

    @Test
    public void binaryMethodDoc() throws JavaModelException {
        JavaProject project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
        IType type = project.findType("java.lang.Object");
        assertThat(type).isNotNull();
        IMethod method = type.getMethod("hashCode", null);
        assertThat(method).isNotNull();
        String htmlContent = JavadocContentAccess2.getHTMLContent(method, true,urlPart);
        assertThat(htmlContent).isNotNull().isNotEmpty().contains("Returns a hash code value for the object.");
    }

    @Test
    public void binaryGenericMethodDoc() throws JavaModelException {
        JavaProject project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
        IType type = project.findType("java.util.List");
        assertThat(type).isNotNull();
        IMethod method = type.getMethod("add", new String[]{"TE;"});
        assertThat(method).isNotNull();
        String htmlContent = JavadocContentAccess2.getHTMLContent(method, true, urlPart);
        assertThat(htmlContent).isNotNull().isNotEmpty().contains("Appends the specified element to the end of this list (optional");
    }

    @Test
    public void binaryFieldDoc() throws JavaModelException {
        JavaProject project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
        IType type = project.findType("java.util.ArrayList");
        assertThat(type).isNotNull();
        IField field = type.getField("size");
        assertThat(field).isNotNull();
        String htmlContent = JavadocContentAccess2.getHTMLContent(field, true, urlPart);
        assertThat(htmlContent).isNotNull().isNotEmpty().contains("The size of the ArrayList (the number of elements it contains).");
    }

    @Test
    public void binaryGenericObjectDoc() throws JavaModelException {
        JavaProject project = new JavaProject(new File("/temp"), "", "/temp", "ws", options);
        IType type = project.findType("java.util.ArrayList");
        assertThat(type).isNotNull();
        String htmlContent = JavadocContentAccess2.getHTMLContent(type, true, urlPart);
        assertThat(htmlContent).isNotNull().isNotEmpty().contains("Resizable-array implementation of the <tt>List</tt> interface.");
    }
}
