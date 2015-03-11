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
package org.eclipse.che.gradle.analyzer.model.builder;


import org.eclipse.che.gradle.analyzer.ModelBuilderService;
import org.eclipse.che.gradle.analyzer.model.TransportableJSON;
import org.eclipse.che.gradle.analyzer.model.TransportableJSONMiscDefinition;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

/**
 * Skeleton of model builder. To quick scalability of model builders.
 *
 * @author Vladyslav Zhukovskii
 */
public class MiscDefinitionBuilder implements ModelBuilderService {
    /** {@inheritDoc} */
    @Override
    public boolean canExec(@Nonnull String requestedModel) {
        return TransportableJSONMiscDefinition.class.getName().equals(requestedModel);
    }

    /** {@inheritDoc} */
    @Override
    public TransportableJSON buildModel(@Nonnull Project project) {
        return new TransportableJSONMiscDefinition() {
            @Nonnull
            @Override
            public String getJSON() {
                throw new UnsupportedOperationException("Not yet implemented.");
            }
        };
    }
}
