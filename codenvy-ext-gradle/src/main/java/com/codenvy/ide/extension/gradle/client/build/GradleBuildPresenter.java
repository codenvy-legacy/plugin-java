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
package com.codenvy.ide.extension.gradle.client.build;

import com.codenvy.api.builder.dto.BuildOptions;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.builder.client.build.BuildController;
import com.codenvy.ide.extension.gradle.client.event.ProjectConfigurationReceivedEvent;
import com.codenvy.ide.extension.gradle.client.event.ProjectConfigurationReceivedHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

/**
 * Custom Gradle build Presenter.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleBuildPresenter implements GradleBuildView.ActionDelegate, ProjectConfigurationReceivedHandler {

    private BuildController buildController;
    private GradleBuildView view;
    private DtoFactory      dtoFactory;
    private List<String>    defaultModelTargets;

    @Inject
    public GradleBuildPresenter(BuildController buildController,
                                GradleBuildView view,
                                DtoFactory dtoFactory) {
        this.buildController = buildController;
        this.view = view;
        this.dtoFactory = dtoFactory;

        this.view.setDelegate(this);
    }

    /** Shows custom build dialog. */
    public void showDialog() {
        StringBuilder targets = new StringBuilder();
        if (defaultModelTargets == null || defaultModelTargets.isEmpty()) {
            targets.append("build");
        } else {
            for (String target : defaultModelTargets) {
                if (!target.isEmpty()) {
                    targets.append(" ");
                }
                targets.append(target);
            }
        }

        this.view.setBuildCommand(targets.toString());
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onStartBuildClicked() {
        buildController.buildActiveProject(getBuildOptions(), true);
        view.close();
    }

    private BuildOptions getBuildOptions() {
        BuildOptions buildOptions = dtoFactory.createDto(BuildOptions.class);
        buildOptions.setSkipTest(view.isSkipTestSelected());

        if (view.isOfflineWorkSelected()) {
            buildOptions.getOptions().put("--offline", "");
        }

        if (view.isRefreshDependenciesSelected()) {
            buildOptions.getOptions().put("--refresh-dependencies", "");
        }

        buildOptions.getTargets().add(view.getBuildCommand());

        return buildOptions;
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationReceived(ProjectConfigurationReceivedEvent event) {
        defaultModelTargets = event.getGrdConfiguration().getProject().getDefaultBuildTasks();
    }
}
