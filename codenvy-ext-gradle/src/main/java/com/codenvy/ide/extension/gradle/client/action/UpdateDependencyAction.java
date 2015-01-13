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
package com.codenvy.ide.extension.gradle.client.action;

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** @author Vladyslav Zhukovskii */
@Singleton
public class UpdateDependencyAction extends ProjectAction {

    private final AppContext           appContext;
    private final AnalyticsEventLogger eventLogger;
    private final DependenciesUpdater  dependenciesUpdater;
    private       BuildContext         buildContext;

    @Inject
    public UpdateDependencyAction(AppContext appContext,
                                  AnalyticsEventLogger eventLogger,
                                  JavaResources resources,
                                  BuildContext buildContext,
                                  DependenciesUpdater dependenciesUpdater) {
        super("Update Dependencies", "Update Dependencies", resources.updateDependencies());
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.buildContext = buildContext;
        this.dependenciesUpdater = dependenciesUpdater;
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        if (!buildContext.isBuilding() &&
            appContext.getCurrentProject() != null &&
            GradleAttributes.GRADLE_ID.equals(appContext.getCurrentProject().getBuilder())) {
            e.getPresentation().setEnabledAndVisible(true);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject != null) {
            eventLogger.log(this);
            dependenciesUpdater.updateDependencies(currentProject.getProjectDescription(), true);
        }
    }
}
