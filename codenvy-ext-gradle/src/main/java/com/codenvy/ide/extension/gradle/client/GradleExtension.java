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
package com.codenvy.ide.extension.gradle.client;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.projecttree.TreeStructureProviderRegistry;
import com.codenvy.ide.api.projecttype.wizard.ProjectTypeWizardRegistry;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizard;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.extension.gradle.client.action.CustomGradleBuildAction;
import com.codenvy.ide.extension.gradle.client.action.ShowTaskListAction;
import com.codenvy.ide.extension.gradle.client.action.UpdateDependencyAction;
import com.codenvy.ide.extension.gradle.client.projecttree.GradleProjectTreeStructureProvider;
import com.codenvy.ide.extension.gradle.client.wizard.GradlePagePresenter;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.codenvy.ide.extension.runner.client.wizard.SelectRunnerPagePresenter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;
import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;
import static com.codenvy.ide.api.action.IdeActions.GROUP_VIEW;

/** @author Vladyslav Zhukovskii */
@Singleton
@Extension(title = "Gradle", version = "3.0.0")
public class GradleExtension {

    private DependenciesUpdater dependenciesUpdater;

    @Inject
    public GradleExtension(EventBus eventBus,
                           DependenciesUpdater dependenciesUpdater,
                           NotificationManager notificationManager,
                           Provider<SelectRunnerPagePresenter> runnerPagePresenter,
                           Provider<GradlePagePresenter> gradlePagePresenter,
                           ProjectTypeWizardRegistry projectTypeWizardRegistry,
                           TreeStructureProviderRegistry treeStructureProviderRegistry) {
        this.dependenciesUpdater = dependenciesUpdater;


        ProjectWizard wizard = new ProjectWizard(notificationManager);
        wizard.addPage(gradlePagePresenter);
        wizard.addPage(runnerPagePresenter);

        projectTypeWizardRegistry.addWizard(GradleAttributes.GRADLE_ID, wizard);

        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(GradleAttributes.GRADLE_ID, GradleProjectTreeStructureProvider.ID);

        eventBus.addHandler(ProjectActionEvent.TYPE, openProjectHandler);
        eventBus.addHandler(FileEvent.TYPE, saveBuildScriptHandler);
    }

    /** Register main and context menu actions. */
    @Inject
    private void registerActions(ActionManager actionManager,
                                 UpdateDependencyAction updateDependencyAction,
                                 ShowTaskListAction showTaskListAction,
                                 CustomGradleBuildAction customGradleBuildAction) {
        // register actions
        actionManager.registerAction("updateGradleDependency", updateDependencyAction);
        actionManager.registerAction("showGradleProjectTasks", showTaskListAction);
        actionManager.registerAction("customGradleBuild", customGradleBuildAction);

        // add actions into main menu
        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);
        buildMenuActionGroup.add(updateDependencyAction);
        buildMenuActionGroup.add(customGradleBuildAction);

        // add actions into view menu
        DefaultActionGroup viewMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_VIEW);
        viewMenuActionGroup.add(showTaskListAction);

        // add actions into context menu
        DefaultActionGroup buildContextMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD_CONTEXT_MENU);
        buildContextMenuGroup.addSeparator();
        buildContextMenuGroup.addAction(updateDependencyAction);
    }

    @Inject
    private void registerIcons(IconRegistry iconRegistry, GradleResources resources) {
        iconRegistry.registerIcon(new Icon("gradle.module", resources.task()));
        // icons for file names
        iconRegistry.registerIcon(new Icon("gradle/gradle.file.small.icon", resources.task()));
//        iconRegistry.registerIcon(new Icon("gradle/settings.gradle.file.small.icon", resources.task()));
    }

    /** Handle open project event and fire update dependency action. */
    ProjectActionHandler openProjectHandler = new ProjectActionHandler() {
        @Override
        public void onProjectOpened(ProjectActionEvent event) {
            ProjectDescriptor project = event.getProject();

            if (GradleAttributes.GRADLE_ID.equals(project.getType())) {
                dependenciesUpdater.updateDependencies(project, false);
            }
        }

        @Override
        public void onProjectClosed(ProjectActionEvent event) {
            //nothing to do
        }
    };

    /** Handle file save event and in case if saved file is "build.gradle" update dependencies action should be fired. */
    FileEventHandler saveBuildScriptHandler = new FileEventHandler() {
        @Override
        public void onFileOperation(FileEvent event) {
            ProjectDescriptor project = event.getFile().getProject().getData();

            if (GradleAttributes.GRADLE_ID.equals(project.getType()) &&
                FileEvent.FileOperation.SAVE == event.getOperationType() &&
                "build.gradle".equals(event.getFile().getName())) {
                dependenciesUpdater.updateDependencies(project, true);
            }
        }
    };
}
