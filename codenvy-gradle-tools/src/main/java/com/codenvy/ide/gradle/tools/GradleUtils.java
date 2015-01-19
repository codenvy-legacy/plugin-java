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


import com.codenvy.api.core.util.CommandLine;
import com.codenvy.api.core.util.LineConsumer;
import com.codenvy.api.core.util.ProcessUtil;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.vfs.impl.fs.VirtualFileImpl;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.idea.IdeaContentRoot;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.idea.IdeaSourceDirectory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/** @author Vladyslav Zhukovskii */
public class GradleUtils {

    /**
     * Invokes gradle command and fetch environment information. e.g. version, ant version, groovy version, etc.
     *
     * @return map of environment variables
     * @throws IOException
     *         in case if reading environment info was failed
     */
    public static Map<String, String> getGradleEnvironmentInformation() throws IOException {
        final Map<String, String> versionInfo = new HashMap<>();
        final LineConsumer cmdOutput = new LineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                // we may have empty lines or starts from "---------", so skip them
                if (line.isEmpty() || line.startsWith("-")) {
                    return;
                }

                String key = null;
                int keyEnd = 0;
                int valueStart = 0;
                final int l = line.length();

                if (line.startsWith("Gradle")) {
                    key = "Gradle version";
                } else {
                    while (keyEnd < l) {
                        if (line.charAt(keyEnd) == ':') {
                            valueStart = keyEnd + 1;
                            break;
                        }
                        keyEnd++;
                    }
                    if (keyEnd > 0) {
                        key = line.substring(0, keyEnd);
                    }
                }

                if (key != null) {
                    while (valueStart < l && Character.isWhitespace(line.charAt(valueStart))) {
                        valueStart++;
                    }
                    if ("Gradle version".equals(key)) {
                        final String value = line.substring(valueStart, l).trim();
                        versionInfo.put(key, value);
                    } else {
                        final String value = line.substring(valueStart);
                        versionInfo.put(key, value);
                    }
                }
            }

            @Override
            public void close() throws IOException {
            }
        };

        readGradleVersionInformation(cmdOutput);
        return versionInfo;
    }

    private static void readGradleVersionInformation(LineConsumer cmdOutput) throws IOException {
        final CommandLine commandLine = new CommandLine(getGradleExecCommand()).add("-version");
        final ProcessBuilder processBuilder = new ProcessBuilder().command(commandLine.toShellCommand()).redirectErrorStream(true);
        final Process process = processBuilder.start();
        ProcessUtil.process(process, cmdOutput, LineConsumer.DEV_NULL);
    }

    /** Retrieve gradle execution command. e.g. /usr/bin/gradle */
    public static String getGradleExecCommand() {
        final java.io.File gradleHome = getGradleHome();
        if (gradleHome != null) {
            String gradle = "bin" + File.separatorChar + "gradle";
            return new java.io.File(gradleHome, gradle).getAbsolutePath();
        } else {
            return "gradle";
        }
    }

    /** Retrieve gradle home directory. */
    public static java.io.File getGradleHome() {
        final String gradleHomeEnv = System.getenv("GRADLE_HOME");
        if (gradleHomeEnv == null) {
            return null;
        }
        final java.io.File gradleHome = new java.io.File(gradleHomeEnv);
        return gradleHome.exists() ? gradleHome : null;
    }

    public interface SRC_DIR_TYPE {
        int MAIN = 4;
        int TEST = 8;
        int ALL  = MAIN | TEST;
    }

    public static List<String> getSourceDirectories(Project project, int whichType) {
        VirtualFile virtualProjectFolder = project.getBaseFolder().getVirtualFile();
        return getSourceDirectories(((VirtualFileImpl)virtualProjectFolder).getIoFile(), whichType);
    }

    /**
     * Parse gradle project to retrieve list of source directories from the last one.
     * Used Intellij IDEA api from tooling-api gradle library to perform parsing.
     * By default if in build script there was no source paths setted, parser return
     * those list of directories:
     * <ul>
     * <li>src/main/java</li>
     * <li>src/main/resources</li>
     * <li>src/test/java</li>
     * <li>src/test/resources</li>
     * </ul>
     *
     * @param ioProjectDir
     *         gradle project to parse
     * @return list of source directories used in gradle project. Paths are relative due to current project.
     */
    public static List<String> getSourceDirectories(java.io.File ioProjectDir, int whichType) {
        List<String> sourceDirs = new ArrayList<>();

        ProjectConnection connection = GradleConnector.newConnector()
                                                      .forProjectDirectory(ioProjectDir)
                                                      .connect();

        try {
            IdeaProject ideaProjectModel = connection.getModel(IdeaProject.class);

            for (IdeaModule ideaModuleModel : ideaProjectModel.getModules()) {
                for (IdeaContentRoot ideaContentRootModel : ideaModuleModel.getContentRoots()) {

                    if ((whichType & SRC_DIR_TYPE.MAIN) == SRC_DIR_TYPE.MAIN)
                        for (IdeaSourceDirectory dir : ideaContentRootModel.getSourceDirectories()) {
                            //transform "/path/to/project/src/main/java" => "src/main/java"
                            String relPath =
                                    ideaContentRootModel.getRootDirectory().toPath().relativize(dir.getDirectory().toPath()).toString();
                            sourceDirs.add(relPath);
                        }

                    if ((whichType & SRC_DIR_TYPE.TEST) == SRC_DIR_TYPE.TEST)
                        for (IdeaSourceDirectory dir : ideaContentRootModel.getTestDirectories()) {
                            //transform "/path/to/project/src/test/java" => "src/test/java"
                            String relPath =
                                    ideaContentRootModel.getRootDirectory().toPath().relativize(dir.getDirectory().toPath()).toString();
                            sourceDirs.add(relPath);
                        }
                }
            }
        } finally {
            connection.close();
        }

        return sourceDirs;
    }
}
