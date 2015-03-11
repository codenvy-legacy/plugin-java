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
package org.eclipse.che.gradle.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author Vladyslav Zhukovskii */
@DTO
public interface GrdConfiguration {
    public enum State {
        ACTUAL,
        OUTDATED
    }

    GrdProject getProject();

    void setProject(GrdProject project);

    GrdConfiguration withProject(GrdProject project);

    State getConfigurationState();

    void setConfigurationState(State configurationState);

    GrdConfiguration withConfigurationState(State configurationState);
}
