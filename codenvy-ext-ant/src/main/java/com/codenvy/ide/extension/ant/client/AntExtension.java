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
package com.codenvy.ide.extension.ant.client;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.NodeChangedEvent;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.projecttree.TreeStructureProviderRegistry;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.ant.client.projecttree.AntProjectTreeStructureProvider;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

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
                        final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                        final ProjectServiceClient projectServiceClient,
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

        // Handle build.xml file save operation and if ant configuration has been changed reload project tree.
        // For example, if user provide custom source directory.
        eventBus.addHandler(FileEvent.TYPE, new FileEventHandler() {
            @Override
            public void onFileOperation(final FileEvent event) {
                if (event.getOperationType() == FileEvent.FileOperation.SAVE && "build.xml".equals(event.getFile().getName())) {
                    Unmarshallable<ProjectDescriptor> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class);
                    projectServiceClient.getProject(event.getFile().getProject().getData().getPath(),
                                                    new AsyncRequestCallback<ProjectDescriptor>(unmarshaller) {
                                                        @Override
                                                        protected void onSuccess(ProjectDescriptor result) {
                                                            if (!result.getAttributes()
                                                                       .equals(event.getFile().getProject().getData().getAttributes())) {
                                                                event.getFile().getProject().setData(result);
                                                                eventBus.fireEvent(NodeChangedEvent.createNodeChildrenChangedEvent(
                                                                        event.getFile().getProject()));
                                                            }
                                                        }

                                                        @Override
                                                        protected void onFailure(Throwable exception) {
                                                            Log.info(getClass(), "Unable to get the project.", exception);
                                                        }
                                                    }
                                                   );
                }
            }
        });

        treeStructureProviderRegistry.associateProjectTypeToTreeProvider(AntAttributes.ANT_ID, AntProjectTreeStructureProvider.ID);
    }
}
