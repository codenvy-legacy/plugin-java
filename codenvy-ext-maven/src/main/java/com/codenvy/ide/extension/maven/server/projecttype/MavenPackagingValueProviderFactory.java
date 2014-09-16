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

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.shared.ValueProvider;
import com.codenvy.api.project.shared.ValueStorageException;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;

import org.apache.maven.model.Model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class MavenPackagingValueProviderFactory extends AbstractMavenValueProviderFactory {
    @Override
    public String getName() {
        return MavenAttributes.MAVEN_PACKAGING;
    }

    @Override
    public ValueProvider newInstance(final Project project) {
        return new ValueProvider() {
            @Override
            public List<String> getValues() throws ValueStorageException {
                final List<String> list = new LinkedList<>();
                try {

                    final Model model = readModel(project);
                    list.add(model.getPackaging());
                } catch (ForbiddenException | ServerException | IOException e) {
                    throwReadException(e);
                }
                return list;
            }

            @Override
            public void setValues(List<String> value) throws ValueStorageException {
                if (value.isEmpty()) {
                    throw new IllegalStateException("Maven Packaging can't be empty.");
                }
                if (value.size() > 1) {
                    throw new IllegalStateException("Maven Packaging must be only one value.");
                }
                try {

                    Model model = readOrCreateModel(project);
                    model.setPackaging(value.get(0));
                    writeModel(model, project);
                } catch (ForbiddenException | ServerException | ConflictException | IOException e) {
                    throwWriteException(e);
                }
            }
        };
    }
}
