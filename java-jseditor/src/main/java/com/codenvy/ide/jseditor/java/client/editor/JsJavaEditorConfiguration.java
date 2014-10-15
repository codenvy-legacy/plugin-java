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

import static com.codenvy.ide.jseditor.client.partition.DefaultPartitioner.DEFAULT_PARTITIONING;
import static com.codenvy.ide.jseditor.client.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

import com.codenvy.ide.api.texteditor.outline.OutlineModel;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.collections.StringMap;
import com.codenvy.ide.ext.java.client.JavaResources;
import com.codenvy.ide.ext.java.client.editor.outline.JavaNodeRenderer;
import com.codenvy.ide.jseditor.client.annotation.AnnotationModel;
import com.codenvy.ide.jseditor.client.codeassist.CodeAssistProcessor;
import com.codenvy.ide.jseditor.client.editorconfig.DefaultTextEditorConfiguration;
import com.codenvy.ide.jseditor.client.partition.DocumentPartitioner;
import com.codenvy.ide.jseditor.client.partition.DocumentPositionMap;
import com.codenvy.ide.jseditor.client.quickfix.QuickAssistProcessor;
import com.codenvy.ide.jseditor.client.reconciler.Reconciler;
import com.codenvy.ide.jseditor.client.reconciler.ReconcilerFactory;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.codenvy.ide.util.executor.BasicIncrementalScheduler;
import com.codenvy.ide.util.executor.UserActivityManager;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Text editor configuration for java files.
 */
public class JsJavaEditorConfiguration extends DefaultTextEditorConfiguration {

    private final OutlineModel outlineModel;
    private final StringMap<CodeAssistProcessor> codeAssistProcessors;
    private final UserActivityManager userActivityManager;
    private final Reconciler reconciler;
    private final DocumentPartitioner partitioner;
    private final DocumentPositionMap documentPositionMap;
    private final AnnotationModel annotationModel;
    private final QuickAssistProcessor quickAssistProcessors;

    @AssistedInject
    public JsJavaEditorConfiguration(@Assisted final EmbeddedTextEditorPresenter editor,
                                     final UserActivityManager userActivityManager,
                                     final JavaResources javaResources,
                                     final JavaCodeAssistProcessorFactory codeAssistProcessorFactory,
                                     final JavaQuickAssistProcessorFactory quickAssistProcessorFactory,
                                     final ReconcilerFactory reconcilerFactory,
                                     final JavaPartitionerFactory partitionerFactory,
                                     final JavaReconcilerStrategyFactory strategyFactory,
                                     final Provider<DocumentPositionMap> docPositionMapProvider,
                                     final JavaAnnotationModelFactory javaAnnotationModelFactory) {
        this.outlineModel = new OutlineModel(new JavaNodeRenderer(javaResources));

        final JavaCodeAssistProcessor codeAssistProcessor = codeAssistProcessorFactory.create(editor);
        this.codeAssistProcessors = Collections.createStringMap();
        this.codeAssistProcessors.put(DEFAULT_CONTENT_TYPE, codeAssistProcessor);
        this.quickAssistProcessors = quickAssistProcessorFactory.create(editor);

        this.userActivityManager = userActivityManager;

        this.documentPositionMap = docPositionMapProvider.get();
        this.annotationModel = javaAnnotationModelFactory.create(this.documentPositionMap);

        final JavaReconcilerStrategy javaReconcilerStrategy = strategyFactory.create(editor,
                                                                                     this.outlineModel,
                                                                                     codeAssistProcessor,
                                                                                     this.annotationModel);

        this.partitioner = partitionerFactory.create(this.documentPositionMap);
        this.reconciler = initReconciler(reconcilerFactory, javaReconcilerStrategy);
    }

    @Override
    public OutlineModel getOutline() {
        return this.outlineModel;
    }

    @Override
    public StringMap<CodeAssistProcessor> getContentAssistantProcessors() {
        return this.codeAssistProcessors;
    }

    @Override
    public QuickAssistProcessor getQuickAssistProcessor() {
        return this.quickAssistProcessors;
    }

    @Override
    public Reconciler getReconciler() {
        return this.reconciler;
    }

    @Override
    public DocumentPartitioner getPartitioner() {
        return this.partitioner;
    }

    @Override
    public DocumentPositionMap getDocumentPositionMap() {
        return this.documentPositionMap;
    }

    @Override
    public AnnotationModel getAnnotationModel() {
        return this.annotationModel;
    }

    private Reconciler initReconciler(final ReconcilerFactory reconcilerFactory,
                                      final JavaReconcilerStrategy javaReconcilerStrategy) {
        final BasicIncrementalScheduler scheduler = new BasicIncrementalScheduler(userActivityManager, 50, 100);
        final Reconciler reconciler = reconcilerFactory.create(DEFAULT_PARTITIONING, scheduler, this.partitioner);
        reconciler.addReconcilingStrategy(DEFAULT_CONTENT_TYPE, javaReconcilerStrategy);
        return reconciler;
    }
}
