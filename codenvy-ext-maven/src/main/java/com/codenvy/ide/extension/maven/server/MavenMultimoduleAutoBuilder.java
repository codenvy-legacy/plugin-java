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
package com.codenvy.ide.extension.maven.server;

import com.codenvy.api.builder.dto.BuildOptions;
import com.codenvy.api.builder.dto.BuildTaskDescriptor;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.UnauthorizedException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.project.server.Project;
import com.codenvy.api.project.server.ProjectCreatedEvent;
import com.codenvy.api.project.server.ProjectManager;
import com.codenvy.commons.lang.Pair;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.maven.tools.Model;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenMultimoduleAutoBuilder implements EventSubscriber<ProjectCreatedEvent> {

    private ProjectManager manager;
    private static final Logger LOG = LoggerFactory.getLogger(MavenMultimoduleAutoBuilder.class);
    @Inject
    @Named("api.endpoint")
    private String apiUrl;

    @Inject
    public MavenMultimoduleAutoBuilder(EventService eventService, ProjectManager manager) {
        this.manager = manager;
        eventService.subscribe(this);
    }

    @Override
    public void onEvent(ProjectCreatedEvent event) {
            try {
                Project project = manager.getProject(event.getWorkspaceId(), event.getProjectPath());
                if (project != null) {
                    if (project.getBaseFolder().getChild("pom.xml") != null) {
                        Model model = Model.readFrom(project.getBaseFolder().getChild("pom.xml").getVirtualFile());
                        if ("pom".equals(model.getPackaging())) {
                            buildMavenProject(project);
                        }
                    }
                }
            } catch (ForbiddenException | ServerException | IOException e) {
                LOG.error(e.getMessage(), e);
            }

    }

    private void buildMavenProject(Project project) {
        String url = apiUrl + "/builder/" + project.getWorkspace() + "/build";
        BuildOptions buildOptions = DtoFactory.getInstance().createDto(BuildOptions.class);
        buildOptions.setSkipTest(true);
        buildOptions.setTargets(Arrays.asList("install"));
        Map<String, String> options = new HashMap<>();
        options.put("-fn", null);
        options.put("-Dmaven.test.skip", "true");
        buildOptions.setOptions(options);
        Pair<String, String> projectParam = Pair.of("project", project.getPath());
        try {
            HttpJsonHelper.request(BuildTaskDescriptor.class, url, "POST", buildOptions, projectParam);
        } catch (IOException | ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
