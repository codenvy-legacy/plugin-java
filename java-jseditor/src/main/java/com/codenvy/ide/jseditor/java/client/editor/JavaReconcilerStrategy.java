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

import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.editor.EditorWithErrors;
import com.codenvy.ide.api.projecttree.VirtualFile;
import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.api.texteditor.outline.OutlineModel;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.JavaLocalizationConstant;
import com.codenvy.ide.ext.java.client.editor.JavaParserWorker;
import com.codenvy.ide.ext.java.client.editor.outline.OutlineUpdater;
import com.codenvy.ide.ext.java.client.projecttree.nodes.JarClassNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.PackageNode;
import com.codenvy.ide.ext.java.client.projecttree.nodes.SourceFileNode;
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

import javax.validation.constraints.NotNull;

public class JavaReconcilerStrategy implements ReconcilingStrategy, JavaParserWorker.WorkerCallback<IProblem> {


    private final BuildContext                   buildContext;
    private final EmbeddedTextEditorPresenter<?> editor;

    private final JavaParserWorker         worker;
    private final OutlineModel             outlineModel;
    private final JavaCodeAssistProcessor  codeAssistProcessor;
    private final JavaLocalizationConstant localizationConstant;
    private final AnnotationModel          annotationModel;

    private VirtualFile      file;
    private EmbeddedDocument document;
    private boolean first = true;
    private boolean sourceFromClass;

    @AssistedInject
    public JavaReconcilerStrategy(@Assisted @NotNull final EmbeddedTextEditorPresenter<?> editor,
                                  @Assisted final OutlineModel outlineModel,
                                  @Assisted final JavaCodeAssistProcessor codeAssistProcessor,
                                  @Assisted final AnnotationModel annotationModel,
                                  final BuildContext buildContext,
                                  final JavaParserWorker worker,
                                  final JavaLocalizationConstant localizationConstant) {
        this.editor = editor;
        this.buildContext = buildContext;
        this.worker = worker;
        this.outlineModel = outlineModel;
        this.codeAssistProcessor = codeAssistProcessor;
        this.localizationConstant = localizationConstant;
        this.annotationModel = annotationModel;

    }

    @Override
    public void setDocument(final EmbeddedDocument document) {
        this.document = document;
        file = editor.getEditorInput().getFile();
        sourceFromClass = file instanceof JarClassNode;
        new OutlineUpdater(file.getPath(), outlineModel, worker);
    }

    @Override
    public void reconcile(final DirtyRegion dirtyRegion, final Region subRegion) {
        parse();
    }

    public void parse() {
        if (this.buildContext.isBuilding()) {
            return;
        }
        if (first) {
            codeAssistProcessor.disableCodeAssistant();
            first = false;
        }

        String packageName = "";
        if(file instanceof SourceFileNode) {
            if (((SourceFileNode)file).getParent() instanceof PackageNode) {
                packageName = ((PackageNode)((SourceFileNode)file).getParent()).getQualifiedName();
            }
        }

        worker.parse(document.getContents(), file.getName(), file.getPath(), packageName, file.getProject().getPath(), sourceFromClass, this);
    }

    @Override
    public void reconcile(final Region partition) {
        parse();
    }

    public VirtualFile getFile() {
        return file;
    }

    @Override
    public void onResult(final Array<IProblem> problems) {
        if (!first) {
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
