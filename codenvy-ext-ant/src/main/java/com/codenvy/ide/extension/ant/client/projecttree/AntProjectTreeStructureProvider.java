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
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.TreeStructure;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.codenvy.ide.ext.java.client.navigation.JavaNavigationService;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

/**
 * Tree structure provider responsible for creating tree structure instances for project.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class AntProjectTreeStructureProvider implements TreeStructureProvider {
    private AntNodeFactory         nodeFactory;
    private EventBus               eventBus;
    private AppContext             appContext;
    private IconRegistry           iconRegistry;
    private ProjectServiceClient   projectServiceClient;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private JavaNavigationService  service;

    /** Create instance of {@link AntProjectTreeStructureProvider}. */
    @Inject
    public AntProjectTreeStructureProvider(AntNodeFactory nodeFactory, EventBus eventBus, AppContext appContext,
                                           IconRegistry iconRegistry, ProjectServiceClient projectServiceClient,
                                           DtoUnmarshallerFactory dtoUnmarshallerFactory, JavaNavigationService service) {
        this.nodeFactory = nodeFactory;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.iconRegistry = iconRegistry;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.service = service;
    }

    @Nonnull
    @Override
    public String getId() {
        return "ant";
    }

    @Override
    public TreeStructure get() {
        return new AntProjectTreeStructure(nodeFactory, eventBus, appContext, projectServiceClient, iconRegistry, dtoUnmarshallerFactory,
                                           service);
    }
}
