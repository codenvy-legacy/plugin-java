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
package org.eclipse.che.gradle.analyzer;

import org.eclipse.che.commons.lang.Deserializer;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Strings;
import org.eclipse.che.ide.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Gradle init.gradle script helper. Perform constructing correct init script.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleBuildScriptResolver {
    private static final Logger LOG = LoggerFactory.getLogger(GradleBuildScriptResolver.class);

    protected static final String INIT_SCRIPT_TEMPLATE = "init.gradle";
    protected static final String JAR_SEPARATOR        = "!/";
    protected static final String JAR_PROTOCOL         = "jar";
    protected static final String FILE_PROTOCOL        = "file";
    protected static final String SCHEME_SEPARATOR     = "://";

    private final String GENERATED_INIT_SCRIPT;

    private static GradleBuildScriptResolver instance;

    private GradleBuildScriptResolver() {
        this.GENERATED_INIT_SCRIPT = makeInitScript();
    }

    /**
     * Get instance of {@link GradleBuildScriptResolver} and if it first calling of this method init.gradle
     * script will be created and stored in memory to fast accessing.
     *
     * @return instance of {@link GradleBuildScriptResolver}
     */
    public static synchronized GradleBuildScriptResolver getInstance() {
        if (instance == null) {
            instance = new GradleBuildScriptResolver();
        }

        return instance;
    }

    /**
     * Get built init.gradle script. Which register custom Gradle plugin.
     *
     * @return init.gradle script generated content
     */
    public String getGradlePluginRegistrationInitScript() {
        return GENERATED_INIT_SCRIPT;
    }

    private String makeInitScript() {
        try (InputStream stream = AnalyzeModelExecutor.class.getResourceAsStream(String.format("/%s", INIT_SCRIPT_TEMPLATE))) {
            Set<Class> classes = getToolingClasses();
            String groovyJarPath = constructGroovyJarPath(classes);

            return Deserializer.resolveVariables(IoUtil.readStream(stream), Collections.singletonMap("JARS_PATH", groovyJarPath));
        } catch (IOException e) {
            LOG.error("Failed to generate init.gradle.");
            return null;
        }
    }

    private Set<Class> getToolingClasses() {
        Set<Class> clazz = new HashSet<>();
        clazz.add(org.eclipse.che.gradle.dto.server.DtoServerImpls.class);
        clazz.add(org.eclipse.che.dto.server.DtoFactoryVisitor.class);
        clazz.add(com.google.gson.GsonBuilder.class);
        clazz.add(GradleModelBuilder.class);
        return Collections.unmodifiableSet(clazz);
    }

    private String constructGroovyJarPath(Set<Class> clazz) {
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

        return resultPath;
    }

    private Pair<String, String> splitJarUrl(String url) {
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
