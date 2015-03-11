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
package org.eclipse.che.ide.extension.ant.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.project.tree.TreeStructureProviderRegistry;
import org.eclipse.che.ide.ext.java.client.DependenciesUpdater;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.extension.ant.client.projecttree.AntProjectTreeStructureProvider;
import org.eclipse.che.ide.extension.ant.shared.AntAttributes;

/**
 * Extension for support Ant projects.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
@Extension(title = "Ant", version = "3.0.0")
public class AntExtension {

    /** Create instance of {@link AntExtension}. */
    @Inject
    public AntExtension(final EventBus eventBus,
                        final DependenciesUpdater dependenciesUpdater,
                        TreeStructureProviderRegistry treeStructureProviderRegistry) {
        // Handle project opened event to fire update dependencies.
        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                ProjectDescriptor project = event.getProject();
                if (AntAttributes.ANT_ID.equals(project.getType())
                    && project.getAttributes().containsKey(Constants.LANGUAGE)
                    && project.getAttributes().get(Constants.LANGUAGE).get(0).equals("java")) {
                    dependenciesUpdater.updateDependencies(project, false);
                }
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
            }
        });

        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(AntAttributes.ANT_ID, AntProjectTreeStructureProvider.ID);
    }
}
