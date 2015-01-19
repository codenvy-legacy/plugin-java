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
package com.codenvy.ide.extension.gradle.client.task;

import elemental.dom.Element;
import elemental.html.TableCellElement;
import elemental.html.TableElement;

import com.codenvy.ide.api.parts.PartStackUIResources;
import com.codenvy.ide.api.parts.base.BaseView;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.extension.gradle.client.GradleResources;
import com.codenvy.ide.extension.gradle.shared.dto.Task;
import com.codenvy.ide.ui.list.SimpleList;
import com.codenvy.ide.util.dom.Elements;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;
import java.util.List;

/** @author Vladyslav Zhukovskii */
public class TaskListViewImpl extends BaseView<TaskListView.ActionDelegate> implements TaskListView {

    interface TaskListViewImplUiBinder extends UiBinder<Widget, TaskListViewImpl> {
    }

    @UiField
    FlowPanel recentTasksPanel;

    @UiField
    FlowPanel allTasksPanel;

    @UiField(provided = true)
    GradleResources resources;

    private SimpleList<Task> recentTasks;
    private SimpleList<Task> tasks;

    private ActionDelegate delegate;

    @Inject
    public TaskListViewImpl(PartStackUIResources partStackUIResources,
                            TaskListViewImplUiBinder uiBinder,
                            com.codenvy.ide.Resources coreRes,
                            GradleResources resources) {
        super(partStackUIResources);
        this.resources = resources;

        setTitle("Gradle tasks");
        this.container.add(uiBinder.createAndBindUi(this));

        TableElement recentTaskElement = Elements.createTableElement();
        recentTaskElement.setAttribute("style", "width: 100%");

        TableElement taskElement = Elements.createTableElement();
        taskElement.setAttribute("style", "width: 100%");


        recentTasks = SimpleList
                .create((SimpleList.View)recentTaskElement, coreRes.defaultSimpleListCss(), recentTasksRenderer, recentTasksDelegate);


        tasks = SimpleList
                .create((SimpleList.View)taskElement, coreRes.defaultSimpleListCss(), tasksRenderer, tasksDelegate);

        ScrollPanel scroller = new ScrollPanel(tasks.asWidget());
        scroller.setAlwaysShowScrollBars(true);

        recentTasksPanel.add(recentTasks);
        allTasksPanel.add(scroller);
    }

    @Override
    public void showRecentTaskList(@Nonnull List<Task> taskList) {
        recentTasks.render(taskList);
    }

    @Override
    public void showTaskList(Array<Task> taskList) {
        tasks.render(taskList);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    SimpleList.ListEventDelegate<Task> recentTasksDelegate = new SimpleList.ListEventDelegate<Task>() {
        @Override
        public void onListItemClicked(Element listItemBase, Task itemData) {
            recentTasks.getSelectionModel().setSelectedItem(itemData);
            tasks.getSelectionModel().clearSelection();
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, Task selectedTask) {
            delegate.executeTask(selectedTask);
        }
    };

    SimpleList.ListItemRenderer<Task> recentTasksRenderer = new SimpleList.ListItemRenderer<Task>() {
        @Override
        public void render(Element listItemBase, Task itemData) {
            TableCellElement label = Elements.createTDElement();
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant("<table><tr>");

            SVGResource taskIcon = resources.recentTask();
            sb.appendHtmlConstant("<td><img src=\"" + taskIcon.getSafeUri().asString() + "\" width=\"16\" height=\"16\"></td>");

            sb.appendHtmlConstant("<td>");
            sb.appendEscaped(itemData.getName());
            sb.appendHtmlConstant("</td></tr></table>");

            label.setInnerHTML(sb.toSafeHtml().asString());

            listItemBase.appendChild(label);
        }

        @Override
        public Element createElement() {
            return Elements.createTRElement();
        }
    };

    SimpleList.ListEventDelegate<Task> tasksDelegate = new SimpleList.ListEventDelegate<Task>() {
        @Override
        public void onListItemClicked(Element listItemBase, Task itemData) {
            tasks.getSelectionModel().setSelectedItem(itemData);
            recentTasks.getSelectionModel().clearSelection();
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, Task selectedTask) {
            delegate.executeTask(selectedTask);
        }
    };

    SimpleList.ListItemRenderer<Task> tasksRenderer = new SimpleList.ListItemRenderer<Task>() {
        @Override
        public void render(Element listItemBase, Task itemData) {
            TableCellElement label = Elements.createTDElement();
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant("<table><tr>");

            SVGResource taskIcon = resources.task();
            sb.appendHtmlConstant("<td><img src=\"" + taskIcon.getSafeUri().asString() + "\" width=\"16\" height=\"16\"></td>");

            sb.appendHtmlConstant("<td>");
            sb.appendEscaped(itemData.getName());
            sb.appendHtmlConstant("</td></tr></table>");

            label.setInnerHTML(sb.toSafeHtml().asString());

            listItemBase.appendChild(label);
        }

        @Override
        public Element createElement() {
            return Elements.createTRElement();
        }
    };
}
