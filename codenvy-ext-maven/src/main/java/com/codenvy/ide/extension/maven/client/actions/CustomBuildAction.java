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
package com.codenvy.ide.extension.maven.client.actions;

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.extension.maven.client.MavenLocalizationConstant;
import com.codenvy.ide.extension.maven.client.MavenResources;
import com.codenvy.ide.extension.maven.client.build.MavenBuildPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to build current project.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class CustomBuildAction extends ProjectAction {

    private final AppContext           appContext;
    private final MavenBuildPresenter  presenter;
    private final AnalyticsEventLogger eventLogger;
    private       BuildContext         buildContext;

    @Inject
    public CustomBuildAction(MavenBuildPresenter presenter,
                             MavenResources resources,
                             MavenLocalizationConstant localizationConstant,
                             AppContext appContext,
                             AnalyticsEventLogger eventLogger,
                             BuildContext buildContext) {
        super(localizationConstant.buildProjectControlTitle(),
              localizationConstant.buildProjectControlDescription(), resources.build());
        this.presenter = presenter;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
        this.buildContext = buildContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void updateProjectAction(ActionEvent e) {
        final String builder = appContext.getCurrentProject().getBuilder();
        if ("maven".equals(builder)) {
            e.getPresentation().setEnabledAndVisible(true);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
        if (buildContext.isBuilding()) {
            e.getPresentation().setEnabled(false);
        }
    }
}
