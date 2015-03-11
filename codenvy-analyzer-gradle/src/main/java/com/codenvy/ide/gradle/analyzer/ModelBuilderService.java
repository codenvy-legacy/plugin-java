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
package com.codenvy.ide.gradle.analyzer;

import com.codenvy.ide.gradle.analyzer.model.TransportableJSON;

import org.gradle.api.Project;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Service that allow Gradle to build specified model.
 *
 * @author Vladyslav Zhukovskii
 */
public interface ModelBuilderService extends Serializable {
    /**
     * Check if current service can build model requested.
     *
     * @param requestedModel
     *         class name passed from {@link com.codenvy.ide.gradle.analyzer.GradleModelBuilder}.
     * @return true if current service can build requested model, otherwise false
     */
    boolean canExec(@Nonnull String requestedModel);

    /**
     * Build model by specified class name requested from Codenvy via Gradle plugin in {@link com.codenvy.ide.gradle.analyzer
     * .GradleModelBuilder}.
     *
     * @param project
     *         Gradle project object which constructed via Gradle on evaluation step
     * @return {@link com.codenvy.ide.gradle.analyzer.model.TransportableJSON} object that contains json string of built model
     */
    TransportableJSON buildModel(@Nonnull Project project);
}
