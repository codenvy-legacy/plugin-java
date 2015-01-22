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

import com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar;
import com.codenvy.ide.api.wizard.WizardPage;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.annotation.Nonnull;

import static com.codenvy.ide.extension.ant.shared.AntAttributes.ANT_ID;

/**
 * Provides information for registering Ant project type into project wizard.
 *
 * @author Artem Zatsarynnyy
 */
public class AntProjectWizardRegistrar implements ProjectWizardRegistrar {
    public static final String CATEGORY = "JAVA";
    private final Array<Provider<? extends WizardPage>> wizardPages;

    @Inject
    public AntProjectWizardRegistrar(Provider<AntPagePresenter> antPagePresenter) {
        wizardPages = Collections.createArray();
        wizardPages.add(antPagePresenter);
    }

    @Nonnull
    public String getProjectTypeId() {
        return ANT_ID;
    }

    @Nonnull
    public String getCategory() {
        return CATEGORY;
    }

    @Nonnull
    public Array<Provider<? extends WizardPage>> getWizardPages() {
        return wizardPages;
    }
}