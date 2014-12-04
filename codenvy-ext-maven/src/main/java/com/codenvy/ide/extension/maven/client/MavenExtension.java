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
import com.codenvy.api.project.shared.dto.BuildersDescriptor;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.constraints.Anchor;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.NodeChangedEvent;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.projecttree.TreeStructureProviderRegistry;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.api.projecttype.wizard.PreSelectedProjectTypeManager;
import com.codenvy.ide.api.projecttype.wizard.ProjectTypeWizardRegistry;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizard;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.extension.maven.client.actions.CreateMavenModuleAction;
import com.codenvy.ide.extension.maven.client.actions.CustomBuildAction;
import com.codenvy.ide.extension.maven.client.actions.UpdateDependencyAction;
import com.codenvy.ide.extension.maven.client.event.BeforeModuleOpenEvent;
import com.codenvy.ide.extension.maven.client.event.BeforeModuleOpenHandler;
import com.codenvy.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import com.codenvy.ide.extension.maven.client.wizard.MavenPagePresenter;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.extension.runner.client.wizard.SelectRunnerPagePresenter;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD;
import static com.codenvy.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;
import static com.codenvy.ide.api.action.IdeActions.GROUP_FILE_NEW;

/**
 * Maven extension entry point.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Maven", version = "3.0.0")
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
        wizardRegistry.addWizard(MavenAttributes.MAVEN_ID, wizard);

        treeStructureProviderRegistry.registerProvider(MavenAttributes.MAVEN_ID, mavenProjectTreeStructureProvider);

        preSelectedProjectManager.setProjectTypeIdToPreselect(MavenAttributes.MAVEN_ID, 100);
    }

    @Inject
    private void bindEvents(final EventBus eventBus,
                            final DependenciesUpdater dependenciesUpdater,
                            final ProjectServiceClient projectServiceClient,
                            final DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        eventBus.addHandler(BeforeModuleOpenEvent.TYPE, new BeforeModuleOpenHandler() {
            @Override
            public void onBeforeModuleOpen(BeforeModuleOpenEvent event) {
                if (isValidForResolveDependencies(event.getModule().getProject().getData())) {
                    dependenciesUpdater.updateDependencies(event.getModule().getData(), false);
                }
            }
        });

        eventBus.addHandler(FileEvent.TYPE, new FileEventHandler() {
            @Override
            public void onFileOperation(final FileEvent event) {
                if (event.getOperationType() == FileEvent.FileOperation.SAVE
                    && "pom.xml".equals(event.getFile().getName())
                    && isValidForResolveDependencies(event.getFile().getProject().getData())) {
                    final ProjectNode project = event.getFile().getProject();
                    dependenciesUpdater.updateDependencies(project.getData(), true);

                    final Unmarshallable<ProjectDescriptor> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class);
                    projectServiceClient.getProject(
                            project.getData().getPath(),
                            new AsyncRequestCallback<ProjectDescriptor>(unmarshaller) {
                                @Override
                                protected void onSuccess(ProjectDescriptor result) {
                                    if (!result.getAttributes().equals(project.getData().getAttributes())) {
                                        project.setData(result);
                                        eventBus.fireEvent(NodeChangedEvent.createNodeChildrenChangedEvent(project));
                                    }
                                }

                                @Override
                                protected void onFailure(Throwable exception) {
                                    Log.info(getClass(), "Unable to get the project.", exception);
                                }
                            });
                }
            }
        });

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                ProjectDescriptor project = event.getProject();
                if (isValidForResolveDependencies(project)) {
                    dependenciesUpdater.updateDependencies(project, false);
                }
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
            }
        });
    }

    @Inject
    private void prepareActions(ActionManager actionManager,
                                CustomBuildAction customBuildAction,
                                UpdateDependencyAction updateDependencyAction,
                                MavenLocalizationConstant mavenLocalizationConstants,
                                CreateMavenModuleAction createMavenModuleAction) {
        // register actions
        actionManager.registerAction(mavenLocalizationConstants.buildProjectControlId(), customBuildAction);
        actionManager.registerAction("updateDependency", updateDependencyAction);
        actionManager.registerAction("createMavenModule", createMavenModuleAction);

        // add actions in main menu
        DefaultActionGroup buildMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_BUILD);
        buildMenuActionGroup.add(customBuildAction);
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
        // icons for file names
        iconRegistry.registerIcon(new Icon("maven/pom.xml.file.small.icon", mavenResources.maven()));
    }

    private boolean isValidForResolveDependencies(ProjectDescriptor project) {
        Map<String, List<String>> attr = project.getAttributes();
        BuildersDescriptor builders = project.getBuilders();
        return builders != null && "maven".equals(builders.getDefault()) &&
               !(attr.containsKey(MavenAttributes.PACKAGING) && "pom".equals(attr.get(MavenAttributes.PACKAGING).get(0)));
    }
}
