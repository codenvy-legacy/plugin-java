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
package com.codenvy.ide.ext.java.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.BuildersDescriptor;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.web.bindery.event.shared.EventBus;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseNodeTest {
    protected static final String PROJECT_PATH = "/project";
    @Mock
    protected EventBus               eventBus;
    @Mock
    protected EditorAgent            editorAgent;
    @Mock
    protected ProjectServiceClient   projectServiceClient;
    @Mock
    protected DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    protected IconRegistry           iconRegistry;
    @Mock
    protected ProjectDescriptor      projectDescriptor;
    @Mock
    protected ProjectNode            projectNode;

    @Before
    public void setUp() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("maven.source.folder", Collections.singletonList("src/main/java"));
        when(projectDescriptor.getAttributes()).thenReturn(attributes);

        BuildersDescriptor buildersDescriptor = mock(BuildersDescriptor.class);
        when(buildersDescriptor.getDefault()).thenReturn("maven");
        when(projectDescriptor.getBuilders()).thenReturn(buildersDescriptor);

        when(projectNode.getData()).thenReturn(projectDescriptor);
        when(projectNode.getPath()).thenReturn(PROJECT_PATH);

        Icon icon = mock(Icon.class);
        when(icon.getSVGImage()).thenReturn(null);
        when(iconRegistry.getIcon(anyString())).thenReturn(icon);
    }
}
