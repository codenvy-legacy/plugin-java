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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/** @author Vladyslav Zhukovskii */
public class GradlePageViewImpl implements GradlePageView {
    private static GradlePageViewImplUiBinder ourUiBinder = GWT.create(GradlePageViewImplUiBinder.class);
    private final DockLayoutPanel rootElement;

    interface GradlePageViewImplUiBinder extends UiBinder<DockLayoutPanel, GradlePageViewImpl> {
    }

    /** Create instance of {@link GradlePageViewImpl}. */
    public GradlePageViewImpl() {
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return rootElement;
    }
}
