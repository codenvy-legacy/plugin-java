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
package com.codenvy.ide.jseditor.java.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.codenvy.api.analytics.logger.AnalyticsEventLogger;
import com.codenvy.ide.api.app.AppContext;
import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.ext.java.jdt.Images;
import com.codenvy.ide.ext.java.messages.WorkerProposal;
import com.codenvy.ide.jseditor.client.codeassist.CodeAssistCallback;
import com.codenvy.ide.jseditor.client.codeassist.CodeAssistProcessor;
import com.codenvy.ide.jseditor.client.codeassist.CompletionProposal;
import com.codenvy.ide.jseditor.client.texteditor.TextEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class JavaCodeAssistProcessor implements CodeAssistProcessor {

    private final AppContext appContext;
    private final BuildContext buildContext;
    private final EditorPartPresenter editor;
    private final JavaParserWorker worker;
    private final JavaResources javaResources;
    private final AnalyticsEventLogger eventLogger;

    private String errorMessage;

    @AssistedInject
    public JavaCodeAssistProcessor(@Assisted final EditorPartPresenter editor,
                                   final AppContext appContext,
                                   final BuildContext buildContext,
                                   final JavaParserWorker worker,
                                   final JavaResources javaResources,
                                   final AnalyticsEventLogger eventLogger) {
        this.appContext = appContext;
        this.buildContext = buildContext;
        this.editor = editor;
        this.worker = worker;
        this.javaResources = javaResources;
        this.eventLogger = eventLogger;
    }

    public static String insertStyle(final JavaResources javaResources, final String display) {
        if (display.contains("#FQN#")) {
            return display.replace("#FQN#", javaResources.css().fqnStyle());
        } else if (display.contains("#COUNTER#")) {
            return display.replace("#COUNTER#", javaResources.css().counter());
        } else {
            return display;
        }
    }

    public static ImageResource getImage(final JavaResources javaResources, final String image) {
        if (image == null) {
            return null;
        }
        final Images i = Images.valueOf(image);
        ImageResource img = null;
        switch (i) {
            case VARIABLE:
                img = javaResources.variable();
                break;
            case JSP_TAG_ITEM:
                img = javaResources.jspTagItem();
                break;
            case publicMethod:
                img = javaResources.publicMethod();
                break;
            case protectedMethod:
                img = javaResources.protectedMethod();
                break;
            case privateMethod:
                img = javaResources.privateMethod();
                break;
            case defaultMethod:
                img = javaResources.defaultMethod();
                break;
            case enumItem:
                img = javaResources.enumItem();
                break;
            case annotationItem:
                img = javaResources.annotationItem();
                break;
            case interfaceItem:
                img = javaResources.interfaceItem();
                break;
            case classItem:
                img = javaResources.classItem();
                break;
            case publicField:
                img = javaResources.publicField();
                break;
            case protectedField:
                img = javaResources.protectedField();
                break;
            case privateField:
                img = javaResources.privateField();
                break;
            case defaultField:
                img = javaResources.defaultField();
                break;
            case packageItem:
                img = javaResources.packageItem();
                break;
            case classDefaultItem:
                img = javaResources.classDefaultItem();
                break;
            case correction_change:
                img = javaResources.correction_change();
                break;
            case local_var:
                img = javaResources.local_var();
                break;
            case delete_obj:
                img = javaResources.delete_obj();
                break;
            case field_public:
                img = javaResources.field_public();
                break;
            case correction_cast:
                img = javaResources.correction_cast();
                break;
            case add_obj:
                img = javaResources.add_obj();
                break;
            case remove_correction:
                img = javaResources.remove_correction();
                break;
            case template:
                img = javaResources.template();
                break;
            case javadoc:
                img = javaResources.javadoc();
                break;
            case exceptionProp:
                img = javaResources.exceptionProp();
                break;
            case correction_delete_import:
                img = javaResources.correction_delete_import();
                break;
            case imp_obj:
                img = javaResources.imp_obj();
                break;
            default:
                break;
        }
        return img;
    }

    @Override
    public void computeCompletionProposals(final TextEditor textEditor, final int offset,
                                           final CodeAssistCallback callback) {
        if (buildContext.isBuilding()) {
            errorMessage = "Code Assistant currently unavailable due to project build.";
        } else {
            errorMessage = null;
        }
        if (errorMessage != null) {
            return;
        }
        this.eventLogger.log(this, "Autocompleting");
        final String fileName = editor.getEditorInput().getFile().getName();
        final String projectPath = this.appContext.getCurrentProject().getProjectDescription().getPath();
        this.worker.computeCAProposals(textEditor.getDocument().getContents(),
                                       offset, fileName, projectPath,
                                       new JavaParserWorker.WorkerCallback<WorkerProposal>() {
                                           @Override
                                           public void onResult(final Array<WorkerProposal> problems) {
                                               handleCAResponse(callback, problems);
                                           }
                                       });
    }

    private void handleCAResponse(final CodeAssistCallback callback, final Array<WorkerProposal> problems) {
        final List<CompletionProposal> proposals = new ArrayList<>(problems.size());
        for (final WorkerProposal proposal : problems.asIterable()) {
            final CompletionProposal completionProposal =
                                                          new JavaCompletionProposal(
                                                                                     proposal.id(),
                                                                                     insertStyle(javaResources, proposal.displayText()),
                                                                                     new Icon("", getImage(javaResources, proposal.image())),
                                                                                     worker);
            proposals.add(completionProposal);
        }

        callback.proposalComputed(proposals);
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void disableCodeAssistant() {
        this.errorMessage = "Code Assistant currently unavailable due to file parsing. Try again in a moment.";
    }

    public void enableCodeAssistant() {
        this.errorMessage = null;
    }
}
