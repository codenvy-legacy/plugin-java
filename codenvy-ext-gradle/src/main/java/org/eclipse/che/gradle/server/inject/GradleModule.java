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
package org.eclipse.che.gradle.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.gradle.server.GradleProjectService;
import org.eclipse.che.gradle.server.project.GradleProjectResolver;
import org.eclipse.che.gradle.server.project.generator.ProjectGenerator;
import org.eclipse.che.gradle.server.project.generator.impl.SimpleProjectGenerator;
import org.eclipse.che.gradle.server.project.generator.impl.WrappedProjectGenerator;
import org.eclipse.che.gradle.server.project.handler.*;
import org.eclipse.che.gradle.server.project.type.GradleProjectType;
import org.eclipse.che.gradle.server.project.type.GradleValueProviderFactory;
import org.eclipse.che.inject.DynaModule;

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
