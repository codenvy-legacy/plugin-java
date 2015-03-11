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
package org.eclipse.che.gradle.server.project;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.*;
import org.eclipse.che.api.project.server.*;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.api.project.shared.Builders;
import org.eclipse.che.gradle.DistributionType;
import org.eclipse.che.gradle.GradleUtils;
import org.eclipse.che.gradle.analyzer.ModelRequest;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.gradle.dto.GrdProject;
import org.eclipse.che.gradle.dto.GrdSourceSet;
import org.eclipse.che.gradle.server.project.analyzer.GradleProjectAnalyzerClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.gradle.shared.GradleAttributes.DISTRIBUTION_TYPE;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.gradle.shared.GradleAttributes.PROJECT_CONTENT_MODIFY_OPTION;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;
import static org.eclipse.che.gradle.DistributionType.NONE;
import static org.eclipse.che.gradle.dto.GrdConfiguration.State.OUTDATED;
import static org.eclipse.che.gradle.dto.GrdSourceSet.Type.MAIN;
import static org.eclipse.che.gradle.dto.GrdSourceSet.Type.TEST;

/**
 * Creating Gradle project structure based on stored Gradle project configuration.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleProjectResolver {
    private static final Logger LOG = LoggerFactory.getLogger(GradleProjectResolver.class);

    private GradleProjectAnalyzerClientService analyzerService;
    private ProjectManager                     projectManager;

    @Inject
    public GradleProjectResolver(GradleProjectAnalyzerClientService analyzerService,
                                 ProjectManager projectManager) {
        this.analyzerService = analyzerService;
        this.projectManager = projectManager;
    }

    /** Perform resolving Gradle project. */
    public boolean resolve(FolderEntry projectFolder)
            throws ServerException, ValueStorageException, InvalidValueException, ProjectTypeConstraintException {
        try {
            final ProjectType projectType = projectManager.getProjectTypeRegistry().getProjectType(GRADLE_ID);

            if (projectType == null) {
                throw new ServerException(String.format("Project type '%s' not registered. ", GRADLE_ID));
            }

            DistributionType distributionType = GradleUtils.getProjectDistribution(projectFolder);

            if (distributionType == NONE) {
                //this means that we don't have gradle files inside user's project
                return false;
            }

            LOG.info("Gradle project resolving has been started.");

            GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(projectFolder);
            if (grdConfiguration.getConfigurationState() == OUTDATED) {
                grdConfiguration = analyzerService.getGrdConfiguration(projectFolder, ModelRequest.PROJECT);
                GradleUtils.writeGrdConfiguration(projectFolder, grdConfiguration);
            }

            if (grdConfiguration.getConfigurationState() == OUTDATED) {
                //nothing suspicious, we can still get problem grdConfiguration again
                //in this case may be went something wrong on the builder side
                LOG.info("Gradle project resolving has been stopped. There are not modules to process.");
                return true;
            }

            GrdProject grdProject = grdConfiguration.getProject();

            Project project = new Project(projectFolder, projectManager);
            project.updateConfig(getProjectConfig(grdProject, projectType, distributionType));

            updateProjectConfigurationRecursively(project, grdProject, projectType, distributionType, projectManager);

            LOG.info("Gradle project resolving has been stopped.");
            return true;
        } catch (ApiException e) {
            throw new ServerException("An error occurred when trying to resolve gradle project.", e);
        }
    }

    /** Create Gradle project structure recursively. */
    private void updateProjectConfigurationRecursively(Project parentProject,
                                                       GrdProject grdProject,
                                                       ProjectType projectType,
                                                       DistributionType distributionType,
                                                       ProjectManager projectManager)
            throws ServerException, ForbiddenException, ConflictException, NotFoundException {
        for (GrdProject grdChild : grdProject.getChild()) {
            FolderEntry moduleEntry = (FolderEntry)parentProject.getBaseFolder().getParent().getChild(grdChild.getDirectory());
            if (moduleEntry != null) {
                Project childModule = projectManager.getProject(moduleEntry.getWorkspace(), moduleEntry.getPath());
                if (childModule == null) {
                    childModule = new Project(moduleEntry, projectManager);
                }

                ProjectConfig projectConfig = getProjectConfig(grdChild, projectType, distributionType);
                childModule.updateConfig(projectConfig);

//                String childModulePath = grdChild.getDirectory().startsWith("/") ? grdChild.getDirectory().substring(1)
//                                                                                 : grdChild.getDirectory();

                String childModulePath = grdChild.getName();

                Set<String> storedSubModules = parentProject.getModules().get();
                if (!storedSubModules.contains(childModulePath)) {
                    projectManager.addModule(moduleEntry.getWorkspace(),
                                             parentProject.getPath(),
                                             childModulePath,
                                             projectConfig,
                                             Collections.singletonMap(PROJECT_CONTENT_MODIFY_OPTION, "false"),
                                             parentProject.getVisibility());
                }

                updateProjectConfigurationRecursively(childModule, grdChild, projectType, distributionType, projectManager);
            }
        }
    }

    /** Get default configuration for new project. */
    private ProjectConfig getProjectConfig(GrdProject grdProject, ProjectType projectType, DistributionType distributionType)
            throws ServerException, ForbiddenException {

        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(DISTRIBUTION_TYPE, new AttributeValue(distributionType.toString()));

        setUpSourceSet(attributes, grdProject);

        Builders builders = new Builders(GRADLE_ID);

        String description = Strings.isNullOrEmpty(grdProject.getDescription()) ? "Gradle project"
                                                                                : grdProject.getDescription();

        return new ProjectConfig(description, projectType.getId(), attributes, null, builders, null);
    }

    /** Fetch source paths for new project. */
    private void setUpSourceSet(Map<String, AttributeValue> attributes, GrdProject grdProject) {
        for (GrdSourceSet grdSourceSet : grdProject.getSourceSet()) {
            if (grdSourceSet.getType() == MAIN) {
                attributes.put(SOURCE_FOLDER, new AttributeValue(grdSourceSet.getSource()));
            } else if (grdSourceSet.getType() == TEST) {
                attributes.put(TEST_FOLDER, new AttributeValue(grdSourceSet.getSource()));
            }
        }
    }
}
