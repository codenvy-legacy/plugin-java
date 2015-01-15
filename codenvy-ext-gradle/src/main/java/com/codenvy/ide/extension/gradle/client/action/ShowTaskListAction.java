/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
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
import com.codenvy.ide.api.action.Action;
import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.extension.gradle.client.task.TaskListPresenter;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** @author Vladyslav Zhukovskii */
@Singleton
public class ShowTaskListAction extends Action {

    private TaskListPresenter    taskListPresenter;
    private AppContext           appContext;
    private AnalyticsEventLogger eventLogger;

    @Inject
    public ShowTaskListAction(TaskListPresenter taskListPresenter,
                              AppContext appContext,
                              AnalyticsEventLogger eventLogger) {
        super("Gradle tasks", "Show Gradle tasks for current project.");
        this.taskListPresenter = taskListPresenter;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        taskListPresenter.showPanel();
    }

    @Override
    public void update(ActionEvent e) {
        CurrentProject project = appContext.getCurrentProject();
        e.getPresentation()
         .setEnabledAndVisible(project != null && GradleAttributes.GRADLE_ID.equals(project.getProjectDescription().getType()));
    }
}
