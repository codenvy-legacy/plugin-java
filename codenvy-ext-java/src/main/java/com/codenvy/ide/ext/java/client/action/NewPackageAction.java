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
package com.codenvy.ide.ext.java.client.action;

import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeStructure;
import com.codenvy.ide.api.projecttree.generic.StorableNode;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.ext.java.client.JavaUtils;
import com.codenvy.ide.ext.java.client.projecttree.nodes.AbstractSourceContainerNode;
import com.codenvy.ide.ext.java.client.projecttree.JavaTreeStructure;
import com.codenvy.ide.ext.java.client.projecttree.nodes.PackageNode;
import com.codenvy.ide.newresource.AbstractNewResourceAction;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.Unmarshallable;
import com.codenvy.ide.ui.dialogs.InputCallback;
import com.codenvy.ide.ui.dialogs.input.InputValidator;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nullable;

import static com.codenvy.ide.api.event.NodeChangedEvent.createNodeChildrenChangedEvent;

/**
 * Action to create new Java package.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewPackageAction extends AbstractNewResourceAction {
    private final InputValidator nameValidator = new NameValidator();

    @Inject
    public NewPackageAction(JavaResources javaResources, JavaLocalizationConstant localizationConstant) {
        super(localizationConstant.actionNewPackageTitle(),
              localizationConstant.actionNewPackageDescription(),
              javaResources.packageIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        dialogFactory.createInputDialog("New " + title, "Name:", new InputCallback() {
            @Override
            public void accepted(String value) {
                final StorableNode parent = getParent();
                createPackage(parent, value, new AsyncCallback<ItemReference>() {
                    @Override
                    public void onSuccess(ItemReference result) {
                        eventBus.fireEvent(createNodeChildrenChangedEvent(getNodeToRefresh((AbstractSourceContainerNode)parent)));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        dialogFactory.createMessageDialog("", caught.getMessage(), null).show();
                    }
                });
            }
        }, null).withValidator(nameValidator).show();
    }

    private AbstractTreeNode getNodeToRefresh(AbstractSourceContainerNode parent) {
        final TreeStructure currentTree = appContext.getCurrentProject().getCurrentTree();
        if (currentTree instanceof JavaTreeStructure && ((JavaTreeStructure)currentTree).getSettings().isCompactEmptyPackages()
            && parent instanceof PackageNode && parent.getChildren().isEmpty()) {
            return (AbstractTreeNode)parent.getParent();
        }
        return parent;
    }

    @Override
    public void updateProjectAction(ActionEvent e) {
        boolean enabled = false;
        Selection<?> selection = selectionAgent.getSelection();
        if (selection != null) {
            enabled = selection.getFirstElement() instanceof AbstractSourceContainerNode;
        }
        e.getPresentation().setEnabledAndVisible(enabled);
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
