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

import com.codenvy.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

/** @author Vladyslav Zhukovskii */
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

    private ActionDelegate            delegate;

    @Inject
    public GradleBuildViewImpl() {
        Widget widget = ourUiBinder.createAndBindUi(this);
        this.setTitle("Gradle Builder");
        this.setWidget(widget);

        skipTest.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                delegate.onSkipTestValueChange(event);
            }
        });

        refreshDependencies.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                delegate.onRefreshDependencyValueChange(event);
            }
        });

        offline.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                delegate.onOfflineValueChange(event);
            }
        });
        createButtons();
    }

    private void createButtons(){
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

    @Nonnull
    @Override
    public String getBuildCommand() {
        return buildCommand.getText();
    }

    @Override
    public void setBuildCommand(@Nonnull String buildCommand) {
        this.buildCommand.setText(buildCommand);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void showDialog() {
        this.show();
    }

    @Override
    public boolean isSkipTestSelected() {
        return skipTest.getValue();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {

    }


}
