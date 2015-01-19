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
package com.codenvy.ide.extension.gradle.client.action;

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.extension.gradle.client.GradleResources;
import com.codenvy.ide.extension.gradle.client.build.GradleBuildPresenter;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** @author Vladyslav Zhukovskii */
@Singleton
public class CustomGradleBuildAction extends ProjectAction {

    private AppContext           appContext;
    private GradleBuildPresenter presenter;
    private AnalyticsEventLogger eventLogger;
    private BuildContext         buildContext;

    @Inject
    public CustomGradleBuildAction(AppContext appContext,
                                   GradleResources resources,
                                   GradleBuildPresenter presenter,
                                   AnalyticsEventLogger eventLogger,
                                   BuildContext buildContext) {
        super("Custom build", "Custom Gradle build");
        this.presenter = presenter;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.buildContext = buildContext;
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        CurrentProject project = appContext.getCurrentProject();

        e.getPresentation().setEnabledAndVisible(project != null && GradleAttributes.GRADLE_ID.equals(project.getBuilder()));
        e.getPresentation().setEnabled(!buildContext.isBuilding());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }
}
