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
package com.codenvy.ide.extension.gradle.server.inject;

import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.handlers.ProjectHandler;
import com.codenvy.api.project.server.type.ProjectType;
import com.codenvy.ide.extension.gradle.server.GradleProjectService;
import com.codenvy.ide.extension.gradle.server.project.GradleProjectResolver;
import com.codenvy.ide.extension.gradle.server.project.generator.ProjectGenerator;
import com.codenvy.ide.extension.gradle.server.project.handler.CreateGradleModuleHandler;
import com.codenvy.ide.extension.gradle.server.project.handler.CreateGradleProjectHandler;
import com.codenvy.ide.extension.gradle.server.project.handler.GetGradleModulesHandler;
import com.codenvy.ide.extension.gradle.server.project.handler.GradleProjectImportedHandler;
import com.codenvy.ide.extension.gradle.server.project.handler.ProjectHasBecomeGradle;
import com.codenvy.ide.extension.gradle.server.project.type.GradleValueProviderFactory;
import com.codenvy.ide.extension.gradle.server.project.generator.impl.SimpleProjectGenerator;
import com.codenvy.ide.extension.gradle.server.project.generator.impl.WrappedProjectGenerator;
import com.codenvy.ide.extension.gradle.server.project.type.GradleProjectType;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Vladyslav Zhukovskii */
@DynaModule
public class GradleModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(GradleProjectResolver.class).asEagerSingleton();

        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(GradleProjectType.class);
        Multibinder.newSetBinder(binder(), ValueProviderFactory.class).addBinding().to(GradleValueProviderFactory.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(CreateGradleProjectHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(CreateGradleModuleHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(GetGradleModulesHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(GradleProjectImportedHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(ProjectHasBecomeGradle.class);
        Multibinder.newSetBinder(binder(), ProjectGenerator.class).addBinding().to(SimpleProjectGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectGenerator.class).addBinding().to(WrappedProjectGenerator.class);

        bind(GradleProjectService.class);
    }
}
