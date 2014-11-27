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

package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.MavenUtils;

import org.apache.maven.model.Model;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class MavenParentGroupIdValueProviderFactory extends AbstractMavenValueProviderFactory {
    @Override
    public String getName() {
        return MavenAttributes.PARENT_GROUP_ID;
    }

    @Override
    public ValueProvider newInstance(final Project project) {
        return new MavenValueProvider(project) {
            @Override
            protected String getValue(Model model) {
                if (model.getParent() != null) {
                    return model.getParent().getGroupId();
                }
                return null;
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException, InvalidValueException {
                if (value == null || value.isEmpty()) {
                    return;
                }

                try {
                    VirtualFile pom = getPom(project);
                    if (pom != null) {
                        MavenUtils.setParentGroupId(pom, value.get(0));
                    }
                } catch (ServerException | ForbiddenException | IOException e) {
                    throwWriteException(e);
                }
            }
        };
    }
}
