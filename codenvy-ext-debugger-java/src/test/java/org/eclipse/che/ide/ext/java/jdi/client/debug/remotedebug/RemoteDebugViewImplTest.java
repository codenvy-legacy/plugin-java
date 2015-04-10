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
package org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug.RemoteDebugView.ActionDelegate;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RemoteDebugViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private JavaRuntimeLocalizationConstant locale;
    @Mock
    private JavaRuntimeResources            resources;
    @Mock
    private ActionDelegate                  delegate;
    @Mock
    private ConfirmDialog                   dialog;
    @Mock
    private DialogFactory                   dialogFactory;

    @Captor
    private ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor;
    @Captor
    private ArgumentCaptor<CancelCallback>  failureCallbackCaptor;

    private RemoteDebugViewImpl view;

    @Before
    public void setUp() {
        when(locale.connectToRemote()).thenReturn(SOME_TEXT);

        when(dialogFactory.createConfirmDialog(anyString(),
                                               Matchers.<RemoteDebugViewImpl>anyObject(),
                                               confirmCallbackCaptor.capture(),
                                               failureCallbackCaptor.capture())).thenReturn(dialog);

        view = new RemoteDebugViewImpl(locale, resources, dialogFactory);
        view.setDelegate(delegate);
    }

    @Test
    public void confirmAcceptedShouldBeCalled() throws Exception {
        when(view.host.getText()).thenReturn(SOME_TEXT);
        when(view.port.getText()).thenReturn("8000");
        verify(dialogFactory).createConfirmDialog(eq(SOME_TEXT),
                                                  eq(view),
                                                  confirmCallbackCaptor.capture(),
                                                  failureCallbackCaptor.capture());

        confirmCallbackCaptor.getValue().accepted();

        verify(delegate).onConfirmClicked(SOME_TEXT, 8000);
        verify(view.host).getText();
        verify(view.port).getText();
        verify(locale).connectToRemote();
    }

    @Test
    public void cancelCallBackShouldBeCalled() throws Exception {
        verify(dialogFactory).createConfirmDialog(eq(SOME_TEXT),
                                                  eq(view),
                                                  confirmCallbackCaptor.capture(),
                                                  failureCallbackCaptor.capture());

        failureCallbackCaptor.getValue().cancelled();

        verify(locale).connectToRemote();
    }

    @Test
    public void dialogShouldBeShown() throws Exception {
        view.show();

        verify(dialog).show();
    }

}