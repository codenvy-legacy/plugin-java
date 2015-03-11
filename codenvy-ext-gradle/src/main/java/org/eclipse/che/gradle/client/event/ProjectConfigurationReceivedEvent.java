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
package org.eclipse.che.gradle.client.event;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.gradle.dto.GrdConfiguration;

import javax.annotation.Nonnull;

/**
 * Event fired when Gradle project configuration received.
 *
 * @author Vladyslav Zhukovskii
 */
public class ProjectConfigurationReceivedEvent extends GwtEvent<ProjectConfigurationReceivedHandler> {
    private GrdConfiguration  grdConfiguration;
    private ProjectDescriptor project;

    public static Type<ProjectConfigurationReceivedHandler> TYPE = new Type<>();

    public ProjectConfigurationReceivedEvent(@Nonnull GrdConfiguration grdConfiguration,
                                             @Nonnull ProjectDescriptor project) {
        this.grdConfiguration = grdConfiguration;
        this.project = project;
    }

    @Nonnull
    public GrdConfiguration getGrdConfiguration() {
        return grdConfiguration;
    }

    @Nonnull
    public ProjectDescriptor getProject() {
        return project;
    }

    @Override
    public Type<ProjectConfigurationReceivedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProjectConfigurationReceivedHandler handler) {
        handler.onConfigurationReceived(this);
    }
}
