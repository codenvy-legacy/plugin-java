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
package com.codenvy.ide.extension.gradle.server.inject;

import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.ide.extension.gradle.server.project.GradleProjectService;
import com.codenvy.ide.extension.gradle.server.project.GradleSourceFolderValueProviderFactory;
import com.codenvy.ide.extension.gradle.server.project.type.GradleProjectTypeDescriptionExtension;
import com.codenvy.ide.extension.gradle.server.project.type.GradleProjectTypeExtension;
import com.codenvy.ide.extension.gradle.server.project.type.GradleProjectTypeResolver;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Vladyslav Zhukovskii */
@DynaModule
public class GradleModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(GradleProjectTypeDescriptionExtension.class);
        bind(GradleProjectTypeExtension.class);
        Multibinder.newSetBinder(binder(), ProjectTypeResolver.class).addBinding().to(GradleProjectTypeResolver.class);

        Multibinder<ValueProviderFactory> multiBinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
        multiBinder.addBinding().to(GradleSourceFolderValueProviderFactory.class);

        bind(GradleProjectService.class);
    }
}
