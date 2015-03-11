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
import org.eclipse.che.gradle.client.GradleLocalizationConstant;
import org.eclipse.che.gradle.client.analyze.ProjectAnalyzer;
import org.eclipse.che.gradle.client.event.ProjectConfigurationReceivedEvent;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.ext.java.client.DependenciesUpdater;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;

import static org.eclipse.che.gradle.dto.GrdConfiguration.State.OUTDATED;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_BUILD_GRADLE;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_SETTINGS_GRADLE;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.SAVE;

/**
 * Analyze project action button.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class AnalyzeProjectAction extends GradleBaseAction implements FileEventHandler, ProjectActionHandler {

    private ProjectAnalyzer     projectAnalyzer;
    private BuildContext        buildContext;
    private EventBus            eventBus;
    private DependenciesUpdater dependenciesUpdater;

    @Inject
    public AnalyzeProjectAction(ProjectAnalyzer projectAnalyzer,
                                GradleLocalizationConstant localization,
                                Resources resources,
                                BuildContext buildContext,
                                EventBus eventBus,
                                DependenciesUpdater dependenciesUpdater) {
        super(localization.analyzeProjectActionText(), localization.analyzeProjectActionDescription(), resources.analyze());
        this.projectAnalyzer = projectAnalyzer;
        this.buildContext = buildContext;
        this.eventBus = eventBus;
        this.dependenciesUpdater = dependenciesUpdater;

        eventBus.addHandler(FileEvent.TYPE, this);
        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setEnabled(!(projectAnalyzer.isBusy() || buildContext.isBuilding()));
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        projectAnalyzer.analyzeProject(appContext.getCurrentProject().getRootProject());
    }

    /** {@inheritDoc} */
    @Override
    public void onFileOperation(FileEvent event) {
        if (gradleNecessaryFileSaved(event) && !projectAnalyzer.isBusy()) {
            projectAnalyzer.analyzeProject(appContext.getCurrentProject().getRootProject());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectOpened(final ProjectActionEvent event) {
        if (isGradleProject(event.getProject())) {
            projectAnalyzer.getGrdConfiguration(event.getProject(), new AsyncCallback<GrdConfiguration>() {
                @Override
                public void onFailure(Throwable e) {
                    projectAnalyzer.analyzeProject(event.getProject());
                }

                @Override
                public void onSuccess(GrdConfiguration grdConfiguration) {
                    if (grdConfiguration.getConfigurationState() == OUTDATED)
                        projectAnalyzer.analyzeProject(event.getProject());
                    else {
                        eventBus.fireEvent(new ProjectConfigurationReceivedEvent(grdConfiguration, event.getProject()));
                    }
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectClosed(ProjectActionEvent event) {
    }

    /**
     * Checks if necessary gradle files were changed e.g. settings.gradle, build.gradle etc.
     */
    private boolean gradleNecessaryFileSaved(@Nonnull FileEvent event) {
        ProjectDescriptor project = event.getFile().getProject().getData();
        String fileName = event.getFile().getName();
        return GRADLE_ID.equals(project.getType()) && (SAVE == event.getOperationType())
               && (DEFAULT_BUILD_GRADLE.equals(fileName) || DEFAULT_SETTINGS_GRADLE.equals(fileName));
    }

    public interface Resources extends ClientBundle {
        @Source("analyze.svg")
        SVGResource analyze();
    }
}
