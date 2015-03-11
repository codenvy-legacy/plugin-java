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
package org.eclipse.che.gradle.client.task.tree;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;

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
