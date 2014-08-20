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
package com.codenvy.ide.extension.maven.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.api.projecttree.TreeStructureProvider;
import com.codenvy.ide.extension.maven.client.build.MavenBuildView;
import com.codenvy.ide.extension.maven.client.build.MavenBuildViewImpl;
import com.codenvy.ide.extension.maven.client.tree.MavenProjectTreeStructureProvider;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;

/** @author Andrey Plotnikov */
@ExtensionGinModule
public class MavenGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        GinMultibinder<TreeStructureProvider> treeStructureBinder = GinMultibinder.newSetBinder(binder(), TreeStructureProvider.class);
        treeStructureBinder.addBinding().to(MavenProjectTreeStructureProvider.class);

        bind(MavenBuildView.class).to(MavenBuildViewImpl.class).in(Singleton.class);
    }
}
