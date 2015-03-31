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
package org.eclipse.che.ide.extension.maven.client;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.BuildersDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.event.RefreshProjectTreeEvent;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.project.tree.TreeStructureProviderRegistry;
import org.eclipse.che.ide.api.project.tree.generic.ProjectNode;
import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.Collections;
import org.eclipse.che.ide.ext.java.client.dependenciesupdater.DependenciesUpdater;
import org.eclipse.che.ide.extension.maven.client.actions.CreateMavenModuleAction;
import org.eclipse.che.ide.extension.maven.client.actions.CustomBuildAction;
import org.eclipse.che.ide.extension.maven.client.actions.UpdateDependencyAction;
import org.eclipse.che.ide.extension.maven.client.event.BeforeModuleOpenEvent;
import org.eclipse.che.ide.extension.maven.client.event.BeforeModuleOpenHandler;
import org.eclipse.che.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_BUILD;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_BUILD_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;

/**
 * Maven extension entry point.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Maven", version = "3.0.0")
public class MavenExtension {
    private static Array<MavenArchetype> archetypes;

    @Inject
    public MavenExtension(TreeStructureProviderRegistry treeStructureProviderRegistry,
                          PreSelectedProjectTypeManager preSelectedProjectManager) {

        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(MavenAttributes.MAVEN_ID, MavenProjectTreeStructureProvider.ID);

        preSelectedProjectManager.setProjectTypeIdToPreselect(MavenAttributes.MAVEN_ID, 100);

        archetypes =
                Collections.createArray(new MavenArchetype("org.apache.maven.archetypes", "maven-archetype-quickstart", "RELEASE", null),
                                        new MavenArchetype("org.apache.maven.archetypes", "maven-archetype-webapp", "RELEASE", null),
                                        new MavenArchetype("org.apache.openejb.maven", "tomee-webapp-archetype", "1.7.1", null),
                                        new MavenArchetype("org.apache.cxf.archetype", "cxf-jaxws-javafirst", "RELEASE", null),
                                        new MavenArchetype("org.apache.cxf.archetype", "cxf-jaxrs-service", "RELEASE", null));
    }

    public static Array<MavenArchetype> getAvailableArchetypes() {
        return archetypes;
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
                                        eventBus.fireEvent(new RefreshProjectTreeEvent(project));
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
