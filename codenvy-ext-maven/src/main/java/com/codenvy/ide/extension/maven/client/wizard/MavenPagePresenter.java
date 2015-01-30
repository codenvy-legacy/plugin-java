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
package com.codenvy.ide.extension.maven.client.wizard;

import com.codenvy.api.project.shared.dto.BuildersDescriptor;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.NewProject;
import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.wizard1.AbstractWizardPage;
import com.codenvy.ide.collections.Jso;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.maven.client.MavenArchetype;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.extension.maven.client.MavenExtension.getAvailableArchetypes;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenPagePresenter extends AbstractWizardPage<NewProject> implements MavenPageView.ActionDelegate {

    protected MavenPageView             view;
    protected EventBus                  eventBus;
    private   MavenPomServiceClient     pomReaderClient;
    private   Map<String, List<String>> attributes;
    private   DtoFactory                dtoFactory;

    @Inject
    public MavenPagePresenter(MavenPageView view, EventBus eventBus, MavenPomServiceClient pomReaderClient, DtoFactory dtoFactory) {
        super();
        this.view = view;
        this.eventBus = eventBus;
        this.pomReaderClient = pomReaderClient;
        this.dtoFactory = dtoFactory;
        view.setDelegate(this);
    }

    @Override
    public void init(NewProject dataObject) {
        super.init(dataObject);

        // TODO: add constants for wizard context
        final boolean isCreatingNewProject = Boolean.parseBoolean(context.get("isCreatingNewProject"));
        if (isCreatingNewProject) {
            // set default values
            Map<String, List<String>> attr = dataObject.getAttributes();
            attr.put(MavenAttributes.VERSION, Arrays.asList("1.0-SNAPSHOT"));
            attr.put(MavenAttributes.SOURCE_FOLDER, Arrays.asList("src/main/java"));
            attr.put(MavenAttributes.TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        }
    }

    @Override
    public boolean isCompleted() {
        // TODO: move it to go() method
        boolean isArtifactIdCompleted = !view.getArtifactId().equals("");
        boolean isGroupIdCompleted = !view.getGroupId().equals("");
        boolean isVersionFieldCompleted = !view.getVersion().equals("");

        view.showArtifactIdMissingIndicator(!isArtifactIdCompleted);
        view.showGroupIdMissingIndicator(!isGroupIdCompleted);
        view.showVersionMissingIndicator(!isVersionFieldCompleted);

        return isCoordinatesValid();
    }

    private boolean isCoordinatesValid() {
        Map<String, List<String>> attr = dataObject.getAttributes();

        List<String> artifactIdValues = attr.get(MavenAttributes.ARTIFACT_ID);
        final boolean artifactIdCompleted = !(artifactIdValues == null || artifactIdValues.isEmpty() || artifactIdValues.get(0).isEmpty());

        List<String> groupIdValues = attr.get(MavenAttributes.GROUP_ID);
        final boolean groupIdCompleted = !(groupIdValues == null || groupIdValues.isEmpty() || groupIdValues.get(0).isEmpty());

        List<String> versionValues = attr.get(MavenAttributes.VERSION);
        final boolean versionCompleted = !(versionValues == null || versionValues.isEmpty() || versionValues.get(0).isEmpty());

        return artifactIdCompleted && groupIdCompleted && versionCompleted;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        view.reset();

        // TODO: update view from dataObject

        // setting project name from the main wizard page
        final String projectName = dataObject.getName();
        if (projectName != null) {
            view.setArtifactId(projectName);
            view.setGroupId(projectName);
            scheduleTextChanges();
        }

        ProjectDescriptor projectUpdate = null; //wizardContext.getData(ProjectWizard.PROJECT_FOR_UPDATE);
//        ProjectDescriptor project = wizardContext.getData(ProjectWizard.PROJECT);

        view.setArchetypeSectionVisibility(projectUpdate == null);
        view.setPackagingVisibility(!view.isGenerateFromArchetypeSelected());
        view.enableArchetypes(view.isGenerateFromArchetypeSelected());
//        if (projectUpdate == null && view.isGenerateFromArchetypeSelected()) {
//            view.setArchetypes(getAvailableArchetypes());
//        }

        if (dataObject != null) {
            attributes = dataObject.getAttributes();
            attributes.put(MavenAttributes.SOURCE_FOLDER, Arrays.asList("src/main/java"));
            attributes.put(MavenAttributes.TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));

            if (view.isGenerateFromArchetypeSelected()) {
                HashMap<String, String> options = new HashMap<>();
                options.put("type", MavenAttributes.ARCHETYPE_GENERATION_STRATEGY);
                dataObject.setGeneratorDescription(dtoFactory.createDto(GeneratorDescription.class).withOptions(options));
            } else {
                dataObject.setGeneratorDescription(dtoFactory.createDto(GeneratorDescription.class));
            }

            BuildersDescriptor builders = dataObject.getBuilders();
            if (builders == null) {
                builders = dtoFactory.createDto(BuildersDescriptor.class);
                dataObject.setBuilders(builders);
            }
            builders.setDefault("maven");
            if (projectUpdate != null) {
                List<String> artifactIdAttr = attributes.get(MavenAttributes.ARTIFACT_ID);
                if (artifactIdAttr != null) {
                    view.setArtifactId(artifactIdAttr.get(0));
                    if (attributes.get(MavenAttributes.GROUP_ID) == null || attributes.get(MavenAttributes.GROUP_ID).isEmpty()) {
                        view.setGroupId(attributes.get(MavenAttributes.PARENT_GROUP_ID).get(0));
                    } else {
                        view.setGroupId(attributes.get(MavenAttributes.GROUP_ID).get(0));
                    }

                    if (attributes.get(MavenAttributes.VERSION) == null || attributes.get(MavenAttributes.VERSION).isEmpty()) {
                        view.setVersion(attributes.get(MavenAttributes.PARENT_VERSION).get(0));
                    } else {
                        view.setVersion(attributes.get(MavenAttributes.VERSION).get(0));
                    }
                    view.setPackaging(attributes.get(MavenAttributes.PACKAGING).get(0));
                    scheduleTextChanges();
                } else {
                    // TODO use estimateProject() instead
                    // TODO do it in generic Wizard

                    pomReaderClient.readPomAttributes(projectUpdate.getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                        @Override
                        protected void onSuccess(String result) {
                            Jso jso = Jso.deserialize(result);
                            view.setArtifactId(jso.getStringField(MavenAttributes.ARTIFACT_ID));
                            view.setGroupId(jso.getStringField(MavenAttributes.GROUP_ID));
                            view.setVersion(jso.getStringField(MavenAttributes.VERSION));
                            view.setPackaging(jso.getStringField(MavenAttributes.PACKAGING));
                            scheduleTextChanges();
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            Log.error(MavenPagePresenter.class, exception);
                        }
                    });
                }
            }
        }
    }

    private void scheduleTextChanges() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onTextsChange();
            }
        });
    }

    @Override
    public void onTextsChange() {
        attributes.put(MavenAttributes.ARTIFACT_ID, Arrays.asList(view.getArtifactId()));
        attributes.put(MavenAttributes.GROUP_ID, Arrays.asList(view.getGroupId()));
        attributes.put(MavenAttributes.VERSION, Arrays.asList(view.getVersion()));
        packagingChanged(view.getPackaging());

        updateDelegate.updateControls();
    }

    @Override
    public void packagingChanged(String packaging) {
        attributes.put(MavenAttributes.PACKAGING, Arrays.asList(packaging));
        if ("pom".equals(packaging)) {
            attributes.remove(MavenAttributes.SOURCE_FOLDER);
            attributes.remove(MavenAttributes.TEST_SOURCE_FOLDER);
        } else {
            attributes.put(MavenAttributes.SOURCE_FOLDER, Arrays.asList("src/main/java"));
            attributes.put(MavenAttributes.TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        }
    }

    @Override
    public void generateFromArchetypeChanged(boolean isGenerateFromArchetype) {
        view.setPackagingVisibility(!isGenerateFromArchetype);
        view.enableArchetypes(isGenerateFromArchetype);
        if (!isGenerateFromArchetype) {
            view.clearArchetypes();
        } else {
            view.setArchetypes(getAvailableArchetypes());
        }

        if (isGenerateFromArchetype) {
            dataObject.setGeneratorDescription(getGeneratorDescription(view.getArchetype()));
        } else {
            dataObject.setGeneratorDescription(dtoFactory.createDto(GeneratorDescription.class));
        }
    }

    @Override
    public void archetypeChanged(MavenArchetype archetype) {
        dataObject.setGeneratorDescription(getGeneratorDescription(view.getArchetype()));
    }

    private GeneratorDescription getGeneratorDescription(MavenArchetype archetype) {
        HashMap<String, String> options = new HashMap<>();
        options.put("type", MavenAttributes.ARCHETYPE_GENERATION_STRATEGY);
        options.put("archetypeGroupId", archetype.getGroupId());
        options.put("archetypeArtifactId", archetype.getArtifactId());
        options.put("archetypeVersion", archetype.getVersion());
        if (archetype.getRepository() != null) {
            options.put("archetypeRepository", archetype.getRepository());
        }
        return dtoFactory.createDto(GeneratorDescription.class).withOptions(options);
    }
}
