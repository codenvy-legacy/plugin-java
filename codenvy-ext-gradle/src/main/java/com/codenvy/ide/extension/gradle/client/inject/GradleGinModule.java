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
package com.codenvy.ide.extension.gradle.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar;
import com.codenvy.ide.ext.java.client.DependencyBuildOptionProvider;
import com.codenvy.ide.extension.gradle.client.GradleDependencyBuildOptions;
import com.codenvy.ide.extension.gradle.client.GradleClientService;
import com.codenvy.ide.extension.gradle.client.GradleClientServiceImpl;
import com.codenvy.ide.extension.gradle.client.build.GradleBuildView;
import com.codenvy.ide.extension.gradle.client.build.GradleBuildViewImpl;
import com.codenvy.ide.extension.gradle.client.module.CreateGradleModuleView;
import com.codenvy.ide.extension.gradle.client.module.CreateGradleModuleViewImpl;
import com.codenvy.ide.extension.gradle.client.projecttree.GradleNodeFactory;
import com.codenvy.ide.extension.gradle.client.projecttree.GradleProjectTreeStructureProvider;
import com.codenvy.ide.extension.gradle.client.task.TaskListView;
import com.codenvy.ide.extension.gradle.client.task.TaskListViewImpl;
import com.codenvy.ide.extension.gradle.client.task.tree.GradleTaskNodeFactory;
import com.codenvy.ide.extension.gradle.client.task.tree.GradleTaskTreeStructureProvider;
import com.codenvy.ide.extension.gradle.client.wizard.GradlePageView;
import com.codenvy.ide.extension.gradle.client.wizard.GradlePageViewImpl;
import com.codenvy.ide.extension.gradle.client.wizard.GradleProjectWizardRegistrar;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

/**
 * Gradle Client Module.
 *
 * @author Vladyslav Zhukovskii
 */
@ExtensionGinModule
public class GradleGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(GradlePageView.class).to(GradlePageViewImpl.class).in(Singleton.class);
        bind(TaskListView.class).to(TaskListViewImpl.class).in(Singleton.class);
        bind(GradleBuildView.class).to(GradleBuildViewImpl.class).in(Singleton.class);
        bind(CreateGradleModuleView.class).to(CreateGradleModuleViewImpl.class).in(Singleton.class);

        bind(GradleClientService.class).to(GradleClientServiceImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder().build(GradleNodeFactory.class));
        GinMultibinder.newSetBinder(binder(), TreeStructureProvider.class).addBinding().to(GradleProjectTreeStructureProvider.class);

        install(new GinFactoryModuleBuilder().build(GradleTaskNodeFactory.class));
        GinMultibinder.newSetBinder(binder(), TreeStructureProvider.class).addBinding().to(GradleTaskTreeStructureProvider.class);

        GinMultibinder.newSetBinder(binder(), DependencyBuildOptionProvider.class).addBinding().to(GradleDependencyBuildOptions.class);

        GinMultibinder.newSetBinder(binder(), ProjectWizardRegistrar.class).addBinding().to(GradleProjectWizardRegistrar.class);
    }
}
