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
package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.api.vfs.server.VirtualFile;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.maven.tools.Model;

import java.io.IOException;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class MavenGroupIdValueProviderFactory extends AbstractMavenValueProviderFactory {
    @Override
    public String getName() {
        return MavenAttributes.GROUP_ID;
    }

    @Override
    public ValueProvider newInstance(final Project project) {
        return new MavenValueProvider(project) {

            @Override
            protected String getValue(Model model) {
                return model.getGroupId();
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException {
                if (value.isEmpty()) {
                    return;
                }
                if (value.size() > 1) {
                    throw new ValueStorageException("Maven GroupId must be only one value.");
                }
                String groupId = value.get(0);
                if (groupId == null || groupId.isEmpty()) {
                    return;
                }
                try {
                    VirtualFile pom = getPom(project);
                    if (pom != null) {
                        final Model model = Model.readFrom(pom);
                        if (!groupId.equals(model.getGroupId())) {
                            model.setGroupId(groupId)
                                 .writeTo(pom);
                        }
                    }
                } catch (ForbiddenException | ServerException | IOException e) {
                    throwWriteException(e);
                }
            }
        };
    }
}
