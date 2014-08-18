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
package com.codenvy.ide.extension.maven.client;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.Anchor;
import com.codenvy.ide.api.action.Constraints;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.projecttree.TreeStructureProviderRegistry;
import com.codenvy.ide.api.projecttype.wizard.ProjectTypeWizardRegistry;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizard;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.builder.client.BuilderLocalizationConstant;
import com.codenvy.ide.extension.maven.client.actions.CustomBuildAction;
import com.codenvy.ide.extension.maven.client.tree.MavenProjectTreeStructureProvider;
import com.codenvy.ide.extension.maven.client.wizard.MavenPagePresenter;
import com.codenvy.ide.extension.runner.client.wizard.SelectRunnerPagePresenter;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;

/**
 * Builder extension entry point.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Building Maven project", version = "3.0.0")
public class MavenExtension {
    /** Create extension. */
    @Inject
    public MavenExtension(MavenLocalizationConstant localizationConstants,
                          BuilderLocalizationConstant builderLocalizationConstant,
                          ActionManager actionManager,
                          CustomBuildAction customBuildAction,
                          Provider<MavenPagePresenter> mavenPagePresenter,
                          Provider<SelectRunnerPagePresenter> runnerPagePresenter,
                          ProjectTypeWizardRegistry wizardRegistry,
                          NotificationManager notificationManager,
                          TreeStructureProviderRegistry treeStructureProviderRegistry,
                          EventBus eventBus,
                          AppContext appContext,
                          ProjectServiceClient projectServiceClient,
                          DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        actionManager.registerAction(localizationConstants.buildProjectControlId(), customBuildAction);

        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);
        buildMenuActionGroup.add(customBuildAction, new Constraints(Anchor.AFTER, builderLocalizationConstant.buildProjectControlId()));

        ProjectWizard wizard = new ProjectWizard(notificationManager);
        wizard.addPage(mavenPagePresenter);
        wizard.addPage(runnerPagePresenter);
        wizardRegistry.addWizard(Constants.MAVEN_ID, wizard);

        treeStructureProviderRegistry.registerTreeStructureProvider(Constants.MAVEN_ID,
                                                                    new MavenProjectTreeStructureProvider(eventBus,
                                                                                                          appContext,
                                                                                                          projectServiceClient,
                                                                                                          dtoUnmarshallerFactory));
    }
}