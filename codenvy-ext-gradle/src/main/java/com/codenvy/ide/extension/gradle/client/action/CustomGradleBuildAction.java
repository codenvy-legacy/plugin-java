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

import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.GradleResources;
import com.codenvy.ide.extension.gradle.client.build.GradleBuildPresenter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Custom Gradle build action button.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class CustomGradleBuildAction extends GradleBaseAction {

    private GradleBuildPresenter presenter;
    private BuildContext         buildContext;

    @Inject
    public CustomGradleBuildAction(GradleResources resources,
                                   GradleBuildPresenter presenter,
                                   GradleLocalizationConstant localization,
                                   BuildContext buildContext) {
        super(localization.customBuildActionText(), localization.customBuildActionDescription(), resources.build());
        this.presenter = presenter;
        this.buildContext = buildContext;
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setEnabled(!buildContext.isBuilding());
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showDialog();
    }
}
