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
package com.codenvy.ide.extension.ant.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.AbstractTreeStructure;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Tree structure provider responsible for creating tree structure instances for project.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class AntProjectTreeStructureProvider implements TreeStructureProvider {
    private EventBus               eventBus;
    private EditorAgent            editorAgent;
    private AppContext             appContext;
    private IconRegistry           iconRegistry;
    private ProjectServiceClient   projectServiceClient;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;

    /** Create instance of {@link AntProjectTreeStructureProvider}. */
    @Inject
    public AntProjectTreeStructureProvider(EventBus eventBus, EditorAgent editorAgent, AppContext appContext, IconRegistry iconRegistry,
                                             ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.eventBus = eventBus;
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.iconRegistry = iconRegistry;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractTreeStructure newTreeStructure(ProjectDescriptor project) {
        return new AntProjectTreeStructure(TreeSettings.DEFAULT, project, eventBus, editorAgent, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory);
    }
}
