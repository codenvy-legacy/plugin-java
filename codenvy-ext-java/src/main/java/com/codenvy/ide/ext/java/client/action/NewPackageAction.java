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
package com.codenvy.ide.ext.java.client.action;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.event.NodeChangedEvent;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.generic.StorableNode;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.ext.java.client.JavaUtils;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFolderNode;
import com.codenvy.ide.newresource.DefaultNewResourceAction;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.ui.dialogs.askValue.AskValueCallback;
import com.codenvy.ide.ui.dialogs.askValue.AskValueDialog;
import com.codenvy.ide.ui.dialogs.info.Info;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Action to create new Java package.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewPackageAction extends DefaultNewResourceAction {
    private JavaLocalizationConstant localizationConstant;

    @Inject
    public NewPackageAction(JavaResources javaResources,
                            JavaLocalizationConstant localizationConstant,
                            AppContext appContext,
                            SelectionAgent selectionAgent,
                            EditorAgent editorAgent,
                            ProjectServiceClient projectServiceClient,
                            EventBus eventBus) {
        super(localizationConstant.actionNewPackageTitle(),
              localizationConstant.actionNewPackageDescription(),
              null,
              javaResources.packageIcon(),
              appContext,
              selectionAgent,
              editorAgent,
              projectServiceClient,
              eventBus);
        this.localizationConstant = localizationConstant;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new AskValueDialog("New " + title, "Name:", new AskValueCallback() {
            @Override
            public void onOk(String value) {
                try {
                    JavaUtils.checkPackageName(value);
                    final StorableNode parent = getParent();
                    createPackage(parent, value, new AsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            eventBus.fireEvent(NodeChangedEvent.createNodeChildrenChangedEvent((AbstractTreeNode<?>)parent));
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            new Info(caught.getMessage()).show();
                        }
                    });
                } catch (IllegalStateException ex) {
                    new Info(localizationConstant.messagesNewPackageInvalidName(), ex.getMessage()).show();
                }
            }
        }
        ).show();
    }

    @Override
    public void update(ActionEvent e) {
        boolean enabled = false;
        Selection<?> selection = selectionAgent.getSelection();
        if (selection != null) {
            enabled = selection.getFirstElement() instanceof PackageNode || selection.getFirstElement() instanceof SourceFolderNode;
        }
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    private void createPackage(StorableNode parent, String name, final AsyncCallback<Void> callback) {
        projectServiceClient.createFolder(parent.getPath() + '/' + name.replace('.', '/'), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }
}
