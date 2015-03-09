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
package org.eclipse.che.ide.extension.maven.client.actions;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.extension.maven.client.MavenLocalizationConstant;
import org.eclipse.che.ide.extension.maven.client.MavenResources;
import org.eclipse.che.ide.extension.maven.client.build.MavenBuildPresenter;

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
