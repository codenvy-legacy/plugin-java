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
package org.eclipse.che.gradle.client;


import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.annotation.Nonnull;

/**
 * Client service for various operation on Gradle project.
 *
 * @author Vladyslav Zhukovskii
 */
public interface GradleClientService {
    /**
     * Perform analysis for specified project passed by <code>projectPath</code> parameter.
     * Analyzer through Gradle tooling api on server side connects to project and build project model send through websocket to client
     * side. After calling this method user should subscribe to websocket channel 'gradle:analyzer:{ws-name}:{project-path}'.
     *
     * @param projectPath
     *         path to gradle project
     * @param callback
     *         callback
     */
    void analyzeProject(@Nonnull String projectPath,
                        AsyncRequestCallback<Void> callback);

    /**
     * Perform synchronizing project with model which have been got by analysis task.
     * It may be useful if user want to synchronize model with current opened project.
     *
     * @param projectPath
     *         path to gradle project
     * @param callback
     *         callback
     */
    void synchronizeProject(@Nonnull String projectPath,
                            AsyncRequestCallback<Void> callback);

    /**
     * Get project model if it exists.
     *
     * @param projectPath
     *         path to gradle project
     * @param callback
     *         callback
     */
    void getProjectModel(@Nonnull String projectPath,
                         AsyncRequestCallback<GrdConfiguration> callback);
}
