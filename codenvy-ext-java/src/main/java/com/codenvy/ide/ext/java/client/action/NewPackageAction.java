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

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.event.NodeChangedEvent;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.StorableNode;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.ext.java.client.JavaUtils;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFolderNode;
import com.codenvy.ide.newresource.AbstractNewResourceAction;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.ui.dialogs.InputCallback;
import com.codenvy.ide.ui.dialogs.input.InputValidator;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nullable;

/**
 * Action to create new Java package.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewPackageAction extends AbstractNewResourceAction {
    private final InputValidator nameValidator = new NameValidator();
    private JavaLocalizationConstant localizationConstant;

    @Inject
    public NewPackageAction(JavaResources javaResources, JavaLocalizationConstant localizationConstant) {
        super(localizationConstant.actionNewPackageTitle(),
              localizationConstant.actionNewPackageDescription(),
              javaResources.packageIcon());
        this.localizationConstant = localizationConstant;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        dialogFactory.createInputDialog("New " + title, "Name:", new InputCallback() {
            @Override
            public void accepted(String value) {
                try {
                    JavaUtils.checkPackageName(value);
                    final StorableNode parent = getParent();
                    final String[] path = value.split("\\.");
                    createPackage(parent, value, new AsyncCallback<ItemReference>() {
                        @Override
                        public void onSuccess(ItemReference result) {
                            eventBus.fireEvent(
                                    NodeChangedEvent.createNodeChildrenChangedEvent((AbstractTreeNode<?>)filterParent(path, parent, 0)));
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            dialogFactory.createMessageDialog("", caught.getMessage(), null).show();
                        }
                    });
                } catch (IllegalStateException ex) {
                    dialogFactory.createMessageDialog(localizationConstant.messagesNewPackageInvalidName(), ex.getMessage(), null).show();
                }

            }
        }, null).withValidator(nameValidator).show();
    }

    @Override
    public void updateProjectAction(ActionEvent e) {
        boolean enabled = false;
        Selection<?> selection = selectionAgent.getSelection();
        if (selection != null) {
            enabled = selection.getFirstElement() instanceof PackageNode || selection.getFirstElement() instanceof SourceFolderNode;
        }
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    private TreeNode filterParent(String[] path, TreeNode parent, int index) {
        for (Object item : parent.getChildren().asIterable()) {
            TreeNode child = (TreeNode)item;
            if (child.getId().equals(path[index])) {
                index++;
                parent = filterParent(path, child, index);
            }
        }
        return parent;
    }

    private void createPackage(StorableNode parent, String name, final AsyncCallback<ItemReference> callback) {
        final Unmarshallable<ItemReference> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class);
        projectServiceClient
                .createFolder(parent.getPath() + '/' + name.replace('.', '/'), new AsyncRequestCallback<ItemReference>(unmarshaller) {
                    @Override
                    protected void onSuccess(ItemReference result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
    }

    private class NameValidator implements InputValidator {
        @Nullable
        @Override
        public Violation validate(String value) {
            try {
                JavaUtils.checkPackageName(value);
            } catch (final IllegalStateException e) {
                return new Violation() {
                    @Nullable
                    @Override
                    public String getMessage() {
                        return e.getMessage();
                    }
                };
            }
            return null;
        }
    }
}
