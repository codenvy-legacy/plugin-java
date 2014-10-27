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
package com.codenvy.ide.extension.maven.client.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Evgen Vidolob
 */
public class MavenPageViewImpl implements MavenPageView {

    private static MavenPageViewImplUiBinder ourUiBinder = GWT.create(MavenPageViewImplUiBinder.class);
    private final DockLayoutPanel rootElement;
    @UiField
    Style   style;
    @UiField
    TextBox versionField;
    @UiField
    TextBox groupId;
    @UiField
    TextBox artifactId;
    @UiField
    ListBox packagingField;
    private ActionDelegate delegate;

    public MavenPageViewImpl() {
        rootElement = ourUiBinder.createAndBindUi(this);

    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public String getArtifactId() {
        return artifactId.getText();
    }

    @Override
    public void setArtifactId(String artifactId) {
        this.artifactId.setText(artifactId);
    }

    @Override
    public String getVersion() {
        return versionField.getText();
    }

    @Override
    public void setVersion(String value) {
        versionField.setText(value);
    }

    @Override
    public String getPackaging() {
        return packagingField.getValue(packagingField.getSelectedIndex());
    }

    @Override
    public void setPackaging(String packaging) {
        for (int i = 0; i < packagingField.getItemCount(); i++) {
            if (packaging.equals(packagingField.getValue(i))) {
                packagingField.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    public void reset() {
        artifactId.setText("");
        artifactId.setFocus(true);
        groupId.setText("");
        versionField.setText("1.0-SNAPSHOT");
        packagingField.setSelectedIndex(0);

    }

    @Override
    public void enablePackaging(boolean enabled) {
        packagingField.setEnabled(enabled);
    }

    @Override
    public void disableAllFields() {
        changeEnabling(false);
    }

    @Override
    public void enableAllFields() {
        changeEnabling(true);
    }

    private void changeEnabling(boolean enabled) {
        versionField.setEnabled(enabled);
        groupId.setEnabled(enabled);
        artifactId.setEnabled(enabled);
        packagingField.setEnabled(enabled);
    }

    @Override
    public String getGroupId() {
        return groupId.getText();
    }

    @Override
    public void setGroupId(String group) {
        groupId.setText(group);
    }

    @UiHandler({"versionField", "groupId", "artifactId"})
    void onKeyUp(KeyUpEvent event) {
        delegate.onTextsChange();
    }

    @UiHandler("packagingField")
    void onPackagingChanged(ChangeEvent event) {
        delegate.setPackaging(getPackaging());
    }

    @Override
    public void showArtifactIdMissingIndicator(boolean doShow) {
        if (doShow) {
            artifactId.addStyleName(style.inputError());
        } else {
            artifactId.removeStyleName(style.inputError());
        }
    }

    @Override
    public void showGroupIdMissingIndicator(boolean doShow) {
        if (doShow) {
            groupId.addStyleName(style.inputError());
        } else {
            groupId.removeStyleName(style.inputError());
        }
    }

    @Override
    public void showVersionMissingIndicator(boolean doShow) {
        if (doShow) {
            versionField.addStyleName(style.inputError());
        } else {
            versionField.removeStyleName(style.inputError());
        }
    }

    interface MavenPageViewImplUiBinder
            extends UiBinder<DockLayoutPanel, MavenPageViewImpl> {
    }

    interface Style extends CssResource {
        String inputError();
    }
}