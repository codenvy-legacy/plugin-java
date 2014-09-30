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
package com.codenvy.ide.ext.java.client.newsourcefile;

import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ui.window.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation of {@link NewJavaSourceFileView}.
 *
 * @author Artem Zatsarynnyy
 */
public class NewJavaSourceFileViewImpl extends Window implements NewJavaSourceFileView {
    final Button btnOk;
    @UiField
    TextBox nameField;
    @UiField
    Label   errorHintField;
    @UiField
    ListBox typeField;
    private ActionDelegate delegate;

    private Array<JavaSourceFileType> sourceFileTypes = Collections.createArray();

    @Inject
    public NewJavaSourceFileViewImpl(AddToIndexViewImplUiBinder uiBinder, JavaLocalizationConstant constant) {
        setTitle(constant.title());

        Button btnCancel = createButton(constant.buttonCancel(), "newJavaClass-dialog-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        getFooter().add(btnCancel);

        btnOk = createButton(constant.buttonOk(), "newJavaClass-dialog-ok", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onOkClicked();
            }
        });
        getFooter().add(btnOk);

        Widget widget = uiBinder.createAndBindUi(this);
        this.setWidget(widget);
        this.ensureDebugId("newJavaSourceFileView-window");

        nameField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                delegate.onNameChanged();
                final boolean isNameEmpty = nameField.getText().trim().isEmpty();
                btnOk.setEnabled(!isNameEmpty);
                if (!isNameEmpty && KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                    delegate.onOkClicked();
                }
            }
        });
    }

    @Override
    public void setTypes(Array<JavaSourceFileType> types) {
        sourceFileTypes.clear();
        typeField.clear();
        sourceFileTypes.addAll(types);
        for (JavaSourceFileType type : sourceFileTypes.asIterable()) {
            typeField.addItem(type.toString());
        }
    }

    @Override
    public String getName() {
        return nameField.getText();
    }

    @Override
    public JavaSourceFileType getSelectedType() {
        return sourceFileTypes.get(typeField.getSelectedIndex());
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void showErrorHint(String text) {
        errorHintField.setText(text);
    }

    @Override
    public void hideErrorHint() {
        errorHintField.setText("");
    }

    @Override
    public void showDialog() {
        nameField.setText("");
        hideErrorHint();
        show();
        btnOk.setEnabled(false);
        new Timer() {
            @Override
            public void run() {
                nameField.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {
    }

    interface AddToIndexViewImplUiBinder extends UiBinder<Widget, NewJavaSourceFileViewImpl> {
    }
}
