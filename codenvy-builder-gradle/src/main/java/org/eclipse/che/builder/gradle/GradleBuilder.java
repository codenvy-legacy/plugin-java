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
package com.codenvy.builder.gradle;

import com.codenvy.api.builder.BuilderException;
import com.codenvy.api.builder.dto.BuildRequest;
import com.codenvy.api.builder.dto.BuilderEnvironment;
import com.codenvy.api.builder.internal.*;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.util.CommandLine;
import com.codenvy.commons.lang.Deserializer;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.ZipUtils;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.gradle.DistributionType;
import com.codenvy.ide.gradle.GradleUtils;
import com.codenvy.ide.gradle.dto.*;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Builder based on Gradle.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleBuilder extends Builder {
    private static final Logger LOG = LoggerFactory.getLogger(GradleBuilder.class);

    public static final String ARTIFACTS_LIST_LOG        = "artifacts_list.log";
    public static final String DEPENDENCIES_DIR          = "dependencies";
    public static final String MONITORING_TARGETS_GRADLE = "monitoring_targets.gradle";
    public static final String COPY_DEPENDENCIES_GRADLE  = "copy_dependencies.gradle";

    public static final String INIT_COMMAND_ARG      = "--init-script";
    public static final String NO_DAEMON_COMMAND_ARG = "--no-daemon";

    private final Map<String, String> gradleProperties;

    private FilenameFilter serviceFiles = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return !MONITORING_TARGETS_GRADLE.equals(name) && !ARTIFACTS_LIST_LOG.equals(name) && !"project.zip".equals(name);
        }
    };

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
        return "gradle";
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
        final File workDir = config.getWorkDir();
        final BuildResult result = new BuildResult(true);
        File[] files = null;

        switch (config.getTaskType()) {
            case DEFAULT:

                //at first check if we executed wrapper task
                //wrapper task creates gradle distribution inside user's project
                //in this case we need to pack all files in work dir
                //this task usually called when new project created
                if (wrapperTargetExist(config.getRequest().getTargets())) {
                    try {
                        final java.io.File zip = new java.io.File(workDir, "project.zip");
                        ZipUtils.zipDir(workDir.getParent(), workDir, zip, serviceFiles);

                        files = new java.io.File[]{zip};
                        break;
                    } catch (IOException e) {
                        throw new BuilderException(e);
                    }
                }

                //if normal build selected
                if (!config.getRequest().isIncludeDependencies()) {
                    //for regular build we use init script which collect built artifacts into log file, then we read this file,
                    //checks artifact existence and add it to general artifacts list
                    try (InputStream in = new FileInputStream(new File(workDir, ARTIFACTS_LIST_LOG))) {
                        Set<File> fileSet = new HashSet<>();
                        String artifacts = IoUtil.readStream(in);
                        new File(workDir, ARTIFACTS_LIST_LOG).delete();
                        for (String artifactPath : artifacts.split("\n")) {
                            if (artifactPath.isEmpty()) {
                                continue;
                            }

                            File artifact = new File(artifactPath);

                            if (!(artifact.exists() || artifact.isFile())) {
                                continue;
                            }

                            fileSet.add(artifact);
                        }

                        files = fileSet.toArray(new java.io.File[fileSet.size()]);
                        break;
                    } catch (IOException e) {
                        throw new BuilderException(e.getMessage());
                    }
                }


                //otherwise where selected include with dependency operation, which means that application was built to run inside docker
                GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(workDir);
                if (grdConfiguration.getConfigurationState() == GrdConfiguration.State.OUTDATED) {
                    //we can't trust outdated model. so just return empty file list
                    //we should have correct model to be able to know which project's sub-module was built with application plugin
                    files = new File[]{};
                    break;
                }

                //request build with dependencies, 'application' plugin was added to project and 'distZip' task was called
                //our zip should be placed by default path: $buildDir/distributions/$project.name-$project.version.«ext»
                //see http://gradle.org/docs/current/userguide/application_plugin.html
                if (grdConfiguration.getProject().getPlugins().containsKey("application")) {
                    Set<File> fileSet = new HashSet<>();
                    fillBuiltArtifactRecursive(workDir.getParentFile(), grdConfiguration.getProject(), fileSet);
                    files = fileSet.toArray(new java.io.File[fileSet.size()]);
                    break;
                }

            case COPY_DEPS:

                try {
                    final List<File> paths = new ArrayList<>();

                    SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                            if (dir.endsWith("dependency")) {
                                File zip = new File(dir.toFile().getParentFile(), "dependencies.zip");
                                ZipUtils.zipDir(dir.toFile().getAbsolutePath(), dir.toFile(), zip, IoUtil.ANY_FILTER);
                                paths.add(zip);
                            }

                            return FileVisitResult.CONTINUE;
                        }
                    };

                    Files.walkFileTree(workDir.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, visitor);

                    files = paths.toArray(new File[paths.size()]);
                } catch (IOException e) {
                    throw new BuilderException(e);
                }
                break;
        }

        Collections.addAll(result.getResults(), files);

        return result;
    }


    /** {@inheritDoc} */
    @Override
    protected CommandLine createCommandLine(BuilderConfiguration config) throws BuilderException {
        final CommandLine commandLine = new CommandLine();
        final List<String> targets = config.getTargets();
        final Map<String, String> options = config.getOptions();
        final java.io.File workDir = config.getWorkDir();
        final DistributionType distribution =
                GradleUtils.getDistributionFromAttributes(config.getRequest().getProjectDescriptor().getAttributes());

        switch (config.getTaskType()) {
            case DEFAULT:
                setupGradleExecCommand(commandLine, distribution, workDir);

                //notify gradle to kill daemon after building process
                commandLine.add(NO_DAEMON_COMMAND_ARG);

                //if selected skip test options
                if (((BuildRequest)config.getRequest()).isSkipTest()) {
                    commandLine.add("-x", "test");
                }

                //at this moment is not supported by client but anyway we can process them
                if (!options.isEmpty()) {
                    for (Map.Entry<String, String> option : options.entrySet()) {
                        commandLine.add(option.getKey(), option.getValue());
                    }
                }

                //if normal build selected
                if (!config.getRequest().isIncludeDependencies()) {
                    try {
                        writeInitScript(workDir, MONITORING_TARGETS_GRADLE, Collections.singletonMap("ARTIFACTS_LIST", ARTIFACTS_LIST_LOG));
                    } catch (IOException e) {
                        throw new BuilderException(e.getMessage());
                    }
                    commandLine.add(INIT_COMMAND_ARG, MONITORING_TARGETS_GRADLE);

                    if (!targets.isEmpty()) {
                        commandLine.add(targets);
                    } else {
                        //user may provide default targets in build.gradle, so if they exists, then those targets will be used
                        List<String> defaultProjectTargets = getDefaultProjectTargets(workDir);
                        commandLine.add(!(defaultProjectTargets == null || defaultProjectTargets.isEmpty()) ? defaultProjectTargets
                                                                                                            : Arrays.asList("build"));
                    }
                    break;
                }

                //If include with dependencies, e.g. for running application
                //Project sources isn't available yet. Postpone parsing of .codenvy/gradle.json file until sources becomes available.
                final SourcesManager sourcesManager = getSourcesManager();
                final SourceManagerListener sourcesListener = new SourceManagerListener() {
                    @Override
                    public void afterDownload(SourceManagerEvent event) {
                        if (!workDir.equals(event.getWorkDir())) {
                            return;
                        }

                        try {
                            GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(workDir);
                            if (grdConfiguration.getConfigurationState() == GrdConfiguration.State.OUTDATED) {
                                //run build as for generic project
                                writeInitScript(workDir, MONITORING_TARGETS_GRADLE,
                                                Collections.singletonMap("ARTIFACTS_LIST", ARTIFACTS_LIST_LOG));
                                commandLine.add(INIT_COMMAND_ARG, MONITORING_TARGETS_GRADLE);
                                return;
                            }

                            Set<String> usedPlugins = grdConfiguration.getProject().getPlugins().keySet();

                            if (usedPlugins.contains("application")) {
                                //application plugin has task distTar and distZip which help us to make a proper distributive
                                commandLine.add("distZip");
                            } else if (usedPlugins.contains("java")) {
                                //anyway init script will monitor pre-configured tasks to collect built artifacts
                                writeInitScript(workDir, MONITORING_TARGETS_GRADLE,
                                                Collections.singletonMap("ARTIFACTS_LIST", ARTIFACTS_LIST_LOG));
                                commandLine.add(INIT_COMMAND_ARG, MONITORING_TARGETS_GRADLE, "build");
                            } else {
                                throw new IllegalStateException("Only Java plugins available to build");
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException(e.getMessage());
                        } finally {
                            sourcesManager.removeListener(this);
                        }
                    }
                };

                sourcesManager.addListener(sourcesListener);

                break;
            case COPY_DEPS:
                setupGradleExecCommand(commandLine, distribution, workDir);

                try {
                    writeInitScript(workDir, COPY_DEPENDENCIES_GRADLE, Collections.singletonMap("DEPENDENCIES", DEPENDENCIES_DIR));
                    commandLine.add(INIT_COMMAND_ARG, COPY_DEPENDENCIES_GRADLE, NO_DAEMON_COMMAND_ARG);

                    commandLine.add(targets);
                    commandLine.add(options);

                } catch (IOException e) {
                    throw new BuilderException(e.getMessage());
                }
                break;
        }

        return commandLine;
    }

    /**
     * Get default project targets. Usually this targets instruct gradle to perform execution tasks if no user tasks were provided.
     *
     * @param workDir
     *         project directory
     * @return list of default project targets
     */
    private List<String> getDefaultProjectTargets(File workDir) {
        GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(workDir);
        if (grdConfiguration.getConfigurationState() == GrdConfiguration.State.OUTDATED) {
            return Collections.emptyList();
        }

        List<String> defaultBuildTasks = grdConfiguration.getProject().getDefaultBuildTasks();

        return !(defaultBuildTasks == null || defaultBuildTasks.isEmpty()) ? defaultBuildTasks : Collections.<String>emptyList();
    }

    /**
     * Write init gradle script.
     *
     * @param workDir
     *         project directory
     * @param templateName
     *         template name
     * @param variables
     *         variables need to replace in template
     * @throws IOException
     *         in any error related to storing template on file system
     */
    private void writeInitScript(File workDir, String templateName, Map<String, String> variables) throws IOException {
        final String templatePath = "/com/codenvy/builder/gradle/templates/%s";
        try (InputStream in = GradleBuilder.class.getResourceAsStream(String.format(templatePath, templateName))) {
            String template = IoUtil.readStream(in);
            if (!(variables == null || variables.isEmpty())) {
                template = Deserializer.resolveVariables(template, variables);
            }
            Files.write(new java.io.File(workDir, templateName).toPath(), template.getBytes());
        }
    }

    /**
     * Setup gradle exec script based on distribution type.
     *
     * @param commandLine
     *         command line object
     * @param distribution
     *         distribution type
     * @param workDir
     *         project directory
     */
    private void setupGradleExecCommand(CommandLine commandLine, DistributionType distribution, File workDir) {
        if (distribution.isWrapped()) { //not sure if this good decision we may request to
            String gradlewExec = GradleUtils.getGradlewExecCommand(workDir);     //collect dependencies from sub-module that can't
            commandLine.add("/bin/chmod", "+x", gradlewExec, "&&", gradlewExec); //contains `gradlew` sh script
        } else {
            commandLine.add(GradleUtils.getGradleExecCommand());
        }
    }


    /** {@inheritDoc} */
    @Override
    public Map<String, BuilderEnvironment> getEnvironments() {
        final Map<String, BuilderEnvironment> env = new HashMap<>();
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
     * Check if passed targets contain wrapper task.
     *
     * @param targets
     *         targets passed by user
     * @return true if wrapper targets exists
     */
    private boolean wrapperTargetExist(List<String> targets) {
        for (String target : targets) {
            if (target.contains("wrapper")) {
                //tasks maybe passed as `wrapper` or `:wrapper` or `:project:wrapper` etc. we just need to know if wrapper task exists
                return true;
            }
        }

        return false;
    }

    /**
     * Search recursively in project directory built artifacts.
     *
     * @param workDir
     *         project directory
     * @param project
     *         Gradle project configuration
     * @param fileSet
     *         file set that should be filled with built artifacts
     */
    private void fillBuiltArtifactRecursive(File workDir, GrdProject project, Set<File> fileSet) {
        final String relativeProjectDir = project.getDirectory();
        final String searchDirectory = String.format("%s/build/distributions/", relativeProjectDir);
        final Path distPath = new File(workDir, searchDirectory).toPath();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(distPath, "*.zip")) {
            for (Path artifact : dirStream) {
                fileSet.add(artifact.toFile());
            }
        } catch (IOException ignored) {
        }

        for (GrdProject child : project.getChild()) {
            fillBuiltArtifactRecursive(workDir, child, fileSet);
        }
    }

}
