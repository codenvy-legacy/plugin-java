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
package com.codenvy.ide.ext.java.client.editor;

import com.codenvy.ide.api.build.BuildContext;
import com.codenvy.ide.api.editor.EditorWithErrors;
import com.codenvy.ide.api.editor.TextEditorPartPresenter;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.text.Document;
import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.api.text.annotation.AnnotationModel;
import com.codenvy.ide.api.texteditor.outline.OutlineModel;
import com.codenvy.ide.api.texteditor.reconciler.DirtyRegion;
import com.codenvy.ide.api.texteditor.reconciler.ReconcilingStrategy;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.ext.java.client.editor.outline.OutlineUpdater;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.codenvy.ide.ext.java.jdt.core.IProblemRequestor;
import com.codenvy.ide.ext.java.jdt.core.compiler.IProblem;
import com.codenvy.ide.util.loging.Log;


/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 */
public class JavaReconcilerStrategy implements ReconcilingStrategy, JavaParserWorker.WorkerCallback<IProblem> {

    private final TextEditorPartPresenter editor;
    private       Document                document;
    private       JavaParserWorker        worker;
    private       OutlineModel            outlineModel;
    private       JavaCodeAssistProcessor codeAssistProcessor;
    private       BuildContext            buildContext;
    private       FileNode                file;
    private       EditorWithErrors        editorWithErrors;
    private boolean first = true;

    public JavaReconcilerStrategy(TextEditorPartPresenter editor,
                                  JavaParserWorker worker,
                                  OutlineModel outlineModel,
                                  JavaCodeAssistProcessor codeAssistProcessor,
                                  BuildContext buildContext) {
        this.editor = editor;
        this.worker = worker;
        this.outlineModel = outlineModel;
        this.codeAssistProcessor = codeAssistProcessor;
        this.buildContext = buildContext;
        if (editor instanceof EditorWithErrors) {
            editorWithErrors = ((EditorWithErrors)editor);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDocument(Document document) {
        this.document = document;
        file = editor.getEditorInput().getFile();
        new OutlineUpdater(file.getPath(), outlineModel, worker);
    }

    /** {@inheritDoc} */
    @Override
    public void reconcile(DirtyRegion dirtyRegion, Region subRegion) {
        parse();
    }

    public void parse() {
        if (buildContext.isBuilding()) {
            return;
        }
        if (first) {
            codeAssistProcessor.disableCodeAssistant();
            first = false;
        }

        String packageName = "";
        if (file.getParent() instanceof PackageNode) {
            packageName = ((PackageNode)file.getParent()).getQualifiedName();
        }
        worker.parse(document.get(), file.getName(), file.getPath(), packageName, file.getProject().getPath(), this);
    }

    /** {@inheritDoc} */
    @Override
    public void reconcile(Region partition) {
        parse();
    }

    /** @return the file */
    public FileNode getFile() {
        return file;
    }

    @Override
    public void onResult(Array<IProblem> problems) {
        if (!first) {
            codeAssistProcessor.enableCodeAssistant();
        }
        AnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
        if (annotationModel == null)
            return;
        IProblemRequestor problemRequestor;
        if (annotationModel instanceof IProblemRequestor) {
            problemRequestor = (IProblemRequestor)annotationModel;
            problemRequestor.beginReporting();
        } else {
            if (editorWithErrors != null) {
                editorWithErrors.setErrorState(EditorWithErrors.EditorState.NONE);
            }
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
            if (editorWithErrors != null) {
                if (error) {
                    editorWithErrors.setErrorState(EditorWithErrors.EditorState.ERROR);
                } else if (warning) {
                    editorWithErrors.setErrorState(EditorWithErrors.EditorState.WARNING);
                } else {
                    editorWithErrors.setErrorState(EditorWithErrors.EditorState.NONE);
                }
            }
        } catch (Exception e) {
            Log.error(getClass(), e);
        } finally {
            problemRequestor.endReporting();
        }
    }
}
