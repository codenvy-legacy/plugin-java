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

import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.maven.client.MavenLocalizationConstant;
import com.codenvy.ide.extension.maven.client.module.CreateMavenModulePresenter;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CreateMavenModuleAction extends Action {


    private AppContext                 context;
    private CreateMavenModulePresenter presenter;

    @Inject
    public CreateMavenModuleAction(AppContext context, MavenLocalizationConstant constant, CreateMavenModulePresenter presenter) {
        super(constant.actionCreateMavenModuleText(), constant.actionCreateMavenModuleDescription());
        this.context = context;
        this.presenter = presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (context.getCurrentProject() != null) {
            presenter.showDialog(context.getCurrentProject());
        }
    }

    @Override
    public void update(ActionEvent e) {
        CurrentProject currentProject = context.getCurrentProject();
        if (currentProject != null &&
            Constants.MAVEN_ID.equals(currentProject.getProjectDescription().getProjectTypeId())) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setEnabled("pom".equals(currentProject.getAttributeValue(MavenAttributes.PACKAGING)));
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}
