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
package com.codenvy.ide.extension.gradle.client.action;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.event.ProjectDescriptorChangedEvent;
import com.codenvy.ide.api.event.ProjectDescriptorChangedHandler;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.extension.gradle.client.GradleExtension;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.event.BeforeGradleModuleOpenEvent;
import com.codenvy.ide.extension.gradle.client.event.BeforeGradleModuleOpenHandler;
import com.codenvy.ide.extension.gradle.client.event.ProjectConfigurationReceivedEvent;
import com.codenvy.ide.extension.gradle.client.event.ProjectConfigurationReceivedHandler;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Update dependency action button.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class UpdateDependencyAction extends GradleBaseAction implements BeforeGradleModuleOpenHandler,
//                                                                        ProjectDescriptorChangedHandler,
                                                                        ProjectConfigurationReceivedHandler {

    private DependenciesUpdater dependenciesUpdater;
    private BuildContext        buildContext;

    @Inject
    public UpdateDependencyAction(Resources resources,
                                  DependenciesUpdater dependenciesUpdater,
                                  GradleLocalizationConstant localization,
                                  EventBus eventBus,
                                  BuildContext buildContext) {
        super(localization.updateDependenciesActionText(), localization.updateDependenciesActionDescription(),
              resources.updateDependencies());
        this.dependenciesUpdater = dependenciesUpdater;
        this.buildContext = buildContext;

        eventBus.addHandler(BeforeGradleModuleOpenEvent.TYPE, this);
//        eventBus.addHandler(ProjectDescriptorChangedEvent.TYPE, this);
        eventBus.addHandler(ProjectConfigurationReceivedEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setEnabled(!buildContext.isBuilding());
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        CurrentProject currentProject = appContext.getCurrentProject();
        dependenciesUpdater.updateDependencies(currentProject.getRootProject(), currentProject.getProjectDescription(), true);
//        dependenciesUpdater.updateDependencies(appContext.getCurrentProject().getProjectDescription(), true);
    }

    /** {@inheritDoc} */
    @Override
    public void onBeforeModuleOpen(BeforeGradleModuleOpenEvent event) {
        log("DEBUG Class UpdateDependencyAction#onBeforeModuleOpen at line 81: onBeforeModuleOpen");
        ProjectDescriptor project = event.getModule().getData();
        if (hasAnySourceFolder(project)) {
            CurrentProject currentProject = appContext.getCurrentProject();
            dependenciesUpdater.updateDependencies(currentProject.getRootProject(), currentProject.getProjectDescription(), false);
//            dependenciesUpdater.updateDependencies(event.getModule().getData(), false);
        }
    }

    /** {@inheritDoc} */
//    @Override
//    public void onProjectDescriptorChanged(ProjectDescriptorChangedEvent event) {
//        log("DEBUG Class UpdateDependencyAction#onProjectDescriptorChanged at line 93: onProjectDescriptorChanged");
//        if (isGradleProject(event.getProjectDescriptor()) && hasAnySourceFolder(event.getProjectDescriptor())) {
//            CurrentProject currentProject = appContext.getCurrentProject();
//            dependenciesUpdater.updateDependencies(currentProject.getRootProject(), currentProject.getProjectDescription(), false);
//            dependenciesUpdater.updateDependencies(event.getProjectDescriptor(), false);
//        }
//    }

    @Override
    public void onConfigurationReceived(ProjectConfigurationReceivedEvent event) {
        log("DEBUG Class UpdateDependencyAction#onConfigurationReceived at line 101: ");
        if (GradleExtension.hasAnySourceFolder(event.getProject())) {
            dependenciesUpdater.updateDependencies(event.getProject(), event.getProject(), false);
        }
    }

    public native void log(String message) /*-{
        $wnd.console.log(message);
    }-*/;

    public interface Resources extends ClientBundle {
        @Source("update-dependencies.svg")
        SVGResource updateDependencies();
    }
}
