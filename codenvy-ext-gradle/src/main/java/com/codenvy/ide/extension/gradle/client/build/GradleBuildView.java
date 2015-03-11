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
package com.codenvy.ide.extension.gradle.client.build;

import com.codenvy.ide.api.mvp.View;

import javax.annotation.Nonnull;

/**
 * The view of {@link GradleBuildPresenter}.
 *
 * @author Vladyslav Zhukovskii
 */
public interface GradleBuildView extends View<GradleBuildView.ActionDelegate> {
    /** Delegate actions which called from View into Presenter. */
    public interface ActionDelegate {
        /** Perform building with custom options. */
        void onStartBuildClicked();

        /** CLose current dialog. */
        void onCancelClicked();
    }

    /** @return entered build command. */
    @Nonnull
    String getBuildCommand();

    /** Set calculated build command into dialog window. */
    void setBuildCommand(@Nonnull String message);

    /** Close current dialog window. */
    void close();

    /** Show current dialog window. */
    void showDialog();

    /** @return true if skip test check box selected. */
    boolean isSkipTestSelected();

    /** @return true if refresh dependencies check box selected. */
    boolean isRefreshDependenciesSelected();

    /** @return true if offline work check box selected. */
    boolean isOfflineWorkSelected();
}
