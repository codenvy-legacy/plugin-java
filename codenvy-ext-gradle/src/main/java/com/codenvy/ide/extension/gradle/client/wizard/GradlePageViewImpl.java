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

import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.GradleResources;
import com.codenvy.ide.gradle.DistributionType;
import com.codenvy.ide.gradle.DistributionVersion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The implementation of {@link com.codenvy.ide.extension.gradle.client.wizard.GradlePageView}.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradlePageViewImpl implements GradlePageView {
    private static GradlePageViewImplUiBinder ourUiBinder = GWT.create(GradlePageViewImplUiBinder.class);
    private final DockLayoutPanel rootElement;

    @UiField
    ListBox gradleType;

    @UiField
    ListBox gradleVersion;

    @UiField
    DockLayoutPanel configPanel;

    @UiField
    DockLayoutPanel versionChoosePanel;

    @UiField
    DockLayoutPanel nothingToConfigurePanel;

    private ActionDelegate delegate;

    @UiField(provided = true)
    GradleResources resources;

    @UiField(provided = true)
    GradleLocalizationConstant localization;

    interface GradlePageViewImplUiBinder extends UiBinder<DockLayoutPanel, GradlePageViewImpl> {
    }

    /** Create instance of {@link GradlePageViewImpl}. */
    @Inject
    public GradlePageViewImpl(GradleResources resources,
                              GradleLocalizationConstant localization) {
        this.resources = resources;
        this.localization = localization;
        rootElement = ourUiBinder.createAndBindUi(this);

        gradleType.ensureDebugId("gradleType");
        gradleVersion.ensureDebugId("gradleVersion");
        configPanel.ensureDebugId("configPanel");
        versionChoosePanel.ensureDebugId("versionChoosePanel");
        nothingToConfigurePanel.ensureDebugId("nothingToConfigurePanel");

        gradleVersion.clear();
        gradleType.clear();

        gradleType.addItem("Codenvy Bundled", DistributionType.BUNDLED.toString());
        gradleType.addItem("Wrapper", DistributionType.WRAPPED.toString());

        for (DistributionVersion version : DistributionVersion.values()) {
            gradleVersion.addItem(version.getVersion(), version.getVersion());
        }

        versionChoosePanel.setVisible(false);
        nothingToConfigurePanel.setVisible(false);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @UiHandler("gradleType")
    public void onGradleChanged(ChangeEvent event) {
        DistributionType distribution = getSelectedDistribution();

        delegate.onGradleTypeChanged(distribution);

        versionChoosePanel.setVisible(distribution.isWrapped());
        delegate.onGradleVersionChanged(distribution.isWrapped() ? getSelectedVersion() : null);
    }

    @UiHandler("gradleVersion")
    public void onGradleVersionChanged(ChangeEvent event) {
        delegate.onGradleVersionChanged(getSelectedVersion());
    }

    private DistributionType getSelectedDistribution() {
        return DistributionType.fromValueOrDefault(gradleType.getValue(gradleType.getSelectedIndex()));
    }

    private DistributionVersion getSelectedVersion() {
        return DistributionVersion.fromVersion(gradleVersion.getValue(gradleVersion.getSelectedIndex()));
    }

    /** {@inheritDoc} */
    @Override
    public void setDistributionConfigurationSectionVisibility(boolean visible) {
        configPanel.setVisible(visible);
        nothingToConfigurePanel.setVisible(!visible);
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        gradleType.setSelectedIndex(0);
        gradleVersion.setSelectedIndex(0);

        configPanel.setVisible(true);
        versionChoosePanel.setVisible(false);
        nothingToConfigurePanel.setVisible(false);

        onGradleChanged(null);
    }
}
