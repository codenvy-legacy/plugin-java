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
import com.google.gwt.event.logical.shared.ValueChangeEvent;

import javax.annotation.Nonnull;

/** @author Vladyslav Zhukovskii */
public interface GradleBuildView extends View<GradleBuildView.ActionDelegate> {
    public interface ActionDelegate {
        void onStartBuildClicked();

        void onCancelClicked();

        void onSkipTestValueChange(ValueChangeEvent<Boolean> event);

        void onRefreshDependencyValueChange(ValueChangeEvent<Boolean> event);

        void onOfflineValueChange(ValueChangeEvent<Boolean> event);
    }

    @Nonnull
    String getBuildCommand();

    void setBuildCommand(@Nonnull String message);

    void close();

    void showDialog();

    boolean isSkipTestSelected();
}
