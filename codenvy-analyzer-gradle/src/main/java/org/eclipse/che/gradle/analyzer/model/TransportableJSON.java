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
package org.eclipse.che.gradle.analyzer.model;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Describe object that represent result of model built by {@link GradleModelBuilder}.
 * This object is necessary for communicating Codenvy IDE with Gradle via custom Gradle plugin.
 *
 * @author Vladyslav Zhukovskii
 * @see org.gradle.tooling.provider.model.ToolingModelBuilder
 * @see GradleModelBuilder
 */
public interface TransportableJSON extends Serializable {

    /**
     * Get model built by {@link GradleModelBuilder} that represent a json string for future
     * de-serializing.
     *
     * @return model represented by json string
     */
    @Nonnull
    String getJSON();
}
