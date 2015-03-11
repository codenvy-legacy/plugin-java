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
package com.codenvy.ide.extension.gradle.client.task.tree;

import com.codenvy.api.builder.dto.BuildOptions;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.projecttree.AbstractTreeNode;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.builder.client.build.BuildController;
import com.codenvy.ide.extension.gradle.client.GradleLocalizationConstant;
import com.codenvy.ide.gradle.dto.GrdTask;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static com.codenvy.ide.api.notification.Notification.Type.INFO;

/** @author Vladyslav Zhukovskii */
public class GradleTaskNode extends AbstractTreeNode<GrdTask> {

    private BuildController            buildController;
    private BuildContext               buildContext;
    private DtoFactory                 dtoFactory;
    private GradleLocalizationConstant localization;
    private NotificationManager        notificationManager;

    @AssistedInject
    public GradleTaskNode(@Assisted TreeNode<?> parent,
                          @Assisted GrdTask data,
                          @Assisted GradleTaskTreeStructure treeStructure,
                          EventBus eventBus,
                          BuildController buildController,
                          BuildContext buildContext,
                          DtoFactory dtoFactory,
                          GradleLocalizationConstant localization,
                          NotificationManager notificationManager) {
        super(parent, data, treeStructure, eventBus);
        this.buildController = buildController;
        this.buildContext = buildContext;
        this.dtoFactory = dtoFactory;
        this.localization = localization;
        this.notificationManager = notificationManager;
    }

    @Nonnull
    @Override
    public GradleTaskTreeStructure getTreeStructure() {
        return (GradleTaskTreeStructure)super.getTreeStructure();
    }

    @Nonnull
    @Override
    public String getId() {
        return getData().getName();
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return getData().getName();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {
        //nothing to refresh
    }

    public boolean isEnabled() {
        return getData().isEnabled();
    }

    @Override
    public void processNodeAction() {
        if (buildContext.isBuilding()) {
            final String message = localization.showTasksPleasWaitForBuild();
            Notification notification = new Notification(message, INFO);
            notificationManager.showNotification(notification);
            return;
        }

        buildController.buildActiveProject(getBuildOptions(getData()), true);
    }

    private BuildOptions getBuildOptions(GrdTask task) {
        BuildOptions buildOptions = dtoFactory.createDto(BuildOptions.class);
        buildOptions.setTargets(Arrays.asList(task.getPath()));

        return buildOptions;
    }
}
