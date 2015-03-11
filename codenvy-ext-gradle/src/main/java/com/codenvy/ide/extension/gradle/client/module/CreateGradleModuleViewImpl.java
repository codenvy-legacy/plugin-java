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

import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.GradleResources;
import com.codenvy.ide.projecttype.wizard.ProjectWizardResources;
import com.codenvy.ide.ui.buttonLoader.ButtonLoaderResources;
import com.codenvy.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

/**
 * The implementation of {@link CreateGradleModuleView}.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class CreateGradleModuleViewImpl extends Window implements CreateGradleModuleView {
    interface CreateGradleModuleViewImplUiBinder extends UiBinder<Widget, CreateGradleModuleViewImpl> {
    }

    @UiField
    TextBox moduleNameField;

    Button createButton;

    @UiField(provided = true)
    GradleResources resources;

    @UiField(provided = true)
    GradleLocalizationConstant localization;

    private ActionDelegate delegate;

    @Inject
    public CreateGradleModuleViewImpl(ProjectWizardResources wizardResources,
                                      ButtonLoaderResources buttonLoaderResources,
                                      GradleResources resources,
                                      GradleLocalizationConstant localization) {
        super(true);
        this.resources = resources;
        this.localization = localization;
        setTitle(localization.createModuleViewCaption());

        CreateGradleModuleViewImplUiBinder uiBinder = GWT.create(CreateGradleModuleViewImplUiBinder.class);
        Widget rootElement = uiBinder.createAndBindUi(this);
        setWidget(rootElement);

        createButton = new Button();
        createButton.setText("Create");
        createButton.addStyleName(wizardResources.wizardCss().button());
        createButton.addStyleName(wizardResources.wizardCss().buttonPrimary());
        createButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.createModule();
            }
        });

        getFooter().add(createButton);
        createButton.addStyleName(buttonLoaderResources.Css().buttonLoader());

        moduleNameField.ensureDebugId("moduleNameField");
    }

    @UiHandler("moduleNameField")
    public void onModuleNameChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            delegate.createModule();
        }

        delegate.moduleNameChanged(moduleNameField.getText());
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getModuleName() {
        return moduleNameField.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setNameError(boolean hasError) {
        if (hasError) {
            moduleNameField.addStyleName(resources.getCSS().inputError());
        } else {
            moduleNameField.removeStyleName(resources.getCSS().inputError());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setCreateButtonEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showButtonLoader(boolean showLoader) {
        if (showLoader) {
            createButton.setEnabled(false);
            createButton.setHTML("<i></i>");
        } else {
            createButton.setEnabled(true);
            createButton.setText("Create");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        delegate.onClose();
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        super.show();
        moduleNameField.setText("");
        createButton.setEnabled(false);
    }
}
