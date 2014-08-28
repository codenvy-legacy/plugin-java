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

import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.selection.Selection;
import com.codenvy.ide.api.selection.SelectionAgent;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.ext.java.client.newsourcefile.NewJavaSourceFilePresenter;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.SourceFolderNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to create new Java source file.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewJavaSourceFileAction extends Action {
    private SelectionAgent             selectionAgent;
    private NewJavaSourceFilePresenter newJavaSourceFilePresenter;

    @Inject
    public NewJavaSourceFileAction(SelectionAgent selectionAgent,
                                   NewJavaSourceFilePresenter newJavaSourceFilePresenter,
                                   JavaLocalizationConstant constant,
                                   JavaResources resources) {
        super(constant.actionNewClassTitle(), constant.actionNewClassDescription(), resources.classItem());
        this.newJavaSourceFilePresenter = newJavaSourceFilePresenter;
        this.selectionAgent = selectionAgent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        newJavaSourceFilePresenter.showDialog();
    }

    @Override
    public void update(ActionEvent e) {
        boolean visible = false;
        Selection<?> selection = selectionAgent.getSelection();
        if (selection != null) {
            visible = selection.getFirstElement() instanceof PackageNode || selection.getFirstElement() instanceof SourceFolderNode;
        }
        e.getPresentation().setEnabledAndVisible(visible);
    }
}
