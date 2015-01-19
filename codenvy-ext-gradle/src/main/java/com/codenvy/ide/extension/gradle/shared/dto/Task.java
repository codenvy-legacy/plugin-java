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
package com.codenvy.ide.extension.gradle.shared.dto;

import com.codenvy.dto.shared.DTO;

/** @author Vladyslav Zhukovskii */
@DTO
public interface Task {
    String getName();

    void setName(String name);

    Task withName(String name);

    String getDescription();

    void setDescription(String description);

    Task withDescription(String description);
}
