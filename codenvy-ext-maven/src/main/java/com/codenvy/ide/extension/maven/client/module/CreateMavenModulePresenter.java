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

package com.codenvy.ide.extension.maven.client.module;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.BuildersDescriptor;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.app.CurrentProject;
import com.codenvy.ide.api.event.RefreshProjectTreeEvent;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.ext.java.shared.Constants;
import com.codenvy.ide.extension.maven.client.wizard.MavenPomServiceClient;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.util.NameUtils;
import com.codenvy.ide.util.loging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CreateMavenModulePresenter implements CreateMavenModuleView.ActionDelegate {


    private CreateMavenModuleView view;
    private ProjectServiceClient  projectService;
    private DtoFactory            dtoFactory;
    private EventBus              eventBus;
    private MavenPomServiceClient mavenPomServiceClient;

    private String moduleName;

    private String         artifactId;
    private CurrentProject parentProject;

    @Inject
    public CreateMavenModulePresenter(CreateMavenModuleView view, ProjectServiceClient projectService, DtoFactory dtoFactory,
                                      EventBus eventBus, MavenPomServiceClient mavenPomServiceClient) {
        this.view = view;
        this.projectService = projectService;
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
        this.mavenPomServiceClient = mavenPomServiceClient;
        view.setDelegate(this);
    }

    public void showDialog(@Nonnull CurrentProject project) {
        parentProject = project;
        view.setParentArtifactId(project.getAttributeValue(MavenAttributes.ARTIFACT_ID));
        view.setGroupId(project.getAttributeValue(MavenAttributes.GROUP_ID));
        view.setVersion(project.getAttributeValue(MavenAttributes.VERSION));
        view.reset();
        view.show();
        updateViewState();
    }

    @Override
    public void onClose() {

    }

    @Override
    public void create() {
        NewProject newProject = dtoFactory.createDto(NewProject.class);
        BuildersDescriptor builders = dtoFactory.createDto(BuildersDescriptor.class);
        builders.setDefault("maven");
        newProject.setType(Constants.MAVEN_ID);
        newProject.setVisibility(parentProject.getProjectDescription().getVisibility());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put(MavenAttributes.ARTIFACT_ID, Arrays.asList(artifactId));
        attributes.put(MavenAttributes.GROUP_ID, Arrays.asList(view.getGroupId()));
        attributes.put(MavenAttributes.VERSION, Arrays.asList(view.getVersion()));
        attributes.put(MavenAttributes.PACKAGING, Arrays.asList(view.getPackaging()));
        attributes.put(MavenAttributes.PARENT_ARTIFACT_ID, Arrays.asList(parentProject.getAttributeValue(MavenAttributes.ARTIFACT_ID)));
        attributes.put(MavenAttributes.PARENT_GROUP_ID, Arrays.asList(parentProject.getAttributeValue(MavenAttributes.GROUP_ID)));
        attributes.put(MavenAttributes.PARENT_VERSION, Arrays.asList(parentProject.getAttributeValue(MavenAttributes.VERSION)));
        newProject.setAttributes(attributes);
        view.showButtonLoader(true);
        projectService.createModule(parentProject.getProjectDescription().getPath(), moduleName, newProject, new AsyncRequestCallback<ProjectDescriptor>() {

            @Override
            protected void onSuccess(ProjectDescriptor result) {
                addModuleToParentPom();
            }

            @Override
            protected void onFailure(Throwable exception) {
                view.showButtonLoader(false);
                Log.error(CreateMavenModulePresenter.class, exception);
            }
        });
    }

    private void addModuleToParentPom() {
        mavenPomServiceClient.addModule(parentProject.getProjectDescription().getPath(), moduleName, new AsyncRequestCallback<Void>() {

            @Override
            protected void onSuccess(Void result) {
                view.close();
                view.showButtonLoader(false);
                eventBus.fireEvent(new RefreshProjectTreeEvent());
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(CreateMavenModulePresenter.class, exception);
            }
        });
    }

    @Override
    public void projectNameChanged(String name) {
        if (NameUtils.checkProjectName(name)) {
            moduleName = name;
        } else {
            moduleName = null;
        }
        updateViewState();
    }

    private void updateViewState() {
        if (moduleName == null) {
            view.setNameError(true);
            view.setCreateButtonEnabled(false);
        } else {
            view.setNameError(false);
            if (artifactId == null) {
                view.setArtifactIdError(true);
                view.setCreateButtonEnabled(false);
            } else {
                view.setArtifactIdError(false);
                view.setCreateButtonEnabled(true);
            }
        }
    }

    @Override
    public void artifactIdChanged(String artifactId) {
        if (NameUtils.checkProjectName(artifactId)) {
            this.artifactId = artifactId;
        } else {
            this.artifactId = null;
        }
        updateViewState();
    }
}
