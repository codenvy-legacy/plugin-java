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
package com.codenvy.ide.ext.java.client.projecttree;

import com.codenvy.ide.api.projecttree.TreeSettings;

/**
 * The settings for the {@link JavaTreeStructure}.
 *
 * @author Artem Zatsarynnyy
 * @see TreeSettings
 */
public class JavaTreeSettings implements TreeSettings {
    private boolean showHiddenItems;
    private boolean compactEmptyPackages  = true;
    private boolean showExternalLibraries = true;

    @Override
    public boolean isShowHiddenItems() {
        return showHiddenItems;
    }

    @Override
    public void setShowHiddenItems(boolean showHiddenItems) {
        this.showHiddenItems = showHiddenItems;
    }

    /**
     * Checks if 'empty' packages should be shown as compacted.
     *
     * @return <code>true</code> - if 'empty' packages should be compacted, <code>false</code> - otherwise
     */
    public boolean isCompactEmptyPackages() {
        return compactEmptyPackages;
    }

    public void setCompactEmptyPackages(boolean compactEmptyPackages) {
        this.compactEmptyPackages = compactEmptyPackages;
    }

    /**
     * Checks if 'External libraries' are shown.
     *
     * @return <code>true</code> - if 'External libraries' should be shown, <code>false</code> - otherwise
     */
    public boolean isShowExternalLibraries() {
        return showExternalLibraries;
    }

    public void setShowExternalLibraries(boolean showExternalLibraries) {
        this.showExternalLibraries = showExternalLibraries;
    }
}
