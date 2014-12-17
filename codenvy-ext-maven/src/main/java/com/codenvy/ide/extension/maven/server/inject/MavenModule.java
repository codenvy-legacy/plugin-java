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
package com.codenvy.ide.extension.maven.server.inject;

import com.codenvy.api.project.server.ProjectGenerator;
import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.ide.extension.maven.server.MavenMultimoduleAutoBuilder;
import com.codenvy.ide.extension.maven.server.MavenPomService;
import com.codenvy.ide.extension.maven.server.projecttype.*;
import com.codenvy.ide.extension.maven.server.projecttype.MavenProjectType;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Artem Zatsarynnyy */
@DynaModule
public class MavenModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MavenProjectType.class);
        bind(MavenProjectTypeDescriptionsExtension.class);
        bind(MavenPomService.class);
        bind(MavenMultimoduleAutoBuilder.class);

        Multibinder<ValueProviderFactory> multiBinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
        multiBinder.addBinding().to(MavenValueProviderFactory.class);

        Multibinder.newSetBinder(binder(), ProjectTypeResolver.class).addBinding().to(MavenProjectTypeResolver.class);
        Multibinder.newSetBinder(binder(), ProjectGenerator.class).addBinding().to(MavenProjectGenerator.class);
    }
}
