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
package com.codenvy.ide.gradle.analyzer.internal;

/**
 * @author Vladyslav Zhukovskyi
 */
public interface AnalyzeRequest {
    long getId();

    void setId(long id);

    AnalyzeRequest withId(long id);

    String getWorkspace();

    void setWorkspace(String workspace);

    AnalyzeRequest withWorkspace(String workspace);

    /** Name of project which represents sources on the ide side. */
    String getProject();

    void setProject(String project);

    AnalyzeRequest withProject(String project);

    long getLifetime();

    void setLifetime(long time);

    AnalyzeRequest withLifetime(long time);
}
