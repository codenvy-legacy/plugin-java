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

import com.codenvy.ide.api.projecttree.TreeSettings;

/** @author Vladyslav Zhukovskii */
public interface GradleTaskTreeSettings extends TreeSettings {
    GradleTaskTreeSettings DEFAULT = new GradleTaskTreeSettings() {
        @Override
        public boolean isShowHiddenItems() {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public void setShowHiddenItems(boolean showHiddenItems) {
            throw new UnsupportedOperationException("Unsupported");
        }
    };
}
