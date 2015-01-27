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
import com.codenvy.ide.dto.DtoFactory;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Wizard page for Ant project.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class AntPagePresenter extends AbstractWizardPage<NewProject> implements AntPageView.ActionDelegate {

    private AntPageView view;
    private DtoFactory  dtoFactory;

    /** Create instance of {@link AntPagePresenter}. */
    @Inject
    public AntPagePresenter(AntPageView view, DtoFactory dtoFactory) {
        super();
        this.view = view;
        this.dtoFactory = dtoFactory;
        view.setDelegate(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompleted() {
        return true;
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

//    /** {@inheritDoc} */
//    @Override
//    public void commit(@Nonnull CommitCallback callback) {
//        ProjectDescriptor project = wizardContext.getData(ProjectWizard.PROJECT);
//        if (project != null) {
//            BuildersDescriptor builders = dtoFactory.createDto(BuildersDescriptor.class).withDefault(AntAttributes.ANT_ID);
//            project.setBuilders(builders);
//            project.getAttributes().put(AntAttributes.SOURCE_FOLDER, Arrays.asList(AntAttributes.DEF_SRC_PATH));
//            project.getAttributes().put(AntAttributes.TEST_SOURCE_FOLDER, Arrays.asList(AntAttributes.DEF_TEST_SRC_PATH));
//        }
//
//        super.commit(callback);
//    }
}
