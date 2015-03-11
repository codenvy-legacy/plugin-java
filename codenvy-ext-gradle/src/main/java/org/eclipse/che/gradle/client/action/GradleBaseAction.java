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

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.gradle.shared.GradleAttributes;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.SAVE;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_BUILD_GRADLE;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_SETTINGS_GRADLE;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;

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

    /** Checks if specified {@link ProjectDescriptor} is Gradle project. */
    protected boolean isGradleProject(@Nullable ProjectDescriptor project) {
        return GradleAttributes.GRADLE_ID.equals(project.getType());
    }

    /** Checks if specified {@link ProjectDescriptor} has any source folder. */
    protected boolean hasAnySourceFolder(@Nonnull ProjectDescriptor project) {
        Map<String, List<String>> attributes = project.getAttributes();

        return !(attributes.get(SOURCE_FOLDER).get(0).isEmpty() || attributes.get(TEST_FOLDER).get(0).isEmpty());
    }
}
