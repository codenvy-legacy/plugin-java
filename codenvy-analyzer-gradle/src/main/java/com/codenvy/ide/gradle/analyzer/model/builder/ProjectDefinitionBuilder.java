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
package com.codenvy.ide.gradle.analyzer.model.builder;

import com.codenvy.ide.gradle.analyzer.ModelBuilderService;
import com.codenvy.ide.gradle.analyzer.model.TransportableJSON;
import com.codenvy.ide.gradle.analyzer.model.TransportableJSONProjectDefinition;
import com.codenvy.ide.gradle.dto.*;
import com.codenvy.ide.gradle.dto.server.DtoServerImpls;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

import static com.codenvy.ide.gradle.dto.GrdSourceSet.Type.MAIN;
import static com.codenvy.ide.gradle.dto.GrdSourceSet.Type.TEST;

/**
 * Gradle project model builder. Build model allow Codenvy to cooperate with gradle project on high level.
 * This model may fetch various information from project e.g. name, description, source sets, tasks, plugins etc.
 *
 * @author Vladyslav Zhukovskii
 */
public class ProjectDefinitionBuilder implements ModelBuilderService {
    /** {@inheritDoc} */
    @Override
    public boolean canExec(@Nonnull String requestedModel) {
        return TransportableJSONProjectDefinition.class.getName().equals(requestedModel);
    }

    /** {@inheritDoc} */
    @Override
    public TransportableJSON buildModel(@Nonnull final Project project) {
        DtoServerImpls.GrdProjectImpl calculatedProject = getProjectRecursive(project, project);
        final String transportableJson = calculatedProject.toJson();

        return new TransportableJSONProjectDefinition() {
            @Nonnull
            @Override
            public String getJSON() {
                return transportableJson;
            }
        };
    }

    /** Construct model recursively from Gradle project. */
    private DtoServerImpls.GrdProjectImpl getProjectRecursive(Project rootProject, Project project) {
        DtoServerImpls.GrdProjectImpl grdProject = DtoServerImpls.GrdProjectImpl.make();
        grdProject.setName(project.getName());
        grdProject.setDescription(project.getDescription());
        grdProject.setPath(project.getPath());
        grdProject.setDefaultBuildTasks(project.getDefaultTasks());
        grdProject.setDirectory(getRelModulePath(rootProject.getProjectDir().getParentFile(), project.getProjectDir()));
        grdProject.setTasks(getTasks(project));
        grdProject.setSourceSet(getSourceSet(project));
        grdProject.setPlugins(getPlugins(project));

        List<GrdProject> grdChildrenList = new ArrayList<>();

        for (Project child : project.getChildProjects().values()) {
            grdChildrenList.add(getProjectRecursive(rootProject, child));
        }

        grdProject.setChild(grdChildrenList);
        return grdProject;
    }

    /**
     * Fetch available project tasks.
     *
     * @see org.gradle.api.tasks.TaskContainer
     */
    private List<GrdTask> getTasks(Project project) {
        List<GrdTask> tasks = new ArrayList<>();

        TaskContainer taskContainer = project.getTasks();
        if (taskContainer == null || taskContainer.isEmpty()) {
            return tasks;
        }

        for (Task task : taskContainer) {
            GrdTask grdTask = DtoServerImpls.GrdTaskImpl.make();
            grdTask.setName(task.getName());
            grdTask.setDescription(task.getDescription());
            grdTask.setPath(task.getPath());
            grdTask.setEnabled(task.getEnabled());
            tasks.add(grdTask);
        }

        return tasks;
    }

    /**
     * Fetch source path configuration.
     *
     * @see org.gradle.api.internal.tasks.DefaultSourceSetContainer
     */
    private List<GrdSourceSet> getSourceSet(Project project) {
        List<GrdSourceSet> grdSourceSets = new ArrayList<>();

        Object oJavaPlugin = project.getConvention().getPlugins().get("java");
        if (oJavaPlugin != null && oJavaPlugin instanceof JavaPluginConvention) {
            JavaPluginConvention jConv = (JavaPluginConvention)oJavaPlugin;
            SourceSetContainer sourceSets = jConv.getSourceSets();
            if (sourceSets.isEmpty()) {
                return grdSourceSets;
            }

            for (SourceSet sourceSet : sourceSets) {
                List<String> srcDirs = new ArrayList<>();
                for (File dir : sourceSet.getJava().getSrcDirs()) {
                    String path = getRelModulePath(project.getProjectDir(), dir);
                    srcDirs.add(path.startsWith("/") ? path.substring(1) : path);
                }

                GrdSourceSet.Type type = sourceSet.getName().toLowerCase().contains("test") ? TEST : MAIN;

                if (grdSourceSets.isEmpty()) {
                    GrdSourceSet grdSourceSet = DtoServerImpls.GrdSourceSetImpl.make();
                    grdSourceSet.setType(type);
                    grdSourceSet.setSource(srcDirs);
                    grdSourceSets.add(grdSourceSet);
                    continue;
                }

                GrdSourceSet grdSourceSetToUpdate = null;
                for (GrdSourceSet grdSourceSet : grdSourceSets) {
                    if (grdSourceSet.getType() == type) {
                        grdSourceSetToUpdate = grdSourceSet;
                        break;
                    }
                }

                if (grdSourceSetToUpdate == null) {
                    GrdSourceSet grdSourceSet = DtoServerImpls.GrdSourceSetImpl.make();
                    grdSourceSet.setType(type);
                    grdSourceSet.setSource(srcDirs);
                    grdSourceSets.add(grdSourceSet);
                } else {
                    grdSourceSetToUpdate.getSource().addAll(srcDirs);
                }
            }
        }

        return grdSourceSets;
    }

    /**
     * Fetch plugin configuration.
     *
     * @see org.gradle.api.plugins.Convention#getPlugins()
     */
    private Map<String, GrdPlugin> getPlugins(Project project) {
        Map<String, GrdPlugin> plugins = new HashMap<>();

        for (Map.Entry<String, Object> pluginEntry : project.getConvention().getPlugins().entrySet()) {
            GrdPlugin grdPlugin = DtoServerImpls.GrdPluginImpl.make();
            grdPlugin.setName(pluginEntry.getKey());
            grdPlugin.setConvention(getPluginConvention(pluginEntry.getKey(), pluginEntry.getValue()));
            plugins.put(pluginEntry.getKey(), grdPlugin);
        }

        return plugins;
    }

    /**
     * Fetch plugin convention which is responsible for storing various attributes related to those plugins.
     *
     * @see org.gradle.api.plugins.Convention
     */
    private GrdPluginConvention getPluginConvention(String pluginName, Object plugin) {
        GrdPluginConvention grdPluginConvention = DtoServerImpls.GrdPluginConventionImpl.make();
        if (!isDefaultPlugin(pluginName)) {
            return grdPluginConvention;
        }

        //TODO logic to fetching plugin convention
        return grdPluginConvention;
    }

    /**
     * Check if used plugin is a part of default plugin list used in Gradle distribution.
     * This need to allow in future to fetch the necessary information from specified plugin.
     *
     * @see org.gradle.api.Plugin
     */
    private boolean isDefaultPlugin(String pluginName) {
        String[] languagePlugins = new String[]{"java", "groovy", "scala", "antlr"};
        String[] incubatingLanguagePlugins = new String[]{"assembler", "c", "cpp", "objective-c", "objective-cpp", "windows-resource"};
        String[] integrationPlugins = new String[]{"application", "ear", "jetty", "maven", "osgi", "war"};
        String[] incubatingIntegrationPlugins = new String[]{"distribution", "java-library-distribution", "ivy-publish", "maven-publish"};
        String[] softwareDevPlugins = new String[]{"announce", "build-announcements", "checkstyle", "codenarc", "eclipse", "eclipse-wtp",
                                                   "findbugs", "idea", "jdepend", "pmd", "project-report", "signing", "sonar"};
        String[] incubatingSoftwareDevPlugins = new String[]{"build-dashboard", "build-init", "cunit", "jacoco", "sonar-runner",
                                                             "visual-studio", "wrapper", "java-gradle-plugin"};
        String[] basePlugins = new String[]{"base", "java-base", "groovy-base", "scala-base", "reporting-base"};

        return !(Arrays.binarySearch(languagePlugins, pluginName) == -1 ||
                 Arrays.binarySearch(incubatingLanguagePlugins, pluginName) == -1 ||
                 Arrays.binarySearch(integrationPlugins, pluginName) == -1 ||
                 Arrays.binarySearch(incubatingIntegrationPlugins, pluginName) == -1 ||
                 Arrays.binarySearch(softwareDevPlugins, pluginName) == -1 ||
                 Arrays.binarySearch(incubatingSoftwareDevPlugins, pluginName) == -1 ||
                 Arrays.binarySearch(basePlugins, pluginName) == -1);
    }

    /** Get relative path to base. */
    private String getRelModulePath(java.io.File rootProject, java.io.File subProject) {
        String rel = rootProject.toPath().relativize(subProject.toPath()).toString();

        return rel.startsWith("/") ? rel : "/" + rel;
    }
}
