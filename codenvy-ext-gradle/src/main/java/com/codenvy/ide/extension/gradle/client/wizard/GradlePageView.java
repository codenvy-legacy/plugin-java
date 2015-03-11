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

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.gradle.DistributionType;
import com.codenvy.ide.gradle.DistributionVersion;
import com.google.inject.ImplementedBy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The view of {@link com.codenvy.ide.extension.gradle.client.wizard.GradlePagePresenter}.
 *
 * @author Vladyslav Zhukovskii
 */
public interface GradlePageView extends View<GradlePageView.ActionDelegate> {
    /** Delegate actions which called from View into Presenter. */
    public interface ActionDelegate {
        /** Perform action when Gradle builder type changed. */
        void onGradleTypeChanged(@Nonnull DistributionType distributionType);

        /** Perform action when Gradle builder version changed. */
        void onGradleVersionChanged(@Nullable DistributionVersion distributionVersion);
    }

    /** Show builder configuration section. */
    void setDistributionConfigurationSectionVisibility(boolean visible);

    /** Reset wizard page state. */
    void reset();
}
