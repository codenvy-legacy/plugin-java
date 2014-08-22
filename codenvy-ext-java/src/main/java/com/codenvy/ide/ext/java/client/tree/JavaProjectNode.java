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
package com.codenvy.ide.ext.java.client.tree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttree.TreeSettings;
import com.codenvy.ide.api.projecttree.generic.ProjectRootNode;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Node that represents Java project.
 *
 * @author Artem Zatsarynnyy
 */
public class JavaProjectNode extends ProjectRootNode {
    public JavaProjectNode(ProjectDescriptor data, TreeSettings settings, EventBus eventBus,
                           ProjectServiceClient projectServiceClient, DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(data, settings, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }
}
