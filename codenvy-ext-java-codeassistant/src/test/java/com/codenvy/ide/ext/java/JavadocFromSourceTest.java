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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class JavadocFromSourceTest extends BaseTest {

    private JavadocFinder finder = new JavadocFinder("test", "testUrl");

    @Test
    public void testJavadoc4Class() throws Exception {
        String javadoc = finder.findJavadoc(project, "Lcom/codenvy/test/MyClass;");
        assertThat(javadoc).isNotNull().contains("Test javadoc for class");
    }

    @Test
    public void testJavadoc4Method() throws Exception {
        String javadoc = finder.findJavadoc(project, "Lcom/codenvy/test/MyClass;.myMethod()");
        assertThat(javadoc).isNotNull().contains("My test method javadoc;");
    }

    @Test
    public void testJavadoc4StaticMethod() throws Exception {
        String javadoc = finder.findJavadoc(project, "Lcom/codenvy/test/MyClass;.isValidName(Ljava.lang.String;)");
        assertThat(javadoc).isNotNull().contains("Verifies that the specified name is valid for our service");
    }

    @Test
    public void testJavadoc4Field() throws Exception {
        String javadoc = finder.findJavadoc(project, "Lcom/codenvy/test/MyClass;.myField)Ljava.lang.String;");
        assertThat(javadoc).isNotNull().contains("My test field javadoc.");
    }

    @Test
    public void testResolveMethodsParam() throws Exception {
        IJavaElement element = project.findElement("Lcom/codenvy/test/MyClass;.isValidName(Ljava.lang.String;)Z", null);
        assertThat(element).isNotNull().isInstanceOf(IMethod.class);
        assertThat(element.getElementName()).isEqualTo("isValidName");
    }

    @Test
    public void testResolveBinaryMethodsParam() throws Exception {
        IJavaElement element = project.findElement("Ljava/lang/String;.endsWith(Ljava.lang.String;)Z", null);
        assertThat(element).isNotNull().isInstanceOf(IMethod.class);
        assertThat(element.getElementName()).isEqualTo("endsWith");
    }
}
