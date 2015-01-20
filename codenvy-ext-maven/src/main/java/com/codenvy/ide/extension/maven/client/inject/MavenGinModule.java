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
package com.codenvy.ide.extension.maven.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar;
import com.codenvy.ide.extension.maven.client.build.MavenBuildView;
import com.codenvy.ide.extension.maven.client.build.MavenBuildViewImpl;
import com.codenvy.ide.extension.maven.client.projecttree.MavenNodeFactory;
import com.codenvy.ide.extension.maven.client.projecttree.MavenProjectTreeStructureProvider;
import com.codenvy.ide.extension.maven.client.wizard.MavenProjectWizardRegistrar;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

/**
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyy
 */
@ExtensionGinModule
public class MavenGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(MavenBuildView.class).to(MavenBuildViewImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(MavenNodeFactory.class));
        GinMultibinder.newSetBinder(binder(), TreeStructureProvider.class).addBinding().to(MavenProjectTreeStructureProvider.class);

        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(MavenProjectWizardRegistrar.class);
    }
}
