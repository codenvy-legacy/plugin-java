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
package com.codenvy.ide.extension.ant.client.wizard;

import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.ide.api.wizard1.AbstractWizardPage;
import com.codenvy.ide.extension.ant.shared.AntAttributes;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Arrays;

/**
 * Wizard page for Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class AntPagePresenter extends AbstractWizardPage<NewProject> implements AntPageView.ActionDelegate {

    private final AntPageView view;

    /** Create instance of {@link AntPagePresenter}. */
    @Inject
    public AntPagePresenter(AntPageView view) {
        super();
        this.view = view;
        view.setDelegate(this);
    }

    @Override
    public void init(NewProject dataObject) {
        super.init(dataObject);

        // TODO: add constants for wizard context
        final boolean isCreatingNewProject = Boolean.parseBoolean(context.get("isCreatingNewProject"));
        if (isCreatingNewProject) {
            // set default values
            dataObject.getAttributes().put(AntAttributes.SOURCE_FOLDER, Arrays.asList(AntAttributes.DEF_SRC_PATH));
            dataObject.getAttributes().put(AntAttributes.TEST_SOURCE_FOLDER, Arrays.asList(AntAttributes.DEF_TEST_SRC_PATH));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean canSkip() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
