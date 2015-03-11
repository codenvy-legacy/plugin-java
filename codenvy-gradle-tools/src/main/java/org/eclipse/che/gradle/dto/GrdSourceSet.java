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

import java.util.List;

/** @author Vladyslav Zhukovskii */
@DTO
public interface GrdSourceSet {
    public enum Type {
        MAIN,
        TEST
    }

    Type getType();

    void setType(Type type);

    GrdSourceSet withType(Type type);

    List<String> getSource();

    void setSource(List<String> source);

    GrdSourceSet withSource(List<String> source);
}
