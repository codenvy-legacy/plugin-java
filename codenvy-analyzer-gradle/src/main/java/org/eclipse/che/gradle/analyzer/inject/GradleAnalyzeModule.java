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
package org.eclipse.che.gradle.analyzer.inject;

import com.google.inject.AbstractModule;

import org.eclipse.che.gradle.analyzer.AnalyzeModelExecutor;

/** @author Vladyslav Zhukovskii */
public class GradleAnalyzeModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(AnalyzeModelExecutor.class).asEagerSingleton();
        System.out.println("GradleAnalyzerModule registered");
    }
}
