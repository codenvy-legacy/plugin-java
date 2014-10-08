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
package com.codenvy.ide.jseditor.java.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.jseditor.java.client.editor.JavaAnnotationModelFactory;
import com.codenvy.ide.jseditor.java.client.editor.JavaCodeAssistProcessorFactory;
import com.codenvy.ide.jseditor.java.client.editor.JavaPartitionScanner;
import com.codenvy.ide.jseditor.java.client.editor.JavaPartitionerFactory;
import com.codenvy.ide.jseditor.java.client.editor.JavaReconcilerStrategyFactory;
import com.codenvy.ide.jseditor.java.client.editor.JsJavaEditorConfigurationFactory;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

@ExtensionGinModule
public class JavaJsEditorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(JavaCodeAssistProcessorFactory.class));
        install(new GinFactoryModuleBuilder().build(JsJavaEditorConfigurationFactory.class));
        install(new GinFactoryModuleBuilder().build(JavaReconcilerStrategyFactory.class));
        install(new GinFactoryModuleBuilder().build(JavaAnnotationModelFactory.class));
        bind(JavaPartitionScanner.class);
        bind(JavaPartitionerFactory.class);
    }
}
