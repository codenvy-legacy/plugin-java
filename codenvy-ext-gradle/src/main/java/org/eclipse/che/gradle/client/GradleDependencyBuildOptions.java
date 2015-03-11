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
package org.eclipse.che.gradle.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.builder.dto.BuildOptions;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedEvent;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedHandler;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.gradle.dto.GrdProject;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.DependencyBuildOptionProvider;

import java.util.Collections;

import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;

/**
 * Providing build options for Gradle build.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleDependencyBuildOptions implements DependencyBuildOptionProvider,
                                                     ProjectConfigurationReceivedHandler {

    private DtoFactory       dtoFactory;
    private GrdConfiguration grdConfiguration;

    @Inject
    public GradleDependencyBuildOptions(DtoFactory dtoFactory,
                                        EventBus eventBus) {
        this.dtoFactory = dtoFactory;

        eventBus.addHandler(ProjectConfigurationReceivedEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationReceived(ProjectConfigurationReceivedEvent event) {
        this.grdConfiguration = event.getGrdConfiguration();
    }

    /** {@inheritDoc} */
    @Override
    public String getBuilder() {
        return GRADLE_ID;
    }

    /** {@inheritDoc} */
    @Override
    public BuildOptions getOptionsForBinJars(ProjectDescriptor project) {
        GrdProject grdProject = getTargetProject(grdConfiguration.getProject(), project);
        StringBuilder target = new StringBuilder();

        if (":".equals(grdProject.getPath())) {
            target.append("copy-dependencies");
        } else {
            target.append(grdProject.getPath()).append(":copy-dependencies");
        }

        return dtoFactory.createDto(BuildOptions.class)
                         .withBuilderName(GRADLE_ID)
                         .withTargets(Collections.singletonList(target.toString()))
                         .withOptions(Collections.singletonMap("-Psources", "false"));
    }

    /** {@inheritDoc} */
    @Override
    public BuildOptions getOptionsForSrcJars(ProjectDescriptor project) {
        GrdProject grdProject = getTargetProject(grdConfiguration.getProject(), project);
        StringBuilder target = new StringBuilder();

        if (":".equals(grdProject.getPath())) {
            target.append("copy-dependencies");
        } else {
            target.append(grdProject.getPath()).append(":copy-dependencies");
        }

        return dtoFactory.createDto(BuildOptions.class)
                         .withBuilderName(GRADLE_ID)
                         .withTargets(Collections.singletonList(target.toString()))
                         .withOptions(Collections.singletonMap("-Psources", "true"));
    }

    /** Fetch from Gradle project configuration specific project module. */
    private GrdProject getTargetProject(GrdProject grdProject, ProjectDescriptor project) {
        String path = project.getPath();
        if (path.equals(grdProject.getDirectory())) {
            return grdProject;
        } else {
            for (GrdProject child : grdProject.getChild()) {
                GrdProject targetProject = getTargetProject(child, project);
                if (targetProject == null) {
                    continue;
                }

                return targetProject;
            }
        }

        return null;
    }
}
