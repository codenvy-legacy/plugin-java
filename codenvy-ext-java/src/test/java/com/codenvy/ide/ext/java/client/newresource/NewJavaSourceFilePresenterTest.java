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
package com.codenvy.ide.ext.java.client.newresource;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.tree.PackageNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link NewJavaSourceFilePresenter} functionality.
 *
 * @author Artem Zatsarynnyy
 */
@RunWith(MockitoJUnitRunner.class)
public class NewJavaSourceFilePresenterTest {
    private static String FILE_NAME    = "TestClass";
    private static String PARENT_PATH  = "/project/src/main/java/com/codenvy";
    private static String PACKAGE_NAME = "com.codenvy";
    @Mock
    private NewJavaSourceFileView      view;
    @Mock
    private EventBus                   eventBus;
    @Mock
    private SelectionAgent             selectionAgent;
    @Mock
    private ProjectServiceClient       projectServiceClient;
    @InjectMocks
    private NewJavaSourceFilePresenter presenter;

    @Before
    public void setUp() {
        PackageNode comPackage = mock(PackageNode.class);
        PackageNode codenvyPackage = mock(PackageNode.class);
        when(codenvyPackage.getPath()).thenReturn(PARENT_PATH);
        when(codenvyPackage.getParent()).thenReturn((AbstractTreeNode)comPackage);

        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(codenvyPackage);
        when(selectionAgent.getSelection()).thenReturn(selection);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<Void> callback = (AsyncRequestCallback<Void>)arguments[4];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, (Void)null);
                return callback;
            }
        }).when(projectServiceClient)
          .createFile(anyString(), anyString(), anyString(), anyString(), (AsyncRequestCallback<Void>)anyObject());
    }

    @Test
    public void shouldShowDialog() {
        presenter.showDialog();

        verify(view).setTypes(Matchers.<Array<JavaSourceFileType>>anyObject());
        verify(view).showDialog();
    }

    @Test
    public void shouldCloseDialogOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testCreateClass() throws Exception {
        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.CLASS);

        presenter.onOkClicked();

        verify(projectServiceClient).createFile(eq(PARENT_PATH), eq(FILE_NAME + ".java"), anyString(), anyString(),
                                                Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Ignore
    @Test
    public void testCreateInterface() throws Exception {
        String interfaceContent = "package " + PACKAGE_NAME + ";\n\npublic interface" + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.INTERFACE);

        presenter.onOkClicked();

        verify(projectServiceClient).createFile(eq(PARENT_PATH), eq(FILE_NAME + ".java"), eq(interfaceContent), anyString(),
                                                Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Test
    public void testCreateEnum() throws Exception {
        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.ENUM);

        presenter.onOkClicked();

        verify(projectServiceClient).createFile(eq(PARENT_PATH), eq(FILE_NAME + ".java"), anyString(), anyString(),
                                                Matchers.<AsyncRequestCallback<Void>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }
}
