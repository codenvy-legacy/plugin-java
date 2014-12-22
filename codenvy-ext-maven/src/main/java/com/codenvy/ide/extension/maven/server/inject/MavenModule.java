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
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.ide.extension.maven.server.MavenMultimoduleAutoBuilder;
import com.codenvy.ide.extension.maven.server.MavenPomService;
import com.codenvy.ide.extension.maven.server.projecttype.MavenProjectType;
import com.codenvy.ide.extension.maven.server.projecttype.MavenValueProviderFactory;
import com.codenvy.ide.extension.maven.server.projecttype.generators.ArchetypeProjectGenerator;
import com.codenvy.ide.extension.maven.server.projecttype.generators.MavenProjectGenerator;
import com.codenvy.ide.extension.maven.server.projecttype.generators.SimpleProjectGenerator;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Artem Zatsarynnyy */
@DynaModule
public class MavenModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MavenPomService.class);
        bind(MavenMultimoduleAutoBuilder.class);

        Multibinder<ValueProviderFactory> multiBinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
        multiBinder.addBinding().to(MavenValueProviderFactory.class);

        Multibinder<ProjectType2> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType2.class);
        projectTypeMultibinder.addBinding().to(MavenProjectType.class);

        Multibinder<ProjectGenerator> projectGeneratorMultibinder = Multibinder.newSetBinder(binder(), ProjectGenerator.class);
        projectGeneratorMultibinder.addBinding().to(MavenProjectGenerator.class);
//        projectGeneratorMultibinder.addBinding().to(ArchetypeProjectGenerator.class);
    }
}
