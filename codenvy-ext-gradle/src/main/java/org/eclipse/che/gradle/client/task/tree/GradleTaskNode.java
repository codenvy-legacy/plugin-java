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
package org.eclipse.che.gradle.client.task.tree;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.builder.dto.BuildOptions;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.gradle.client.GradleLocalizationConstant;
import org.eclipse.che.gradle.dto.GrdTask;
import org.eclipse.che.ide.api.build.BuildContext;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.extension.builder.client.build.BuildController;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static org.eclipse.che.ide.api.notification.Notification.Type.INFO;

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
