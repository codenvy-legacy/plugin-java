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

package com.codenvy.ide.ext.java.client.documentation;

import elemental.html.FrameElement;

import com.codenvy.ide.toolbar.ToolbarPresenter;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class QuickDocViewImpl implements QuickDocView {


    private       ActionDelegate delegate;
    private final PopupPanel     popupPanel;
    private final Frame          frame;

    @Inject
    public QuickDocViewImpl(@Named("quickdocToolbar") ToolbarPresenter toolbarPresenter) {
        popupPanel = new PopupPanel(true, true);
        popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                delegate.onCloseView();
            }
        });

        DockLayoutPanel rootPanel = new DockLayoutPanel(Style.Unit.PX);
        SimplePanel toolPanel = new SimplePanel();
        toolPanel.setSize("100%", "100%");
        rootPanel.addSouth(toolPanel, 20);
        toolbarPresenter.go(toolPanel);

        popupPanel.setWidget(rootPanel);
        rootPanel.setSize("400px", "200px");

        frame = new Frame();
        frame.setSize("100%", "100%");
        frame.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        rootPanel.add(frame);

    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return popupPanel;
    }

    @Override
    public void show(String url) {
        frame.setUrl(url);
        popupPanel.show();
        popupPanel.center();
    }

    @Override
    public void back() {
        FrameElement element = (FrameElement)frame.getElement();
        element.getContentWindow().getHistory().back();
    }

    @Override
    public void forward() {
        FrameElement element = (FrameElement)frame.getElement();
        element.getContentWindow().getHistory().forward();
    }
}
