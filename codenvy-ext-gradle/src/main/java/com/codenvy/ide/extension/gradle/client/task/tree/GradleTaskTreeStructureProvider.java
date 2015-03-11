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
package com.codenvy.ide.extension.gradle.client.task.tree;

import com.codenvy.ide.api.projecttree.TreeStructure;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GradleTaskTreeStructureProvider implements TreeStructureProvider {
    private GradleTaskNodeFactory nodeFactory;

    @Inject
    public GradleTaskTreeStructureProvider(GradleTaskNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    @Nonnull
    @Override
    public String getId() {
        return "gradle_tasks";
    }

    @Override
    public TreeStructure get() {
        return new GradleTaskTreeStructure(nodeFactory);
    }
}
