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
package com.codenvy.ide.ext.java.client.watcher;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.api.event.WindowActionEvent;
import com.codenvy.ide.api.event.WindowActionHandler;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ProjectStateListener {

    private ProjectDescriptor project;

    @Inject
    public ProjectStateListener(EventBus eventBus, final ProjectWatcherService service) {
        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                service.projectOpened(event.getProject());
                project = event.getProject();
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
                service.projectClosed(event.getProject());
                project = null;
            }
        });

        eventBus.addHandler(WindowActionEvent.TYPE, new WindowActionHandler() {
            @Override
            public void onWindowClosing(WindowActionEvent event) {
                if (project != null) {
                    service.projectClosed(project);
                }
            }

            @Override
            public void onWindowClosed(WindowActionEvent event) {

            }
        });
    }
}
