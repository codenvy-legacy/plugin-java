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
package com.codenvy.ide.extension.gradle.server.project.analyzer;

import com.codenvy.ide.gradle.dto.GrdConfiguration;

/**
 * Callback for asynchronous analysis request.
 *
 * @author Vladyslav Zhukovskii
 */
public interface GradleProjectAnalyzerCallback {
    /**
     * Perform action when analysis was successful.
     *
     * @param grdConfiguration
     *         created Gradle project configuration
     */
    void onSuccess(GrdConfiguration grdConfiguration);

    /**
     * Perform action when analysis was failed.
     *
     * @param buildLog
     *         fetched log from Gradle tooling api
     */
    void onFailed(String buildLog);
}
