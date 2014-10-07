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

import static com.codenvy.ide.api.notification.Notification.Status.FINISHED;

import javax.validation.constraints.NotNull;

import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.editor.EditorWithErrors;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.api.texteditor.outline.OutlineModel;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.ext.java.client.editor.outline.OutlineUpdater;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.jdt.core.IProblemRequestor;
import com.codenvy.ide.ext.java.jdt.core.compiler.IProblem;
import com.codenvy.ide.jseditor.client.annotation.AnnotationModel;
import com.codenvy.ide.jseditor.client.document.EmbeddedDocument;
import com.codenvy.ide.jseditor.client.reconciler.DirtyRegion;
import com.codenvy.ide.jseditor.client.reconciler.ReconcilingStrategy;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class JavaReconcilerStrategy implements ReconcilingStrategy, JavaParserWorker.WorkerCallback<IProblem> {

    private final EmbeddedTextEditorPresenter editor;
    private final JavaParserWorker worker;
    private final OutlineModel outlineModel;
    private final NotificationManager notificationManager;
    private final JavaCodeAssistProcessor codeAssistProcessor;
    private final JavaLocalizationConstant localizationConstant;
    private final AnnotationModel annotationModel;

    private FileNode file;
    private EmbeddedDocument document;
    private boolean first = true;
    private Notification notification;

    @AssistedInject
    public JavaReconcilerStrategy(@Assisted @NotNull final EmbeddedTextEditorPresenter editor,
                                  @Assisted final OutlineModel outlineModel,
                                  @Assisted final JavaCodeAssistProcessor codeAssistProcessor,
                                  @Assisted final AnnotationModel annotationModel,
                                  final JavaParserWorker worker,
                                  final NotificationManager notificationManager,
                                  final JavaLocalizationConstant localizationConstant) {
        this.editor = editor;
        this.worker = worker;
        this.outlineModel = outlineModel;
        this.notificationManager = notificationManager;
        this.codeAssistProcessor = codeAssistProcessor;
        this.localizationConstant = localizationConstant;
        this.annotationModel = annotationModel;

        editor.addCloseHandler(new EditorPartPresenter.EditorPartCloseHandler() {
            @Override
            public void onClose(final EditorPartPresenter editor) {
                if (notification != null && !notification.isFinished()) {
                    notification.setStatus(FINISHED);
                    notification.setType(Notification.Type.WARNING);
                    notification.setMessage("Parsing file canceled");
                    notification = null;
                }
            }
        });
    }

    @Override
    public void setDocument(final EmbeddedDocument document) {
        this.document = document;
        file = editor.getEditorInput().getFile();
        new OutlineUpdater(file.getPath(), outlineModel, worker);
    }

    @Override
    public void reconcile(final DirtyRegion dirtyRegion, final Region subRegion) {
        parse();
    }

    public void parse() {
        if (first) {
            notification = new Notification("Parsing file...", Notification.Status.PROGRESS);
            codeAssistProcessor.disableCodeAssistant();
            notificationManager.showNotification(notification);
            first = false;
        }

        String packageName = "";
        if (file.getParent() instanceof PackageNode) {
            packageName = ((PackageNode)file.getParent()).getQualifiedName();
        }
        worker.parse(document.getContents(), file.getName(), file.getPath(), packageName, file.getProject().getPath(), this);
    }

    @Override
    public void reconcile(final Region partition) {
        parse();
    }

    public FileNode getFile() {
        return file;
    }

    @Override
    public void onResult(final Array<IProblem> problems) {
        if (!first) {
            if (notification != null) {
                notification.setStatus(FINISHED);
                notification.setMessage(localizationConstant.fileFuccessfullyParsed());
                notification = null;
            }
            codeAssistProcessor.enableCodeAssistant();
        }

        if (this.annotationModel == null) {
            return;
        }
        IProblemRequestor problemRequestor;
        if (this.annotationModel instanceof IProblemRequestor) {
            problemRequestor = (IProblemRequestor)this.annotationModel;
            problemRequestor.beginReporting();
        } else {
            editor.setErrorState(EditorWithErrors.EditorState.NONE);
            return;
        }
        try {
            boolean error = false;
            boolean warning = false;
            for (IProblem problem : problems.asIterable()) {
                if (!error) {
                    error = problem.isError();
                }
                if (!warning) {
                    warning = problem.isWarning();
                }
                problemRequestor.acceptProblem(problem);
            }
            if (error) {
                editor.setErrorState(EditorWithErrors.EditorState.ERROR);
            } else if (warning) {
                editor.setErrorState(EditorWithErrors.EditorState.WARNING);
            } else {
                editor.setErrorState(EditorWithErrors.EditorState.NONE);
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            problemRequestor.endReporting();
        }
    }
}
