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
package org.eclipse.che.gradle.server.project.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.gradle.GradleUtils;
import org.eclipse.che.gradle.server.project.generator.ProjectGenerator;
import org.eclipse.che.gradle.server.project.generator.ProjectGeneratorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.eclipse.che.gradle.shared.GradleAttributes.GENERATION_STRATEGY_OPTION;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;

/** @author Vladyslav Zhukovskii */
@Singleton
public class CreateGradleProjectHandler implements CreateProjectHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreateGradleProjectHandler.class);
    private ProjectGeneratorProvider generatorProvider;

    @Inject
    public CreateGradleProjectHandler(ProjectGeneratorProvider generatorProvider) {
        this.generatorProvider = generatorProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {

        AttributeValue sourceFolders = attributes.get(SOURCE_FOLDER);
        if (sourceFolders != null) {
            for (String sourceFolder : sourceFolders.getList()) {
                baseFolder.createFolder(sourceFolder);
            }
        }

        AttributeValue testSourceFolders = attributes.get(TEST_FOLDER);
        if (testSourceFolders != null) {
            for (String testSourceFolder : testSourceFolders.getList()) {
                baseFolder.createFolder(testSourceFolder);
            }
        }

        ProjectGenerator generator = generatorProvider.getDefaultGenerator();

        if (!(options == null || options.get(GENERATION_STRATEGY_OPTION) == null)) {
            generator = generatorProvider.getGenerator(options.get(GENERATION_STRATEGY_OPTION));
        }

        generator.generateProject(baseFolder, attributes, options);

        if (!GradleUtils.markRootProjectAsOutdated(baseFolder)) {
            LOG.info("No root project was found.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getProjectType() {
        return GRADLE_ID;
    }
}
