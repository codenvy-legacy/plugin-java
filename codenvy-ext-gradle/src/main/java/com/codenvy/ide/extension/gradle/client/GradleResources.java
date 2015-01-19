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
package com.codenvy.ide.extension.gradle.client;

import com.google.gwt.resources.client.ClientBundle;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vladyslav Zhukovskii */
public interface GradleResources extends ClientBundle {
    @Source("gradle.svg")
    SVGResource gradle();

    @Source("task.svg")
    SVGResource task();

    @Source("recentTask.svg")
    SVGResource recentTask();
}
