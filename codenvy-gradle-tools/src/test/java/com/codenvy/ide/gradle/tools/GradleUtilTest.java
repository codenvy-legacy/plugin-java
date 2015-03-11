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
package com.codenvy.ide.gradle.tools;


import com.codenvy.commons.lang.Pair;
import com.codenvy.commons.lang.Strings;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** @author Vladyslav Zhukovskii */
public class GradleUtilTest {

    public static final String GRADLE_SCRIPT = "apply plugin: 'java'";

    //@Test disable due to long downloading gradle wrapper
//    @Test
    public void testShouldReturnDefaultSourceDirectories() throws Exception {
//        Path workDir = Files.createTempDirectory("gradle-");
//        Path buildScript = Files.write(workDir.resolve("build.gradle"), GRADLE_SCRIPT.getBytes(), StandardOpenOption.CREATE_NEW);
//
//        List<String> sourceDirectories = GradleUtils.getSourceDirectories(workDir.toFile(), GradleUtils.SRC_DIR_TYPE.ALL);
//
//        List<String> expectedDirectories = Arrays.asList("src/main/resources", "src/main/java", "src/test/resources", "src/test/java");
//
//        assertEquals(sourceDirectories, expectedDirectories);
    }


    private String getToolingJarsPath(Set<Class> clazz) {
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        for (Iterator<Class> it = clazz.iterator(); it.hasNext(); ) {
            Class<?> aClass = it.next();
            String jarPath = getJarPathForClass(aClass);
            buf.append('\"').append(jarPath).append('\"');
            if (it.hasNext()) {
                buf.append(',');
            }
        }
        buf.append(']');
        return buf.toString();
    }


    @Test
    public void testFoo() throws Exception {
        String toolingJarsPath = getToolingJarsPath(getToolingDTOClasses());
        System.out.println(toolingJarsPath);
    }


    private Set<Class> getToolingDTOClasses() {
        Set<Class> clazz = new HashSet<>();
        clazz.add(com.codenvy.ide.gradle.dto.server.DtoServerImpls.class);
        clazz.add(com.codenvy.dto.server.DtoFactoryVisitor.class);
        clazz.add(com.google.gson.GsonBuilder.class);
        return Collections.unmodifiableSet(clazz);
    }

    private String getJarPathForClass(Class clazz) {

        final String path = "/" + clazz.getName().replace('.', '/') + ".class";

        URL url = clazz.getResource(path);
        if (url == null) {
            url = ClassLoader.getSystemResource(path.substring(1));
        }

        return url != null ? extractRoot(url, path) : null;
    }

    private String extractRoot(URL resourceURL, String resourcePath) {
        String resultPath = null;
        String protocol = resourceURL.getProtocol();
        if ("file".equals(protocol)) {
            String path = resourceURL.getFile();
            String testPath = path.replace('\\', '/');
            String testResourcePath = resourcePath.replace('\\', '/');
            if (testPath.endsWith(testResourcePath)) {
                resultPath = path.substring(0, path.length() - resourcePath.length());
            }
        } else if ("jar".equals(protocol)) {
            Pair<String, String> paths = splitJarUrl(resourceURL.getFile());
            if (paths != null) {
                resultPath = paths.first;
            }
        }

        if (resultPath == null) {
            return null;
        }

        resultPath = Strings.trimEnd(resultPath, File.separator);
//        resultPath = URLUtil.unescapePercentSequences(resultPath);

        return resultPath;
    }

    public static final String JAR_SEPARATOR    = "!/";
    public static final String JAR_PROTOCOL     = "jar";
    public static final String FILE_PROTOCOL    = "file";
    public static final String SCHEME_SEPARATOR = "://";

    public static Pair<String, String> splitJarUrl(String url) {
        int pivot = url.indexOf(JAR_SEPARATOR);
        if (pivot < 0) return null;

        String resourcePath = url.substring(pivot + 2);
        String jarPath = url.substring(0, pivot);

        if (Strings.startsWithConcatenation(jarPath, JAR_PROTOCOL, ":")) {
            jarPath = jarPath.substring(JAR_PROTOCOL.length() + 1);
        }

        if (jarPath.startsWith(FILE_PROTOCOL)) {
            jarPath = jarPath.substring(FILE_PROTOCOL.length());
            if (jarPath.startsWith(SCHEME_SEPARATOR)) {
                jarPath = jarPath.substring(SCHEME_SEPARATOR.length());
            } else if (Strings.startsWithChar(jarPath, ':')) {
                jarPath = jarPath.substring(1);
            }
        }

        return Pair.of(jarPath, resourcePath);
    }
}
