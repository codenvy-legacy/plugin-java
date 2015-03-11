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
package org.eclipse.che.gradle;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.project.server.Constants;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.gradle.dto.GrdProject;
import org.eclipse.che.gradle.dto.GrdSourceSet;
import org.eclipse.che.gradle.dto.GrdTask;
import org.eclipse.che.vfs.impl.fs.VirtualFileImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.gradle.dto.GrdConfiguration.State.OUTDATED;

/** @author Vladyslav Zhukovskii */
public class GradleUtils {

    /**
     * Builder's util methods
     */

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

    /** Retrieve gradlew execution script to allow user run build with distributed gradle in project. */
    public static String getGradlewExecCommand(java.io.File workDir) {
        java.io.File gradlew = new java.io.File(workDir, "gradlew");
        return gradlew.getAbsolutePath();
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

    public static DistributionType getDistributionFromAttributes(Map<String, List<String>> projectAttributes) {
        List<String> values = projectAttributes.get("gradle.distribution");

        if (values == null || values.isEmpty()) {
            return DistributionType.BUNDLED;
        }

        return DistributionType.fromValueOrDefault(values.get(0));
    }

    /**
     * Gradle Extension's util methods.
     */

    /**
     *
     * @param projectFolder
     * @param grdConfiguration
     * @return
     * @throws ServerException
     */

    public static void writeGrdConfiguration(FolderEntry projectFolder, GrdConfiguration grdConfiguration) throws ServerException {
        writeServiceFile(projectFolder, "gradle.json", grdConfiguration);
    }

    public static void writeGrdSourceSetForCA(FolderEntry projectFolder, List<GrdSourceSet> sourceSets) throws ServerException {
        writeServiceFile(projectFolder, "gradle_source_set.json", sourceSets);
    }

    public static void writeServiceFile(FolderEntry projectFolder, String fileName, Object o) throws ServerException {
        try {
            FileEntry gradleFile = (FileEntry)projectFolder.getChild(Constants.CODENVY_DIR + "/" + fileName);
            if (gradleFile != null) {
                // this method may be called from EventSubscriber
                if (gradleFile.getVirtualFile() instanceof VirtualFileImpl) {
                    java.io.File ioFile = ((VirtualFileImpl)gradleFile.getVirtualFile()).getIoFile();
                    Files.write(ioFile.toPath(), DtoFactory.getInstance().toJson(o).getBytes());
                }
            } else {
                FolderEntry codenvy = (FolderEntry)projectFolder.getChild(Constants.CODENVY_DIR);
                if (codenvy == null) {
                    try {
                        codenvy = projectFolder.createFolder(Constants.CODENVY_DIR);
                    } catch (ConflictException e) {
                        // Already checked existence of folder ".codenvy".
                        throw new ServerException(e.getServiceError());
                    }
                }
                try {
                    final String json = DtoFactory.getInstance().toJson(o);
                    codenvy.createFile(fileName, json.getBytes(), null);
                } catch (ConflictException e) {
                    // Not expected, existence of file already checked
                    throw new ServerException(e.getServiceError());
                }
            }

        } catch (ForbiddenException e) {
            throw new ServerException(e.getServiceError());
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    public static DistributionType getProjectDistribution(FolderEntry projectFolder) throws ServerException, ForbiddenException {
        VirtualFileEntry buildGradle = projectFolder.getChild("build.gradle");
        if (buildGradle == null || buildGradle.isFolder()) {
            return DistributionType.NONE;
        }

        VirtualFileEntry gradlew = projectFolder.getChild("gradlew");
        if (!(gradlew == null || gradlew.isFolder())) {
            return DistributionType.DEF_WRAPPED;
        }

        GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(projectFolder);
        if (grdConfiguration.getConfigurationState() != OUTDATED && isWrapperTaskExist(grdConfiguration.getProject())) {
            return DistributionType.WRAPPED;
        }

        return DistributionType.BUNDLED;
    }

    public static boolean isWrapperTaskExist(GrdProject project) {
        for (GrdTask task : project.getTasks()) {
            if ("wrapper".equals(task.getName())) {
                return true;
            }
        }

        return false;
    }

    public static FolderEntry getRootProjectFolder(FolderEntry folder) throws ServerException, ForbiddenException {
        FolderEntry sinkFolder = folder;
        FolderEntry project = null;
        while (!sinkFolder.isRoot()) {
            if (sinkFolder.isProjectFolder()) {
                project = sinkFolder;
            }

            sinkFolder = sinkFolder.getParent();
        }

        return project;
    }

    public static GrdConfiguration getGrdProjectConfiguration(FolderEntry folderEntry) throws ServerException, ForbiddenException {
        FolderEntry rootProjectFolder = getRootProjectFolder(folderEntry);

        if (rootProjectFolder == null) {
            return DtoFactory.getInstance().createDto(GrdConfiguration.class).withConfigurationState(OUTDATED);
        }

        VirtualFileEntry json = rootProjectFolder.getChild(String.format("%s/%s", ".codenvy", "gradle.json"));

        if (json == null || json.isFolder()) {
            return DtoFactory.getInstance().createDto(GrdConfiguration.class).withConfigurationState(OUTDATED);
        }

        try {
            return DtoFactory.getInstance().createDtoFromJson(((FileEntry)json).getInputStream(), GrdConfiguration.class);
        } catch (IOException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public static boolean markRootProjectAsOutdated(FolderEntry projectFolder) throws ServerException, ForbiddenException {
        FolderEntry rootProjectFolder = GradleUtils.getRootProjectFolder(projectFolder);

       if (rootProjectFolder == null) {
           return false;
       }

        GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(rootProjectFolder);
        grdConfiguration.setConfigurationState(GrdConfiguration.State.OUTDATED);

        GradleUtils.writeGrdConfiguration(rootProjectFolder, grdConfiguration);
        return true;
    }

    //builder util method
    public static GrdConfiguration getGrdProjectConfiguration(File projectDirectory) {
        File ioGradleConfig = new File(projectDirectory, ".codenvy/gradle.json");
        if (!(ioGradleConfig.exists() || ioGradleConfig.isDirectory())) {
            return DtoFactory.getInstance().createDto(GrdConfiguration.class).withConfigurationState(OUTDATED);
        }
        try (InputStream in = new FileInputStream(ioGradleConfig)) {
            String json = IoUtil.readStream(in);
            return DtoFactory.getInstance().createDtoFromJson(json, GrdConfiguration.class);
        } catch (IOException e) {
            return DtoFactory.getInstance().createDto(GrdConfiguration.class).withConfigurationState(OUTDATED);
        }
    }

}
