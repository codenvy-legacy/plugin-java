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

import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.editor.EditorAgent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class QuickDocPresenter implements QuickDocumentation, QuickDocView.ActionDelegate {


    private QuickDocView view;
    private AppContext   appContext;
    private String       caContext;
    private EditorAgent editorAgent;

    @Inject
    public QuickDocPresenter(QuickDocView view, AppContext appContext, @Named("javaCA") String caContext, EditorAgent editorAgent) {
        this.view = view;
        this.appContext = appContext;
        this.caContext = caContext;
        this.editorAgent = editorAgent;
    }

    @Override
    public void showDocumentation() {
//        editorAgent.getActiveEditor()


//        view.show(caContext + "/javadoc/" + appContext.getWorkspace().getName() + "/find?fqn=java.lang.String&projectpath=" +
//                  appContext.getCurrentProject().getProjectDescription().getPath());
    }

    @Override
    public void back() {
        view.back();
    }

    @Override
    public void forward() {
        view.forward();
    }

    @Override
    public void onCloseView() {

    }
}
