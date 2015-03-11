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
package org.eclipse.che.gradle.client.wizard;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.Collections;

import javax.annotation.Nonnull;

import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_CATEGORY;

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
