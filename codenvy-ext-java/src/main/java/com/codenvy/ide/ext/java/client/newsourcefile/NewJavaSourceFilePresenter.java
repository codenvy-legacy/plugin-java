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
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.event.NodeChangedEvent;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.JavaUtils;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFolderNode;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.ui.dialogs.info.Info;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Presenter for creating Java source file.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewJavaSourceFilePresenter implements NewJavaSourceFileView.ActionDelegate {
    private static final String DEFAULT_CONTENT = " {\n}\n";
    private NewJavaSourceFileView  view;
    private SelectionAgent         selectionAgent;
    private ProjectServiceClient   projectServiceClient;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private EventBus               eventBus;
    private EditorAgent editorAgent;
    private Array<JavaSourceFileType> sourceFileTypes = Collections.createArray();

    @Inject
    public NewJavaSourceFilePresenter(NewJavaSourceFileView view, SelectionAgent selectionAgent, ProjectServiceClient projectServiceClient,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory, EventBus eventBus, EditorAgent editorAgent) {
        this.view = view;
        this.selectionAgent = selectionAgent;
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.eventBus = eventBus;
        this.editorAgent = editorAgent;

        this.view.setDelegate(this);
        sourceFileTypes.add(JavaSourceFileType.CLASS);
        sourceFileTypes.add(JavaSourceFileType.INTERFACE);
        sourceFileTypes.add(JavaSourceFileType.ENUM);
    }

    public void showDialog() {
        view.setTypes(sourceFileTypes);
        view.showDialog();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onNameChanged() {
        try {
            String fileNameWithExtension = view.getName();
            if (fileNameWithExtension.lastIndexOf(".java") == -1) {
                fileNameWithExtension += ".java";
            }
            JavaUtils.checkCompilationUnitName(fileNameWithExtension);
            view.hideErrorHint();
        } catch (IllegalStateException e) {
            view.showErrorHint(e.getLocalizedMessage());
        }
    }

    @Override
    public void onOkClicked() {
        String fileNameWithoutExtension = view.getName();
        if (fileNameWithoutExtension.lastIndexOf(".java") != -1) {
            fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.lastIndexOf(".java"));
        }

        if (JavaUtils.isValidCompilationUnitName(fileNameWithoutExtension + ".java")) {
            view.close();
            final FolderNode parent = (FolderNode)selectionAgent.getSelection().getFirstElement();
            switch (view.getSelectedType()) {
                case CLASS:
                    createClass(fileNameWithoutExtension, parent);
                    break;
                case INTERFACE:
                    createInterface(fileNameWithoutExtension, parent);
                    break;
                case ENUM:
                    createEnum(fileNameWithoutExtension, parent);
                    break;
            }
        }
    }

    private void createClass(String name, FolderNode parent) {
        createSourceFile(name, parent, getPackageName(parent) + "public class " + name + DEFAULT_CONTENT);
    }

    private void createInterface(String name, FolderNode parent) {
        createSourceFile(name, parent, getPackageName(parent) + "public interface " + name + DEFAULT_CONTENT);
    }

    private void createEnum(String name, FolderNode parent) {
        createSourceFile(name, parent, getPackageName(parent) + "public enum " + name + DEFAULT_CONTENT);
    }

    private void createSourceFile(String name, final FolderNode parent, String content) {
        final Unmarshallable<ItemReference> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class);
        projectServiceClient
                .createFile(parent.getPath(), name + ".java", content, null, new AsyncRequestCallback<ItemReference>(unmarshaller) {
                    @Override
                    protected void onSuccess(ItemReference result) {
                        eventBus.fireEvent(NodeChangedEvent.createNodeChildrenChangedEvent(parent));
                        FileNode file =
                                new FileNode(null, result, eventBus, projectServiceClient, null);
                        editorAgent.openEditor(file);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        new Info(exception.getMessage()).show();
                    }
                });
    }

    private String getPackageName(FolderNode parent) {
        if (parent instanceof SourceFolderNode) {
            return "\n";
        }

        String packageName = parent.getName();
        TreeNode<?> parentNode = parent.getParent();
        while (parentNode instanceof PackageNode) {
            packageName = ((PackageNode)parentNode).getName() + '.' + packageName;
            parentNode = parentNode.getParent();
        }

        return "package " + packageName + ";\n\n";
    }
}
