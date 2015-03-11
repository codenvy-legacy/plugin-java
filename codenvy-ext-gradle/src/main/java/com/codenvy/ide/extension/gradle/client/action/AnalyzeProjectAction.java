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
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.FileEventHandler;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.ext.java.client.DependenciesUpdater;
import com.codenvy.ide.extension.gradle.client.GradleExtension;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.analyze.ProjectAnalyzer;
import com.codenvy.ide.extension.gradle.client.event.ProjectConfigurationReceivedEvent;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;

import static com.codenvy.ide.api.event.FileEvent.FileOperation.SAVE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DEFAULT_BUILD_GRADLE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DEFAULT_SETTINGS_GRADLE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;
import static com.codenvy.ide.gradle.dto.GrdConfiguration.State.OUTDATED;

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
                    if (grdConfiguration.getConfigurationState() == OUTDATED) {
                        projectAnalyzer.analyzeProject(event.getProject());
                    } else {
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
        return GRADLE_ID.equals(project.getType()) && SAVE == event.getOperationType()
               && (DEFAULT_BUILD_GRADLE.equals(fileName) || DEFAULT_SETTINGS_GRADLE.equals(fileName));
    }

    public interface Resources extends ClientBundle {
        @Source("analyze.svg")
        SVGResource analyze();
    }
}
