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
package com.codenvy.ide.ext.java.jdi.client.fqn;

import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaFqnResolver implements FqnResolver {
    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String resolveFqn(@Nonnull final FileNode file) {
        final ProjectNode project = file.getProject();
        final String builderName = project.getData().getBuilder();
        final List<String> sourceFolders = project.getAttributeValues("builder." + builderName + ".source_folders");

        String fqn = "";
        for (String sourceFolder : sourceFolders) {
            if (file.getPath().startsWith(project.getPath() + "/" + sourceFolder)) {
                fqn = file.getPath().substring((project.getPath() + "/" + sourceFolder + "/").length());
                break;
            }
        }

        fqn = fqn.replaceAll("/", ".");
        fqn = fqn.substring(0, fqn.lastIndexOf('.'));
        return fqn;
    }
}