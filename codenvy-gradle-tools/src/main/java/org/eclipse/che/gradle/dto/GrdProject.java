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
import java.util.Map;

/** @author Vladyslav Zhukovskii */
@DTO
public interface GrdProject {
    String getName();

    void setName(String name);

    GrdProject withName(String name);

    String getPath();

    void setPath(String path);

    GrdProject withPath(String path);

    String getDescription();

    void setDescription(String description);

    GrdProject withDescription(String description);

    String getDirectory();

    void setDirectory(String directory);

    GrdProject withDirectory(String directory);

    List<GrdSourceSet> getSourceSet();

    void setSourceSet(List<GrdSourceSet> sourceSet);

    GrdProject withSourceSet(List<GrdSourceSet> sourceSet);

    List<GrdTask> getTasks();

    void setTasks(List<GrdTask> tasks);

    GrdProject withTasks(List<GrdTask> tasks);

    List<String> getDefaultBuildTasks();

    void setDefaultBuildTasks(List<String> defaultBuildTasks);

    GrdProject withDefaultBuildTasks(List<String> defaultBuildTasks);

    List<GrdProject> getChild();

    void setChild(List<GrdProject> child);

    GrdProject withChild(List<GrdProject> child);

    Map<String, GrdPlugin> getPlugins();

    void setPlugins(Map<String, GrdPlugin> plugins);

    GrdProject withPlugins(Map<String, GrdPlugin> plugins);
}
