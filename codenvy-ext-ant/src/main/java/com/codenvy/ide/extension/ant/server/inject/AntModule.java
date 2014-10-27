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
package com.codenvy.ide.extension.ant.server.inject;

import com.codenvy.api.project.server.ProjectTypeResolver;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.ide.extension.ant.server.project.type.*;
import com.codenvy.inject.DynaModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/** @author Vladyslav Zhukovskii */
@DynaModule
public class AntModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(AntProjectTypeDescriptionsExtension.class);
        bind(AntProjectTypeExtension.class);
        Multibinder.newSetBinder(binder(), ProjectTypeResolver.class).addBinding().to(AntProjectTypeResolver.class);

        Multibinder<ValueProviderFactory> multiBinder = Multibinder.newSetBinder(binder(), ValueProviderFactory.class);
        multiBinder.addBinding().to(AntProjectContentValueProvider.class);
        multiBinder.addBinding().to(AntSourceFolderValueProviderFactory.class);
        multiBinder.addBinding().to(AntTestSourceFolderValueProviderFactory.class);
    }
}
