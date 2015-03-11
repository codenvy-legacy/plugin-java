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
package org.eclipse.che.gradle.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

import org.eclipse.che.gradle.client.GradleClientService;
import org.eclipse.che.gradle.client.GradleClientServiceImpl;
import org.eclipse.che.gradle.client.GradleDependencyBuildOptions;
import org.eclipse.che.gradle.client.build.GradleBuildView;
import org.eclipse.che.gradle.client.build.GradleBuildViewImpl;
import org.eclipse.che.gradle.client.module.CreateGradleModuleView;
import org.eclipse.che.gradle.client.module.CreateGradleModuleViewImpl;
import org.eclipse.che.gradle.client.projecttree.GradleNodeFactory;
import org.eclipse.che.gradle.client.projecttree.GradleProjectTreeStructureProvider;
import org.eclipse.che.gradle.client.task.TaskListView;
import org.eclipse.che.gradle.client.task.TaskListViewImpl;
import org.eclipse.che.gradle.client.task.tree.GradleTaskNodeFactory;
import org.eclipse.che.gradle.client.task.tree.GradleTaskTreeStructureProvider;
import org.eclipse.che.gradle.client.wizard.GradlePageView;
import org.eclipse.che.gradle.client.wizard.GradlePageViewImpl;
import org.eclipse.che.gradle.client.wizard.GradleProjectWizardRegistrar;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.ext.java.client.DependencyBuildOptionProvider;

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
