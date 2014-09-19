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
package com.codenvy.ide.ext.java.client.newsourcefile;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFolderNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.gwt.test.utils.GwtReflectionUtils;

import org.junit.Before;
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
    private static String FILE_NAME            = "TestClass";
    private static String SRC_FOLDER_PATH      = "/project/src/main/java";
    private static String CODENVY_PACKAGE_PATH = "/project/src/main/java/com/codenvy";
    private static String PACKAGE_NAME         = "com.codenvy";
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
    @Mock
    private SourceFolderNode           srcFolder;
    @Mock
    private PackageNode                codenvyPackage;
    @Mock
    private ItemReference              createdFile;

    @Before
    public void setUp() {
        when(srcFolder.getPath()).thenReturn(SRC_FOLDER_PATH);
        PackageNode comPackage = mock(PackageNode.class);
        when(codenvyPackage.getParent()).thenReturn((AbstractTreeNode)comPackage);
        when(codenvyPackage.getName()).thenReturn("codenvy");
        when(codenvyPackage.getPath()).thenReturn(CODENVY_PACKAGE_PATH);
        when(comPackage.getParent()).thenReturn((AbstractTreeNode)srcFolder);
        when(comPackage.getName()).thenReturn("com");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                AsyncRequestCallback<ItemReference> callback = (AsyncRequestCallback<ItemReference>)arguments[4];
                Method onSuccess = GwtReflectionUtils.getMethod(callback.getClass(), "onSuccess");
                onSuccess.invoke(callback, createdFile);
                return callback;
            }
        }).when(projectServiceClient)
          .createFile(anyString(), anyString(), anyString(), anyString(), (AsyncRequestCallback<ItemReference>)anyObject());
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
    public void shouldShowHintWhenNameIsInvalid() throws Exception {
        when(view.getName()).thenReturn('#' + FILE_NAME);
        presenter.onNameChanged();
        verify(view).showErrorHint(anyString());
    }

    @Test
    public void shouldHideHintWhenNameIsValid() throws Exception {
        when(view.getName()).thenReturn(FILE_NAME);
        presenter.onNameChanged();
        verify(view).hideErrorHint();
    }

    @Test
    public void shouldCreateClassInsideSourceFolder() throws Exception {
        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(srcFolder);
        when(selectionAgent.getSelection()).thenReturn(selection);

        final String fileContent = "\npublic class " + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.CLASS);

        presenter.onOkClicked();

        verify(view).close();
        verify(projectServiceClient).createFile(eq(SRC_FOLDER_PATH), eq(FILE_NAME + ".java"), eq(fileContent), anyString(),
                                                Matchers.<AsyncRequestCallback<ItemReference>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Test
    public void shouldCreateClassInsidePackage() throws Exception {
        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(codenvyPackage);
        when(selectionAgent.getSelection()).thenReturn(selection);

        final String fileContent = "package " + PACKAGE_NAME + ";\n\npublic class " + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.CLASS);

        presenter.onOkClicked();

        verify(view).close();
        verify(projectServiceClient).createFile(eq(CODENVY_PACKAGE_PATH), eq(FILE_NAME + ".java"), eq(fileContent), anyString(),
                                                Matchers.<AsyncRequestCallback<ItemReference>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Test
    public void shouldCreateInterfaceInsideSourceFolder() throws Exception {
        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(srcFolder);
        when(selectionAgent.getSelection()).thenReturn(selection);

        final String fileContent = "\npublic interface " + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.INTERFACE);

        presenter.onOkClicked();

        verify(view).close();
        verify(projectServiceClient).createFile(eq(SRC_FOLDER_PATH), eq(FILE_NAME + ".java"), eq(fileContent), anyString(),
                                                Matchers.<AsyncRequestCallback<ItemReference>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Test
    public void shouldCreateInterfaceInsidePackage() throws Exception {
        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(codenvyPackage);
        when(selectionAgent.getSelection()).thenReturn(selection);

        final String fileContent = "package " + PACKAGE_NAME + ";\n\npublic interface " + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.INTERFACE);

        presenter.onOkClicked();

        verify(view).close();
        verify(projectServiceClient).createFile(eq(CODENVY_PACKAGE_PATH), eq(FILE_NAME + ".java"), eq(fileContent), anyString(),
                                                Matchers.<AsyncRequestCallback<ItemReference>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Test
    public void shouldCreateEnumInsideSourceFolder() throws Exception {
        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(srcFolder);
        when(selectionAgent.getSelection()).thenReturn(selection);

        final String fileContent = "\npublic enum " + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.ENUM);

        presenter.onOkClicked();

        verify(view).close();
        verify(projectServiceClient).createFile(eq(SRC_FOLDER_PATH), eq(FILE_NAME + ".java"), eq(fileContent), anyString(),
                                                Matchers.<AsyncRequestCallback<ItemReference>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }

    @Test
    public void shouldCreateEnumInsidePackage() throws Exception {
        Selection selection = mock(Selection.class);
        when(selection.getFirstElement()).thenReturn(codenvyPackage);
        when(selectionAgent.getSelection()).thenReturn(selection);

        final String fileContent = "package " + PACKAGE_NAME + ";\n\npublic enum " + FILE_NAME + " {\n}\n";

        when(view.getName()).thenReturn(FILE_NAME);
        when(view.getSelectedType()).thenReturn(JavaSourceFileType.ENUM);

        presenter.onOkClicked();

        verify(view).close();
        verify(projectServiceClient).createFile(eq(CODENVY_PACKAGE_PATH), eq(FILE_NAME + ".java"), eq(fileContent), anyString(),
                                                Matchers.<AsyncRequestCallback<ItemReference>>anyObject());
        verify(eventBus).fireEvent(Matchers.<Event<Object>>anyObject());
    }
}
