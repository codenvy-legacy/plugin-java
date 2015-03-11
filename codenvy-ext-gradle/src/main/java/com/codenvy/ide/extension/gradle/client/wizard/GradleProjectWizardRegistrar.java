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

import com.codenvy.api.project.shared.dto.ImportProject;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar;
import com.codenvy.ide.api.wizard.WizardPage;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.annotation.Nonnull;

import static com.codenvy.ide.ext.java.shared.Constants.JAVA_CATEGORY;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;

/**
 * Provides information for registering Maven project type into project wizard.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleProjectWizardRegistrar implements ProjectWizardRegistrar {
    private final Array<Provider<? extends WizardPage<ImportProject>>> wizardPages;

    @Inject
    public GradleProjectWizardRegistrar(Provider<GradlePagePresenter> gradlePagePresenter) {
        wizardPages = Collections.createArray();
        wizardPages.add(gradlePagePresenter);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getProjectTypeId() {
        return GRADLE_ID;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getCategory() {
        return JAVA_CATEGORY;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public Array<Provider<? extends WizardPage<ImportProject>>> getWizardPages() {
        return wizardPages;
    }
}
