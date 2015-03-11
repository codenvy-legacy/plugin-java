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
package com.codenvy.ide.extension.gradle.server.project.type;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.server.FolderEntry;
import com.codenvy.api.project.server.InvalidValueException;
import com.codenvy.api.project.server.ValueProvider;
import com.codenvy.api.project.server.ValueProviderFactory;
import com.codenvy.api.project.server.ValueStorageException;
import com.codenvy.ide.gradle.DistributionType;
import com.codenvy.ide.gradle.GradleUtils;
import com.codenvy.ide.gradle.dto.GrdConfiguration;
import com.codenvy.ide.gradle.dto.GrdProject;
import com.codenvy.ide.gradle.dto.GrdSourceSet;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DISTRIBUTION_TYPE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.TEST_FOLDER;
import static com.codenvy.ide.gradle.DistributionType.NONE;
import static com.codenvy.ide.gradle.dto.GrdSourceSet.Type.MAIN;
import static com.codenvy.ide.gradle.dto.GrdSourceSet.Type.TEST;

/**
 * Value provider for Gradle project.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradleValueProviderFactory implements ValueProviderFactory {

    /** {@inheritDoc} */
    @Override
    public ValueProvider newInstance(final FolderEntry projectFolder) {
        return new ValueProvider() {
            @Override
            public List<String> getValues(String attributeName) throws ValueStorageException {
                try {
                    FolderEntry rootProjectFolder = GradleUtils.getRootProjectFolder(projectFolder);

                    DistributionType distributionType = GradleUtils.getProjectDistribution(rootProjectFolder);
                    if (distributionType == NONE) {
                        //we processed non gradle project
                        return null;
                    }

                    if (DISTRIBUTION_TYPE.equals(attributeName)) {
                        return Arrays.asList(distributionType.toString());
                    }

                    GrdConfiguration grdConfiguration = GradleUtils.getGrdProjectConfiguration(rootProjectFolder);

                    if (grdConfiguration.getConfigurationState() == GrdConfiguration.State.OUTDATED) {
                        return Collections.singletonList("");
                    }

                    GrdProject grdProject = getGrdProject(grdConfiguration.getProject(),
                                                          /*getRelSubProjectPath(rootProjectFolder, projectFolder)*/
                                                          projectFolder.getPath());

                    if (grdProject == null) {
                        //nothing to fetch from model, so exit
                        return Collections.singletonList("");
                    }

                    //update module state on the file system
                    //this need in future for code assistant
                    //module contains source folder paths
                    GradleUtils.writeGrdSourceSetForCA(projectFolder, grdProject.getSourceSet());

                    for (GrdSourceSet source : grdProject.getSourceSet()) {
                        if (source.getType() == MAIN && SOURCE_FOLDER.equals(attributeName)) {
                            return source.getSource();
                        } else if (source.getType() == TEST && TEST_FOLDER.equals(attributeName)) {
                            return source.getSource();
                        }
                    }

                } catch (ForbiddenException | ServerException e) {
                    throw new ValueStorageException(e.getServiceError());
                }

                return Collections.singletonList("");
            }

            @Override
            public void setValues(String attributeName, List<String> value) throws ValueStorageException, InvalidValueException {
                //temporary unsupported
            }
        };
    }

    /** Get Gradle project configuration based on sub-module path. */
    @Nullable
    private GrdProject getGrdProject(@Nonnull GrdProject grdProject, @Nonnull String relSubProjectPath) {
        if (grdProject.getDirectory().equals(relSubProjectPath)) {
            return grdProject;
        }

        for (GrdProject child : grdProject.getChild()) {
            GrdProject sink = getGrdProject(child, relSubProjectPath);
            if (sink == null) {
                continue;
            }

            return sink;
        }

        return null;
    }
}
