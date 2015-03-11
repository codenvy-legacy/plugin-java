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
package org.eclipse.che.gradle.client;

import org.eclipse.che.ide.ui.Styles;
import com.google.gwt.resources.client.ClientBundle;

import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vladyslav Zhukovskii */
public interface GradleResources extends ClientBundle {
    @Source("gradle.svg")
    SVGResource gradle();

    @Source("build.svg")
    SVGResource build();

    @Source("module.svg")
    SVGResource module();

    public interface Css extends Styles {
        @ClassName("field")
        String field();

        @ClassName("spaceRight")
        String spaceRight();

        @ClassName("projectNamePosition")
        String projectNamePosition();

        @ClassName("wizardContentWrapped")
        String wizardContentWrapped();
    }

    @Source({"gradle.css", "com/codenvy/ide/api/ui/style.css", "com/codenvy/ide/ui/Styles.css"})
    Css getCSS();
}
