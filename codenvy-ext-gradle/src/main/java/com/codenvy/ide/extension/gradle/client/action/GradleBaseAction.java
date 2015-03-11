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

import com.codenvy.api.analytics.client.logger.AnalyticsEventLogger;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.action.ProjectAction;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.event.FileEvent;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.event.FileEvent.FileOperation.SAVE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DEFAULT_BUILD_GRADLE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DEFAULT_SETTINGS_GRADLE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.TEST_FOLDER;

/**
 * Base Gradle action.
 *
 * @author Vladyslav Zhukovskii
 */
public abstract class GradleBaseAction extends ProjectAction {

    protected AnalyticsEventLogger eventLogger;

    protected GradleBaseAction(String text, String description, SVGResource svgIcon) {
        super(text, description, svgIcon);
    }

    @Inject
    public void injectEventLogger(AnalyticsEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    protected void updateProjectAction(ActionEvent e) {
        e.getPresentation().setVisible(isGradleProject(appContext.getCurrentProject().getRootProject()));
    }

    /** Checks if specified {@link com.codenvy.api.project.shared.dto.ProjectDescriptor} is Gradle project. */
    protected boolean isGradleProject(@Nullable ProjectDescriptor project) {
        return GradleAttributes.GRADLE_ID.equals(project.getType());
    }

    /** Checks if specified {@link com.codenvy.api.project.shared.dto.ProjectDescriptor} has any source folder. */
    protected boolean hasAnySourceFolder(@Nonnull ProjectDescriptor project) {
        Map<String, List<String>> attributes = project.getAttributes();

        return !(attributes.get(SOURCE_FOLDER).get(0).isEmpty() || attributes.get(TEST_FOLDER).get(0).isEmpty());
    }
}
