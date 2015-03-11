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
package org.eclipse.che.gradle.client.action;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.gradle.client.GradleExtension;
import org.eclipse.che.gradle.client.GradleLocalizationConstant;
import org.eclipse.che.gradle.client.event.BeforeGradleModuleOpenEvent;
import org.eclipse.che.gradle.client.event.BeforeGradleModuleOpenHandler;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedEvent;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedHandler;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.ext.java.client.DependenciesUpdater;
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
