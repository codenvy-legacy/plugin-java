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
import com.codenvy.ide.api.constraints.Anchor;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.TreeStructureProviderRegistry;
import com.codenvy.ide.api.projecttype.wizard.PreSelectedProjectTypeManager;
import com.codenvy.ide.extension.gradle.client.action.AnalyzeProjectAction;
import com.codenvy.ide.extension.gradle.client.action.CreateGradleModuleAction;
import com.codenvy.ide.extension.gradle.client.action.CustomGradleBuildAction;
import com.codenvy.ide.extension.gradle.client.action.ShowTaskListAction;
import com.codenvy.ide.extension.gradle.client.action.UpdateDependencyAction;
import com.codenvy.ide.extension.gradle.client.projecttree.GradleProjectTreeStructureProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;
import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;
import static com.codenvy.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.TEST_FOLDER;

/** @author Vladyslav Zhukovskii */
@Singleton
@Extension(title = "Gradle", version = "3.0.0")
public class GradleExtension {

    @Inject
    public GradleExtension(TreeStructureProviderRegistry treeStructureProviderRegistry,
                           PreSelectedProjectTypeManager preSelectedProjectManager,
                           GradleResources resources) {
        resources.getCSS().ensureInjected();

        preSelectedProjectManager.setProjectTypeIdToPreselect(GRADLE_ID, 100);

        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(GRADLE_ID, GradleProjectTreeStructureProvider.ID);
    }

    /** Register main and context menu actions. */
    @Inject
    private void registerActions(ActionManager actionManager,
                                 UpdateDependencyAction updateDependencyAction,
                                 ShowTaskListAction showTaskListAction,
                                 CustomGradleBuildAction customGradleBuildAction,
                                 CreateGradleModuleAction createGradleModuleAction,
                                 AnalyzeProjectAction analyzeProjectAction) {
        // register actions
        actionManager.registerAction("updateGradleDependency", updateDependencyAction);
        actionManager.registerAction("showGradleProjectTasks", showTaskListAction);
        actionManager.registerAction("customGradleBuild", customGradleBuildAction);
        actionManager.registerAction("createGradleModule", createGradleModuleAction);
        actionManager.registerAction("analyzeProjectAction", analyzeProjectAction);

        // add actions into main menu
        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);
        buildMenuActionGroup.add(updateDependencyAction);
        buildMenuActionGroup.add(customGradleBuildAction);
        buildMenuActionGroup.add(analyzeProjectAction);
        buildMenuActionGroup.add(showTaskListAction);

        // add actions into context menu
        DefaultActionGroup buildContextMenuGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD_CONTEXT_MENU);
        buildContextMenuGroup.addSeparator();
        buildContextMenuGroup.addAction(updateDependencyAction);

        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.add(createGradleModuleAction, new Constraints(Anchor.AFTER, "newProject"));
    }

    @Inject
    private void registerIcons(IconRegistry iconRegistry, GradleResources resources) {
        iconRegistry.registerIcon(new Icon("gradle.module", resources.module()));
        // icons for file names
        iconRegistry.registerIcon(new Icon("gradle/gradle.file.small.icon", resources.gradle()));
        iconRegistry.registerIcon(new Icon("gradle/settings.gradle.file.small.icon", resources.gradle()));
    }

    public static boolean hasAnySourceFolder(@Nonnull ProjectDescriptor project) {
        Map<String, List<String>> attributes = project.getAttributes();

        return !(attributes.get(SOURCE_FOLDER).get(0).isEmpty() || attributes.get(TEST_FOLDER).get(0).isEmpty());
    }
}
