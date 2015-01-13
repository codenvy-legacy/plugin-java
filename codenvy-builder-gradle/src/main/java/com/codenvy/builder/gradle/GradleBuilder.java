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
package com.codenvy.builder.gradle;

import com.codenvy.api.builder.BuilderException;
import com.codenvy.api.builder.dto.BuildRequest;
import com.codenvy.api.builder.dto.BuilderEnvironment;
import com.codenvy.api.builder.internal.BuildResult;
import com.codenvy.api.builder.internal.Builder;
import com.codenvy.api.builder.internal.BuilderConfiguration;
import com.codenvy.api.builder.internal.Constants;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.util.CommandLine;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.ZipUtils;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.codenvy.ide.gradle.tools.GradleUtils;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Builder based on Gradle.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleBuilder extends Builder {
    private static final Logger LOG = LoggerFactory.getLogger(GradleBuilder.class);


    public static final String ARTIFACTS_LIST_FILE_NAME           = "artifacts_list.log";
    public static final String INIT_SCRIPT_FILE_NAME              = "init.gradle";
    public static final String COPY_DEPENDENCIES_SCRIPT_FILE_NAME = "copy_dependencies.gradle";

    /**
     * Groovy build script with task that copy dependencies into separate directory of current build project.
     */
    public static final String COPY_DEPENDENCIES_SCRIPT_CONTENT =
            "apply from: 'build.gradle'\n" +
            "task copyDependencies(type: Copy) {\n" +
            "   from configurations.compile\n" +
            "   into 'dependencies'\n" +
            "}";

    /**
     * Init script content, used when build ends. Groovy script collect built artifacts from tasks and write file list into
     * {@link GradleBuilder#ARTIFACTS_LIST_FILE_NAME} of current build project.
     */
    public static final String INIT_SCRIPT_CONTENT =
            "gradle.buildFinished {\n" +
            "    result ->\n" +
            "        String[] monitoredTasks = [ \"jar\", \"war\", \"ear\", \"distZip\", \"distTar\" ]\n" +
            "        File workDir = result.gradle.rootProject.projectDir as File\n" +
            "        PrintWriter artifactWriter = new PrintWriter(new File(workDir, \"" + ARTIFACTS_LIST_FILE_NAME + "\"))\n" +
            "        result.gradle.rootProject.tasks.each {\n" +
            "            task ->\n" +
            "                if (monitoredTasks.contains(task.name)) {\n" +
            "                    task.outputs.files.files.each {\n" +
            "                        builtArtifact ->\n" +
            "                            if (builtArtifact.exists()) {\n" +
            "                                artifactWriter.println(builtArtifact.absolutePath)\n" +
            "                            }\n" +
            "                    }\n" +
            "                }\n" +
            "        }\n" +
            "        artifactWriter.flush()\n" +
            "        artifactWriter.close()\n" +
            "}";

    private final Map<String, String> gradleProperties;

    @Inject
    public GradleBuilder(@Named(Constants.BASE_DIRECTORY) java.io.File rootDirectory,
                         @Named(Constants.NUMBER_OF_WORKERS) int numberOfWorkers,
                         @Named(Constants.QUEUE_SIZE) int queueSize,
                         @Named(Constants.KEEP_RESULT_TIME) int cleanupTime,
                         EventService eventService) {
        super(rootDirectory, numberOfWorkers, queueSize, cleanupTime, eventService);

        Map<String, String> tmpEnvProperties = null;
        try {
            tmpEnvProperties = GradleUtils.getGradleEnvironmentInformation();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        gradleProperties = tmpEnvProperties == null ? Collections.<String, String>emptyMap()
                                                    : Collections.unmodifiableMap(tmpEnvProperties);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return GradleAttributes.GRADLE_ID;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Gradle based builder implementation";
    }

    /** {@inheritDoc} */
    @Override
    protected BuildResult getTaskResult(FutureBuildTask task, boolean successful) throws BuilderException {
        if (!successful) {
            return new BuildResult(false);
        }

        if (!isGradleTaskSuccess(task)) {
            return new BuildResult(false);
        }

        final BuilderConfiguration config = task.getConfiguration();
        final java.io.File workDir = config.getWorkDir();
        final BuildResult result = new BuildResult(true);
        java.io.File[] files = null;

        switch (config.getTaskType()) {
            case DEFAULT:
                files = getBuiltArtifacts(workDir);
                break;
            case COPY_DEPS:
                final java.io.File dependencies = new java.io.File(workDir, "dependencies");
                if (dependencies.isDirectory() && dependencies.list().length > 0) {
                    final java.io.File zip = new java.io.File(workDir, "dependencies.zip");
                    try {
                        ZipUtils.zipDir(dependencies.getAbsolutePath(), dependencies, zip, IoUtil.ANY_FILTER);
                    } catch (IOException e) {
                        throw new BuilderException(e);
                    }
                    files = new java.io.File[]{zip};
                }
                break;
        }

        if (files != null && files.length > 0) {
            Collections.addAll(result.getResults(), files);
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected CommandLine createCommandLine(BuilderConfiguration config) throws BuilderException {
        final CommandLine commandLine = new CommandLine(GradleUtils.getGradleExecCommand());
        final List<String> targets = config.getTargets();
        final java.io.File workDir = config.getWorkDir();

        switch (config.getTaskType()) {
            case DEFAULT:
                try {
                    Files.write(new java.io.File(workDir, INIT_SCRIPT_FILE_NAME).toPath(), INIT_SCRIPT_CONTENT.getBytes());
                    commandLine.add("--init-script", INIT_SCRIPT_FILE_NAME);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }

                if (((BuildRequest)config.getRequest()).isSkipTest()) {
                    commandLine.add("-x", "test");
                }

                if (!targets.isEmpty()) {
                    commandLine.add(targets);
                } else {
                    commandLine.add("build");
                }
                break;
            case COPY_DEPS:
                if (!targets.isEmpty()) {
                    LOG.warn("Targets {} ignored when copy dependencies", targets);
                }

                try {
                    Files.write(new java.io.File(workDir, COPY_DEPENDENCIES_SCRIPT_FILE_NAME).toPath(),
                                COPY_DEPENDENCIES_SCRIPT_CONTENT.getBytes());
                    commandLine.add("-b", COPY_DEPENDENCIES_SCRIPT_FILE_NAME, "copyDependencies");
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                break;
        }

        return commandLine;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, BuilderEnvironment> getEnvironments() {
        final Map<String, BuilderEnvironment> env = new HashMap<>(4);
        final Map<String, String> properties = new HashMap<>(gradleProperties);
        properties.remove("Build time");
        properties.remove("Build number");
        properties.remove("Revision");
        final BuilderEnvironment def = DtoFactory.getInstance().createDto(BuilderEnvironment.class)
                                                 .withId("default")
                                                 .withIsDefault(true)
                                                 .withDisplayName(properties.get("Gradle version"))
                                                 .withProperties(properties);
        env.put(def.getId(), def);
        return env;
    }

    /**
     * Check status of Gradle build based on Gradle logs.
     *
     * @param task
     *         current build task to check
     * @return true in case if Gradle build was successful, otherwise false
     * @throws BuilderException
     */
    private boolean isGradleTaskSuccess(FutureBuildTask task) throws BuilderException {
        boolean gradleTaskSuccess = false;
        try (BufferedReader logReader = new BufferedReader(task.getBuildLogger().getReader())) {
            String line;
            while ((line = logReader.readLine()) != null) {
                if ("BUILD SUCCESSFUL".equals(line)) {
                    gradleTaskSuccess = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new BuilderException(e);
        }

        return gradleTaskSuccess;
    }

    /**
     * Retrieve built artifacts list from {@link GradleBuilder#ARTIFACTS_LIST_FILE_NAME}
     * which generated by Gradle {@link GradleBuilder#INIT_SCRIPT_CONTENT} in project directory.
     *
     * @param workDir
     *         Gradle project directory
     * @return list of built artifacts or null in case if {@link GradleBuilder#ARTIFACTS_LIST_FILE_NAME} file wasn't found
     * @throws BuilderException
     */
    private java.io.File[] getBuiltArtifacts(java.io.File workDir) throws BuilderException {
        java.io.File ioArtifactsLogFile = new java.io.File(workDir, ARTIFACTS_LIST_FILE_NAME);

        if (!ioArtifactsLogFile.exists()) {
            return null;
        }

        Set<java.io.File> builtArtifacts = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ioArtifactsLogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                java.io.File ioBuiltArtifact = new java.io.File(line);
                if (ioBuiltArtifact.exists() && ioBuiltArtifact.isFile()) {
                    builtArtifacts.add(ioBuiltArtifact);
                }
            }
        } catch (IOException e) {
            throw new BuilderException(e);
        }

        return builtArtifacts.toArray(new java.io.File[builtArtifacts.size()]);

    }
}
