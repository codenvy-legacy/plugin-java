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
package com.codenvy.ide.extension.gradle.client.task;

import com.codenvy.ide.api.parts.PartStackUIResources;
import com.codenvy.ide.api.parts.base.BaseView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/** @author Vladyslav Zhukovskii */
public class TaskListViewImpl extends BaseView<TaskListView.ActionDelegate> implements TaskListView {

    interface TaskListViewImplUiBinder extends UiBinder<Widget, TaskListViewImpl> {
    }

    @UiField
    HTMLPanel mainPanel;

    @Inject
    public TaskListViewImpl(PartStackUIResources resources,
                            TaskListViewImplUiBinder uiBinder) {
        super(resources);

        setTitle("Gradle tasks");
        this.container.add(uiBinder.createAndBindUi(this));
    }

    @Override
    public void showPanel() {
        mainPanel.setVisible(true);
    }

    @Override
    public void hidePanel() {
        mainPanel.setVisible(false);
    }


}
