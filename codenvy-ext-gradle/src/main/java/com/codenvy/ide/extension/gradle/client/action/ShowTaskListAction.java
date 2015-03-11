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

import com.codenvy.ide.api.action.ActionEvent;
import com.codenvy.ide.api.event.ProjectActionEvent;
import com.codenvy.ide.api.event.ProjectActionHandler;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.extension.gradle.client.task.TaskListPresenter;
import com.google.gwt.resources.client.ClientBundle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Show task list action button.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class ShowTaskListAction extends GradleBaseAction implements ProjectActionHandler {

    private TaskListPresenter presenter;

    @Inject
    public ShowTaskListAction(TaskListPresenter presenter,
                              Resources resources,
                              GradleLocalizationConstant localization,
                              EventBus eventBus) {
        super(localization.showTasksActionText(), localization.showTasksActionDescription(), resources.task());
        this.presenter = presenter;

        eventBus.addHandler(ProjectActionEvent.TYPE, this);
    }

    @Override
    protected void updateProjectAction(ActionEvent e) {
        super.updateProjectAction(e);
        e.getPresentation().setEnabled(true);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.showPanel();
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectOpened(ProjectActionEvent event) {
        if (isGradleProject(event.getProject())) {
            presenter.addToPartStack();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectClosed(ProjectActionEvent event) {
        if (isGradleProject(event.getProject())) {
            presenter.removeFromPartStack();
        }
    }

    public interface Resources extends ClientBundle {
        @Source("task.svg")
        SVGResource task();
    }
}
