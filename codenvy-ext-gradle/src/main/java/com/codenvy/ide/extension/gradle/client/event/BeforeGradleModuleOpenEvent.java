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
package com.codenvy.ide.extension.gradle.client.event;

import com.codenvy.ide.extension.gradle.client.projecttree.GradleModuleNode;
import com.google.gwt.event.shared.GwtEvent;

import javax.annotation.Nonnull;


/**
 * Event fired when Gradle module opens in Project Explorer part.
 *
 * @author Vladyslav Zhukovskii
 */
public class BeforeGradleModuleOpenEvent extends GwtEvent<BeforeGradleModuleOpenHandler> {
    public static GwtEvent.Type<BeforeGradleModuleOpenHandler> TYPE = new GwtEvent.Type<BeforeGradleModuleOpenHandler>();

    private GradleModuleNode module;

    public BeforeGradleModuleOpenEvent(@Nonnull GradleModuleNode module) {
        this.module = module;
    }

    @Nonnull
    public GradleModuleNode getModule() {
        return module;
    }

    public GwtEvent.Type<BeforeGradleModuleOpenHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(BeforeGradleModuleOpenHandler handler) {
        handler.onBeforeModuleOpen(this);
    }
}
