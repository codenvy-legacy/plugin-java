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

import com.codenvy.ide.ext.java.server.JavaNavigation;
import com.codenvy.ide.ext.java.server.SourcesFromBytecodeGenerator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class FindDeclarationTest extends BaseTest {

    private JavaNavigation navigation = new JavaNavigation(new SourcesFromBytecodeGenerator());

    @Test
    public void testFindClassIsNotNullOrEmpty() throws Exception {
        String declaration = navigation.findDeclaration(project, "Ljava/lang/String;");
        assertThat(declaration).isNotNull().isNotEmpty();
    }

    @Test
    public void testFindClassShouldReturnSource() throws Exception {
        String declaration = navigation.findDeclaration(project, "Ljava/lang/String;");
        assertThat(declaration).isNotNull().isNotEmpty();
        JsonObject object = new JsonParser().parse(declaration).getAsJsonObject();
        JsonElement source = object.get("source");
        assertThat(source).isNotNull();
        assertThat(source.getAsString()).contains("public final class String")
                                        .contains("implements java.io.Serializable, Comparable<String>, CharSequence {");
    }

    @Test
    public void testFindClassShouldReturnNameRange() throws Exception {
        String declaration = navigation.findDeclaration(project, "Ljava/lang/String;");
        assertThat(declaration).isNotNull().isNotEmpty();
        JsonObject object = new JsonParser().parse(declaration).getAsJsonObject();
        JsonElement name = object.get("nameRange");
        assertThat(name).isNotNull();
        JsonObject nameRange = name.getAsJsonObject();
        assertThat(nameRange.get("offset")).isNotNull();
        assertThat(nameRange.get("length")).isNotNull();

    }
}
