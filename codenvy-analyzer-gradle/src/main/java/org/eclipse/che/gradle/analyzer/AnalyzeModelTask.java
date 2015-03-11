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
import java.util.concurrent.Callable;

/**
 * Analysis task. This task run Gradle tooling api, register custom plugin and request {@link TransportableJSON} which builds via
 * {@link GradleModelBuilder}.
 *
 * @author Vladyslav Zhukovskii
 */
public class AnalyzeModelTask implements Callable<TransportableJSON> {

    private static final Logger LOG                  = LoggerFactory.getLogger(AnalyzeModelExecutor.class);
    private static final String INIT_GRADLE          = "init.gradle";
    private static final String INIT_SCRIPT_ARGUMENT = "--init-script";
    private static final String QUITE_MODE           = "--quiet";

    private File         workDir;
    private ModelRequest request;

    /**
     * New instance of {@link AnalyzeModelTask}
     */
    public AnalyzeModelTask(File workDir, ModelRequest request) {
        this.workDir = workDir;
        this.request = request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportableJSON call() throws Exception {

        final String initScript = GradleBuildScriptResolver.getInstance().getGradlePluginRegistrationInitScript();
        Path ioInitScript = Files.write(new File(workDir, INIT_GRADLE).toPath(), initScript.getBytes());

        ByteArrayOutputStream errStdOutStream = new ByteArrayOutputStream();

        try {
            //use gradle built in project
            //if project doesn't have gradle distribution, then will be used gradle which built in tooling-api
            return getModel(true, workDir, ioInitScript.toFile(), errStdOutStream, request);
        } catch (UnsupportedVersionException e) {
            LOG.info(e.getMessage(), e);
            //we can catch this exception if user uses gradle version under 1.6
            //close current connection and create new with latest version of gradle
            errStdOutStream.reset();
            try {
                //and obtain model again
                return getModel(false, workDir, ioInitScript.toFile(), errStdOutStream, request);
            } catch (BuildException e1) {
                throw new AnalyzeModelException(errStdOutStream.toString());
            }
        } catch (BuildException e) {
            throw new AnalyzeModelException(errStdOutStream.toString());
        }
    }

    /**
     * Get {@link TransportableJSON} model based on Gradle project from Gradle tooling api.
     *
     * @param useBuildDistribution
     *         indicate is we use first Gradle distribution version specified to project, otherwise use latest actual version of Gradle
     *         distribution
     * @param workDir
     *         Gradle project directory
     * @param initScript
     *         path to init.gradle script which register custom Gradle plugin which allow model build
     * @param errStdOutStream
     *         stream to store error out
     * @param request
     *         type of model requested. {@link TransportableJSONProjectDefinition} model only
     *         supports at this moment
     * @return {@link TransportableJSON} object contains json string which represent built model
     */
    private TransportableJSON getModel(boolean useBuildDistribution,
                                       File workDir,
                                       File initScript,
                                       OutputStream errStdOutStream,
                                       ModelRequest request) {
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
            return connection.model(getModelClassFor(request))
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

    /**
     * Get request class model based on user request.
     */
    private Class<? extends TransportableJSON> getModelClassFor(ModelRequest request) {
        switch (request) {
            case PROJECT:
                return TransportableJSONProjectDefinition.class;
            default:
                return TransportableJSON.class;
        }
    }
}
