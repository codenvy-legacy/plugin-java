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
package com.codenvy.ide.extension.gradle.client.wizard;

import com.codenvy.api.project.shared.dto.BuildersDescriptor;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizard;
import com.codenvy.ide.api.wizard.AbstractWizardPage;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GradlePagePresenter extends AbstractWizardPage implements GradlePageView.ActionDelegate {

    private GradlePageView view;
    private DtoFactory  dtoFactory;

    /** Create instance of {@link com.codenvy.ide.extension.gradle.client.wizard.GradlePagePresenter}. */
    @Inject
    public GradlePagePresenter(GradlePageView view, DtoFactory dtoFactory) {
        super("Gradle project settings", null);
        this.view = view;
        this.dtoFactory = dtoFactory;
        view.setDelegate(this);
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getNotice() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompleted() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void focusComponent() {
    }

    /** {@inheritDoc} */
    @Override
    public void removeOptions() {
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

    /** {@inheritDoc} */
    @Override
    public void commit(@Nonnull CommitCallback callback) {
        ProjectDescriptor project = wizardContext.getData(ProjectWizard.PROJECT);
        if (project != null) {
            BuildersDescriptor builders = dtoFactory.createDto(BuildersDescriptor.class).withDefault(GradleAttributes.GRADLE_ID);
            project.setBuilders(builders);
            project.getAttributes().put(GradleAttributes.SOURCE_FOLDER, Arrays.asList("src/main/java", "src/main/resources"));
            project.getAttributes().put(GradleAttributes.TEST_SOURCE_FOLDER, Arrays.asList("src/test/java", "src/test/resources"));
        }

        super.commit(callback);
    }

}
