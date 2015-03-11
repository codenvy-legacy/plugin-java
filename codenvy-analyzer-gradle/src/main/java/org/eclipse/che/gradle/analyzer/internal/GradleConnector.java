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
package org.eclipse.che.gradle.analyzer.internal;

import org.eclipse.che.api.builder.internal.BuildLogger;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.gradle.analyzer.GradleBuildScriptResolver;
import org.eclipse.che.gradle.analyzer.model.TransportableJSON;
import org.eclipse.che.gradle.analyzer.model.TransportableJSONProjectDefinition;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.UnsupportedVersionException;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Vladyslav Zhukovskyi
 */
public class GradleConnector {
    private static final Logger LOG                  = LoggerFactory.getLogger(GradleConnector.class);
    private static final String INIT_GRADLE          = "init.gradle";
    private static final String INIT_SCRIPT_ARGUMENT = "--init-script";
    private static final String QUITE_MODE           = "--quiet";
    private File            workDir;
    private AnalysisProcess process;
    private AnalyzeRequest  request;
    private EventService    eventService;


    public GradleConnector(AnalysisProcess process, AnalyzeRequest request, EventService eventService) {
        this.process = process;
        this.request = request;
        this.eventService = eventService;
    }

    public void execute() {
        Path futureWorkDir = Paths.get(sourcesManager.getDirectory().getAbsolutePath(), event.getWorkspace(), event.getProject());
        Path workDir = Files.createDirectories(futureWorkDir);

        sourcesManager.addListener(new GradleSourcesManager(workDir.toFile(), event.getRequest()));
        sourcesManager.getSources(BuildLogger.DUMMY,
                                  event.getWorkspace(),
                                  event.getProject(),
                                  event.getSourcesZipBallLink(),
                                  workDir.toFile());
    }

    public TransportableJSON connectAndGetModel() throws Exception {
        final String initScript = GradleBuildScriptResolver.getInstance().getGradlePluginRegistrationInitScript();
        Path ioInitScript = Files.write(new File(workDir, INIT_GRADLE).toPath(), initScript.getBytes());

        ByteArrayOutputStream errStdOutStream = new ByteArrayOutputStream();

        try {
            //use gradle built in project
            //if project doesn't have gradle distribution, then will be used gradle which built in tooling-api
            return getModel(true, workDir, ioInitScript.toFile(), errStdOutStream);
        } catch (UnsupportedVersionException e) {
            LOG.info(e.getMessage(), e);
            //we can catch this exception if user uses gradle version under 1.6
            //close current connection and create new with latest version of gradle
            errStdOutStream.reset();
            try {
                //and obtain model again
                return getModel(false, workDir, ioInitScript.toFile(), errStdOutStream);
            } catch (BuildException e1) {
                throw new AnalyzerException(errStdOutStream.toString());
            }
        } catch (BuildException e) {
            throw new AnalyzerException(errStdOutStream.toString());
        }
    }

    private TransportableJSON getModel(boolean useBuildDistribution,
                                       File workDir,
                                       File initScript,
                                       OutputStream errStdOutStream) {
        FileDescriptor orig = FileDescriptor.out;
//        final PrintStream original = System.out;
        System.out.println("Backuped");

        OutputStream dummyStdOutStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        };

        System.setOut(new PrintStream(dummyStdOutStream));

        DefaultGradleConnector connector = (DefaultGradleConnector)DefaultGradleConnector.newConnector();
        connector.embedded(false);
        connector.forProjectDirectory(workDir);
        connector.setVerboseLogging(true);

        if (useBuildDistribution) {
            connector.useBuildDistribution();
        } else {
            connector.useGradleVersion(GradleVersion.current().getVersion());
        }

        ProjectConnection connection = connector.connect();


        try {
            return connection.model(TransportableJSONProjectDefinition.class)
                             .withArguments(INIT_SCRIPT_ARGUMENT, initScript.getAbsolutePath(), QUITE_MODE)
                             .setStandardOutput(dummyStdOutStream)
                    .setStandardError(errStdOutStream)
                            //uncomment this to run Gradle in debug mode
                            //.setJvmArguments("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9008")
                    .setColorOutput(true)
                    .get();
        } finally {
            connection.close();
            FileOutputStream fdOut = new FileOutputStream(orig);
            System.setOut(new PrintStream(new BufferedOutputStream(fdOut, 128), true));
            System.out.println("Restored");
        }
    }
}
