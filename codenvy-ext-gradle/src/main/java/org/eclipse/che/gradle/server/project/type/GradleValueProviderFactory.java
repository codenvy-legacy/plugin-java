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
package org.eclipse.che.gradle.server.project.type;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.*;
import org.eclipse.che.gradle.DistributionType;
import org.eclipse.che.gradle.GradleUtils;
import org.eclipse.che.gradle.dto.GrdConfiguration;
import org.eclipse.che.gradle.dto.GrdProject;
import org.eclipse.che.gradle.dto.GrdSourceSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.gradle.shared.GradleAttributes.DISTRIBUTION_TYPE;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;
import static org.eclipse.che.gradle.DistributionType.NONE;
import static org.eclipse.che.gradle.dto.GrdSourceSet.Type.MAIN;
import static org.eclipse.che.gradle.dto.GrdSourceSet.Type.TEST;

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
