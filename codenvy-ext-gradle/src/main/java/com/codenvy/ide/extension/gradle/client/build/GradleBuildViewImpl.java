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

import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

/**
 * The implementation of {@link GradleBuildView}.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleBuildViewImpl extends Window implements GradleBuildView {

    interface GradleBuildViewImplUiBinder extends UiBinder<Widget, GradleBuildViewImpl> {
    }

    private static GradleBuildViewImplUiBinder ourUiBinder = GWT.create(GradleBuildViewImplUiBinder.class);

    @UiField
    CheckBox skipTest;
    @UiField
    CheckBox refreshDependencies;
    @UiField
    CheckBox offline;
    @UiField
    TextBox  buildCommand;

    Button btnStartBuild;
    Button btnCancel;

    @UiField(provided = true)
    GradleLocalizationConstant localization;

    private ActionDelegate delegate;

    @Inject
    public GradleBuildViewImpl(GradleLocalizationConstant localization) {
        this.localization = localization;
        Widget widget = ourUiBinder.createAndBindUi(this);
        this.setTitle(localization.customBuildViewCaption());
        this.setWidget(widget);

        skipTest.ensureDebugId("skipTestCheckBox");
        refreshDependencies.ensureDebugId("refreshDependenciesCheckBox");
        offline.ensureDebugId("offlineCheckBox");
        buildCommand.ensureDebugId("buildCommandField");

        createButtons();
    }

    private void createButtons() {
        btnCancel = createButton("Cancel", "project-buildWithOptions-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        getFooter().add(btnCancel);

        btnStartBuild = createButton("Start build", "project-buildWithOptions-startBuild", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onStartBuildClicked();
            }
        });
        getFooter().add(btnStartBuild);
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getBuildCommand() {
        return buildCommand.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setBuildCommand(@Nonnull String buildCommand) {
        this.buildCommand.setText(buildCommand);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSkipTestSelected() {
        return skipTest.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRefreshDependenciesSelected() {
        return refreshDependencies.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOfflineWorkSelected() {
        return offline.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
    }
}
