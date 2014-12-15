/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.generator.archetype.dto;

import com.codenvy.dto.shared.DTO;

/**
 * Generate task status.
 *
 * @author Artem Zatsarynnyy
 */
@DTO
public interface GenerateTask {
    Status getStatus();

    void setStatus(Status status);

    GenerateTask withStatus(Status status);

    String getStatusUrl();

    void setStatusUrl(String StatusUrl);

    GenerateTask withStatusUrl(String StatusUrl);

    String getDownloadUrl();

    void setDownloadUrl(String downloadUrl);

    GenerateTask withDownloadUrl(String downloadUrl);

    String getReport();

    void setReport(String report);

    GenerateTask withReport(String report);

    enum Status {
        IN_PROGRESS,
        SUCCESSFUL,
        FAILED
    }
}