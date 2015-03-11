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
package com.codenvy.builder.gradle;

import com.codenvy.api.builder.internal.Builder;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Vladyslav Zhukovskii */
@DynaModule
public class GradleBuilderModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        Multibinder<Builder> multiBinder = Multibinder.newSetBinder(binder(), Builder.class);
        multiBinder.addBinding().to(GradleBuilder.class);
    }
}
