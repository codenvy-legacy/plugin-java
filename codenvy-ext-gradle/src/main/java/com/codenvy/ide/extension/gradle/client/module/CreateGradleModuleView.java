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
package com.codenvy.ide.extension.gradle.client.module;

import com.codenvy.ide.api.mvp.View;

import javax.annotation.Nonnull;

/**
 * The view of {@link CreateGradleModulePresenter}.
 *
 * @author Vladyslav Zhukovskii
 */
public interface CreateGradleModuleView extends View<CreateGradleModuleView.ActionDelegate> {
    /** Delegate actions which called from View into Presenter. */
    public interface ActionDelegate {
        /** Perform new module creation. */
        void createModule();

        /** Handle module name changed event. */
        void moduleNameChanged(@Nonnull String moduleName);

        /** Handle close dialog window. */
        void onClose();
    }

    /** Get module name. */
    @Nonnull
    String getModuleName();

    /** Show error if name doesn't match specified rules. */
    void setNameError(boolean hasError);

    /** Set create buttons enabled if no errors found. */
    void setCreateButtonEnabled(boolean enabled);

    /** Show loader when module creation started. */
    void showButtonLoader(boolean showLoader);

    /** Show current dialog window. */
    void show();

    /** Close current dialog window. */
    void close();
}
