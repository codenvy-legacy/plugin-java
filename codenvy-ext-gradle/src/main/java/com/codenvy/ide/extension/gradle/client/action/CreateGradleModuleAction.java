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
package com.codenvy.ide.extension.gradle.client.action;

import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.projecttree.generic.FolderNode;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.api.projecttree.generic.StorableNode;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.module.CreateGradleModulePresenter;
import com.google.gwt.resources.client.ClientBundle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nullable;

/**
 * Create custom module action button.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class CreateGradleModuleAction extends GradleBaseAction {

    private CreateGradleModulePresenter presenter;
    private SelectionAgent              selectionAgent;
    private BuildContext                buildContext;

    @Inject
    public CreateGradleModuleAction(GradleLocalizationConstant localization,
                                    CreateGradleModulePresenter presenter,
                                    SelectionAgent selectionAgent,
                                    Resources resources,
                                    BuildContext buildContext) {
        super(localization.createModuleActionText(), localization.createModuleActionDescription(), resources.module());
        this.presenter = presenter;
        this.selectionAgent = selectionAgent;
        this.buildContext = buildContext;
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setEnabled(!(buildContext.isBuilding() || getSelectedFolderNode() == null));
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        StorableNode node;
        if ((node = getSelectedFolderNode()) != null) {
            presenter.showDialog(node);
        }
    }

    /** Get selected node in Project Explorer. */
    @Nullable
    private StorableNode getSelectedFolderNode() {
        Selection<?> selection = selectionAgent.getSelection();
        if (selection != null && selection.getFirstElement() != null && selection.getFirstElement() instanceof StorableNode) {
            StorableNode node = (StorableNode)selection.getFirstElement();
            if (node instanceof FolderNode || node instanceof ProjectNode) {
                return node;
            }
        }
        return null;
    }

    public interface Resources extends ClientBundle {
        @Source("module.svg")
        SVGResource module();
    }
}
