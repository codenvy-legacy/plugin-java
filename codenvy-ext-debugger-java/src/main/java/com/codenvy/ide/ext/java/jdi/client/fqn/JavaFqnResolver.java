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

import com.codenvy.api.project.shared.dto.BuildersDescriptor;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.api.projecttree.generic.ProjectNode;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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
        final BuildersDescriptor builders = project.getData().getBuilders();
        final List<String> sourceFolders = new ArrayList<>();
        if (builders != null) {
            final String builderName = builders.getDefault();
            if (builderName != null) {
                List<String> list = project.getAttributeValues(builderName + ".source.folder");
                if (list != null) {
                    sourceFolders.addAll(list);
                }
                list = project.getAttributeValues(builderName + ".test.source.folder");
                if (list != null) {
                    sourceFolders.addAll(list);
                }
            }
        }

        final String projectPath = project.getPath();
        String path = file.getPath();
        int i = 1;
        int j = path.lastIndexOf('.');
        if (j < 0) {
            j = path.length();
        }
        for (String sourceFolder : sourceFolders) {
            boolean projectPathEndsWithSeparator = projectPath.charAt(projectPath.length() - 1) == '/';
            boolean sourcePathStartsWithSeparator = sourceFolder.charAt(0) == '/';
            boolean sourcePathEndsWithSeparator = sourceFolder.charAt(sourceFolder.length() - 1) == '/';
            String base;
            if (projectPathEndsWithSeparator && sourcePathStartsWithSeparator) {
                base = project + sourceFolder.substring(1);
            } else if (!(projectPathEndsWithSeparator || sourcePathStartsWithSeparator)) {
                base = projectPath + '/' + sourceFolder;
            } else {
                base = project + sourceFolder;
            }
            if (!sourcePathEndsWithSeparator) {
                base = base + '/';
            }

            if (path.startsWith(base)) {
                i = base.length();
                return path.substring(i, j).replaceAll("/", ".");
            }
        }
        return path.substring(i, j).replaceAll("/", ".");
    }
}