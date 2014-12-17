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
package com.codenvy.ide.ext.java.server.projecttype;

import com.codenvy.api.project.server.type.ProjectType2;
import com.codenvy.ide.ext.java.shared.Constants;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * @author gazarenkov
 */
@Singleton
public class JavaProjectType extends ProjectType2 {
    private static final Logger LOG = LoggerFactory.getLogger(JavaProjectType.class);

    @Inject
    public JavaProjectType() {

        super("Java", "Java");
        addConstantDefinition(Constants.LANGUAGE, "language", "java");
        addConstantDefinition(Constants.LANGUAGE_VERSION, "language version", "1.6");

    }

}

