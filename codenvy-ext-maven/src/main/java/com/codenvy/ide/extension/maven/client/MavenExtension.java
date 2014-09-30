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

import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.constraints.Anchor;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.projecttree.TreeStructureProviderRegistry;
import com.codenvy.ide.api.projecttype.wizard.PreSelectedProjectTypeManager;
import com.codenvy.ide.api.projecttype.wizard.ProjectTypeWizardRegistry;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizard;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.builder.client.BuilderLocalizationConstant;
import com.codenvy.ide.extension.maven.client.actions.CreateMavenModuleAction;
import com.codenvy.ide.extension.maven.client.actions.CustomBuildAction;
import com.codenvy.ide.extension.maven.client.actions.UpdateDependencyAction;
import com.codenvy.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import com.codenvy.ide.extension.maven.client.wizard.MavenPagePresenter;
import com.codenvy.ide.extension.runner.client.wizard.SelectRunnerPagePresenter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;
import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;
import static com.codenvy.ide.api.action.IdeActions.GROUP_FILE_NEW;

/**
 * Maven extension entry point.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Building Maven project", version = "3.0.0")
public class MavenExtension {

    @Inject
    public MavenExtension(Provider<MavenPagePresenter> mavenPagePresenter,
                          Provider<SelectRunnerPagePresenter> runnerPagePresenter,
                          ProjectTypeWizardRegistry wizardRegistry,
                          NotificationManager notificationManager,
                          TreeStructureProviderRegistry treeStructureProviderRegistry,
                          MavenProjectTreeStructureProvider mavenProjectTreeStructureProvider,
                          PreSelectedProjectTypeManager preSelectedProjectManager) {

        ProjectWizard wizard = new ProjectWizard(notificationManager);
        wizard.addPage(mavenPagePresenter);
        wizard.addPage(runnerPagePresenter);
        wizardRegistry.addWizard(Constants.MAVEN_ID, wizard);

        treeStructureProviderRegistry.registerProvider(Constants.MAVEN_ID, mavenProjectTreeStructureProvider);

        preSelectedProjectManager.setProjectTypeIdToPreselect(Constants.MAVEN_ID, 100);
    }

    @Inject
    private void prepareActions(ActionManager actionManager,
                                CustomBuildAction customBuildAction,
                                UpdateDependencyAction updateDependencyAction,
                                MavenLocalizationConstant mavenLocalizationConstants,
                                BuilderLocalizationConstant builderLocalizationConstant,
                                CreateMavenModuleAction createMavenModuleAction) {
        // register actions
        actionManager.registerAction(mavenLocalizationConstants.buildProjectControlId(), customBuildAction);
        actionManager.registerAction("updateDependency", updateDependencyAction);
        actionManager.registerAction("createMavenModule", createMavenModuleAction);

        // add actions in main menu
        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);
        buildMenuActionGroup.add(customBuildAction, new Constraints(Anchor.AFTER, builderLocalizationConstant.buildProjectControlId()));
        buildMenuActionGroup.add(updateDependencyAction);

        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.add(createMavenModuleAction, new Constraints(Anchor.AFTER, "newProject"));

        // add actions in context menu
        DefaultActionGroup buildContextMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD_CONTEXT_MENU);
        buildContextMenuGroup.addSeparator();
        buildContextMenuGroup.addAction(updateDependencyAction);
    }

    @Inject
    private void registerIcons(IconRegistry iconRegistry, MavenResources mavenResources) {
        iconRegistry.registerIcon(new Icon("maven.module", mavenResources.module()));
    }
}
