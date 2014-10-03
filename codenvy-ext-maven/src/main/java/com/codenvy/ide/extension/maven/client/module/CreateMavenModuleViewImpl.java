/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package com.codenvy.ide.extension.maven.client.module;

import com.codenvy.ide.ui.buttonLoader.ButtonLoaderResources;
import com.codenvy.ide.ui.window.Window;
import com.codenvy.ide.wizard.project.ProjectWizardResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CreateMavenModuleViewImpl extends Window implements CreateMavenModuleView {
    public static final String CREATE = "Create";
    private static CreateMavenModuleViewImplUiBinder ourUiBinder = GWT.create(CreateMavenModuleViewImplUiBinder.class);
    private final Button createButton;
    @UiField
    TextBox parentArtifactId;
    @UiField
    TextBox nameField;
    @UiField
    TextBox artifactId;
    @UiField
    TextBox groupIdField;
    @UiField
    TextBox versionField;
    @UiField
    ListBox packagingField;

    @UiField(provided = true)
    CreateMavenModuleResources.Css styles;
    private ActionDelegate delegate;

    @Inject
    public CreateMavenModuleViewImpl(ProjectWizardResources wizardResources, CreateMavenModuleResources resources,
                                     ButtonLoaderResources buttonLoaderResources) {
        super(true);
        styles = resources.css();
        styles.ensureInjected();
        setTitle("Create Maven Module");
        FlowPanel rootElement = ourUiBinder.createAndBindUi(this);
        setWidget(rootElement);
        createButton = new Button();
        createButton.setText(CREATE);
        createButton.addStyleName(wizardResources.wizardCss().blueButton());
        createButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.create();
            }
        });
        getFooter().add(createButton);
        createButton.addStyleName(buttonLoaderResources.Css().buttonLoader());
    }

    @UiHandler("nameField")
    void onNameChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.projectNameChanged(nameField.getText());
    }

    @UiHandler("artifactId")
    void onArtifactChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.artifactIdChanged(artifactId.getText());
    }

    @Override
    protected void onClose() {
        delegate.onClose();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setParentArtifactId(String artifactId) {
        parentArtifactId.setValue(artifactId);
    }

    @Override
    public void setGroupId(String groupId) {
        groupIdField.setValue(groupId);
    }

    @Override
    public void setVersion(String version) {
        versionField.setValue(version);
    }

    @Override
    public void setCreateButtonEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
    }

    @Override
    public void setNameError(boolean hasError) {
        if (hasError) {
            nameField.addStyleName(styles.inputError());
        } else {
            nameField.removeStyleName(styles.inputError());
        }
    }

    @Override
    public void setArtifactIdError(boolean hasError) {
        if (hasError) {
            artifactId.addStyleName(styles.inputError());
        } else {
            artifactId.removeStyleName(styles.inputError());
        }
    }

    @Override
    public void reset() {
        nameField.setValue("");
        artifactId.setValue("");
    }

    @Override
    public String getPackaging() {
        return packagingField.getValue(packagingField.getSelectedIndex());
    }

    @Override
    public String getGroupId() {
        return groupIdField.getText();
    }

    @Override
    public String getVersion() {
        return versionField.getText();
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void showButtonLoader(boolean showLoader) {
        if (showLoader) {
            createButton.setEnabled(false);
            createButton.setHTML("<i></i>");
        } else {
            createButton.setEnabled(true);
            createButton.setText(CREATE);
        }
    }

    interface CreateMavenModuleViewImplUiBinder extends
                                                UiBinder<FlowPanel, CreateMavenModuleViewImpl> {
    }


}