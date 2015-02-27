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
package com.codenvy.ide.extension.maven.server.inject;

import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.handlers.ProjectHandler;
import com.codenvy.api.project.server.type.ProjectType;
import com.codenvy.ide.extension.maven.server.MavenMultimoduleAutoBuilder;
import com.codenvy.ide.extension.maven.server.projecttype.MavenProjectType;
import com.codenvy.ide.extension.maven.server.projecttype.MavenProjectTypeResolver;
import com.codenvy.ide.extension.maven.server.projecttype.MavenValueProviderFactory;
import com.codenvy.ide.extension.maven.server.projecttype.handler.AddMavenModuleHandler;
import com.codenvy.ide.extension.maven.server.projecttype.handler.ArchetypeGenerationStrategy;
import com.codenvy.ide.extension.maven.server.projecttype.handler.GeneratorStrategy;
import com.codenvy.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import com.codenvy.ide.extension.maven.server.projecttype.handler.MavenProjectImportedHandler;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Artem Zatsarynnyy */
@DynaModule
public class MavenModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MavenMultimoduleAutoBuilder.class);
        Multibinder.newSetBinder(binder(), ValueProviderFactory.class).addBinding().to(MavenValueProviderFactory.class);
        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(MavenProjectType.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(MavenProjectGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(AddMavenModuleHandler.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(MavenProjectImportedHandler.class);
        Multibinder.newSetBinder(binder(), GeneratorStrategy.class).addBinding().to(ArchetypeGenerationStrategy.class);
        Multibinder.newSetBinder(binder(), ProjectTypeResolver.class).addBinding().to(MavenProjectTypeResolver.class);
    }
}
