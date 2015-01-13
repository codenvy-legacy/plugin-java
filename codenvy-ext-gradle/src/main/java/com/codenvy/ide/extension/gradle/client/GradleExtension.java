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
package com.codenvy.ide.extension.gradle.client;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.extension.gradle.client.action.UpdateDependencyAction;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;
import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;

/** @author Vladyslav Zhukovskii */
@Singleton
@Extension(title = "Gradle", version = "3.0.0")
public class GradleExtension {

    private DependenciesUpdater dependenciesUpdater;

    @Inject
    public GradleExtension(final EventBus eventBus,
                           final DependenciesUpdater dependenciesUpdater) {
        this.dependenciesUpdater = dependenciesUpdater;

        eventBus.addHandler(ProjectActionEvent.TYPE, openProjectHandler);
        eventBus.addHandler(FileEvent.TYPE, saveBuildScriptHandler);
    }

    /** Register main and context menu actions. */
    @Inject
    private void registerActions(ActionManager actionManager,
                                 UpdateDependencyAction updateDependencyAction) {
        // register actions
        actionManager.registerAction("updateGradleDependency", updateDependencyAction);

        // add actions into main menu
        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);
        buildMenuActionGroup.add(updateDependencyAction);

        // add actions into context menu
        DefaultActionGroup buildContextMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD_CONTEXT_MENU);
        buildContextMenuGroup.addSeparator();
        buildContextMenuGroup.addAction(updateDependencyAction);
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
