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
package org.eclipse.che.ide.extension.maven.client;

import org.eclipse.che.ide.ext.java.client.DependencyBuildOptionProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.che.api.builder.dto.BuildOptions;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.dto.DtoFactory;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/** @author Vladyslav Zhukovskii */
@Singleton
public class MavenDependencyBuildOptions implements DependencyBuildOptionProvider {

    private DtoFactory dtoFactory;

    @Inject
    public MavenDependencyBuildOptions(DtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

    @Override
    public String getBuilder() {
        return MAVEN_ID;
    }

    @Override
    public BuildOptions getOptionsForBinJars(ProjectDescriptor project) {
        return dtoFactory.createDto(BuildOptions.class).withTargets(Arrays.asList("-pl", project.getName()));
    }

    @Override
    public BuildOptions getOptionsForSrcJars(ProjectDescriptor project) {
        return dtoFactory.createDto(BuildOptions.class)
                         .withBuilderName(MAVEN_ID)
                         .withTargets(Arrays.asList("-pl", project.getName()))
                         .withOptions(Collections.singletonMap("-Dclassifier", "sources"));
    }
}
