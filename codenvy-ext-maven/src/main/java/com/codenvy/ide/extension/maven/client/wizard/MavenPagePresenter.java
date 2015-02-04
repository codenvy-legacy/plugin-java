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

import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.ImportProject;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode;
import com.codenvy.ide.api.wizard.AbstractWizardPage;
import com.codenvy.ide.collections.Jso;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.maven.client.MavenArchetype;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode.CREATE;
import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode.UPDATE;
import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static com.codenvy.ide.extension.maven.client.MavenExtension.getAvailableArchetypes;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_ARTIFACT_ID_OPTION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GROUP_ID_OPTION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_REPOSITORY_OPTION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_VERSION_OPTION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.DEFAULT_VERSION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GENERATION_STRATEGY_OPTION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.PARENT_VERSION;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static com.codenvy.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyy
 */
public class MavenPagePresenter extends AbstractWizardPage<ImportProject> implements MavenPageView.ActionDelegate {

    protected final MavenPageView         view;
    protected final EventBus              eventBus;
    private final   MavenPomServiceClient pomReaderClient;
    private final   DtoFactory            dtoFactory;

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
    public void init(ImportProject dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            // set default values
            setAttribute(VERSION, DEFAULT_VERSION);
            setAttribute(PACKAGING, "jar");
            setAttribute(SOURCE_FOLDER, DEFAULT_SOURCE_FOLDER);
            setAttribute(TEST_SOURCE_FOLDER, DEFAULT_TEST_SOURCE_FOLDER);
        }
    }

    @Override
    public boolean isCompleted() {
        return isCoordinatesCompleted();
    }

    private boolean isCoordinatesCompleted() {
        final String artifactId = getAttribute(ARTIFACT_ID);
        final String groupId = getAttribute(GROUP_ID);
        final String version = getAttribute(VERSION);

        return !(artifactId.isEmpty() || groupId.isEmpty() || version.isEmpty());
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        final String projectName = dataObject.getProject().getName();

        // use project name for artifactId and groupId for new project
        if (CREATE == wizardMode && projectName != null) {
            if (getAttribute(ARTIFACT_ID).isEmpty()) {
                setAttribute(ARTIFACT_ID, projectName);
            }
            if (getAttribute(GROUP_ID).isEmpty()) {
                setAttribute(GROUP_ID, projectName);
            }
            updateDelegate.updateControls();
        }

        updateView();
        validateCoordinates();

        view.setArchetypeSectionVisibility(UPDATE != wizardMode);
        view.enableArchetypes(view.isGenerateFromArchetypeSelected());

        if (UPDATE == wizardMode) {
            Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
            if (attributes.get(ARTIFACT_ID) == null) {
                // TODO use estimateProject() instead
                // TODO do it in generic Wizard
                pomReaderClient.readPomAttributes(projectName, new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                    @Override
                    protected void onSuccess(String result) {
                        Jso jso = Jso.deserialize(result);
                        view.setArtifactId(jso.getStringField(ARTIFACT_ID));
                        view.setGroupId(jso.getStringField(GROUP_ID));
                        view.setVersion(jso.getStringField(VERSION));
                        view.setPackaging(jso.getStringField(PACKAGING));
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

    /** Updates view from data-object. */
    private void updateView() {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();

        final String artifactId = getAttribute(ARTIFACT_ID);
        if (!artifactId.isEmpty()) {
            view.setArtifactId(artifactId);
        }

        if (attributes.get(GROUP_ID) != null) {
            view.setGroupId(getAttribute(GROUP_ID));
        } else {
            view.setGroupId(getAttribute(PARENT_GROUP_ID));
        }

        if (attributes.get(VERSION) != null) {
            view.setVersion(getAttribute(VERSION));
        } else {
            view.setVersion(getAttribute(PARENT_VERSION));
        }

        final String packaging = getAttribute(PACKAGING);
        if (!packaging.isEmpty()) {
            view.setPackaging(packaging);
        }
    }

    private void scheduleTextChanges() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onCoordinatesChanged();
            }
        });
    }

    @Override
    public void onCoordinatesChanged() {
        setAttribute(ARTIFACT_ID, view.getArtifactId());
        setAttribute(GROUP_ID, view.getGroupId());
        setAttribute(VERSION, view.getVersion());

        packagingChanged(view.getPackaging());
        validateCoordinates();
        updateDelegate.updateControls();
    }

    @Override
    public void packagingChanged(String packaging) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(PACKAGING, Arrays.asList(packaging));
        if ("pom".equals(packaging)) {
            attributes.remove(SOURCE_FOLDER);
            attributes.remove(TEST_SOURCE_FOLDER);
        } else {
            attributes.put(SOURCE_FOLDER, Arrays.asList(DEFAULT_SOURCE_FOLDER));
            attributes.put(TEST_SOURCE_FOLDER, Arrays.asList(DEFAULT_TEST_SOURCE_FOLDER));
        }

        updateDelegate.updateControls();
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

        final GeneratorDescription generatorDescription = dtoFactory.createDto(GeneratorDescription.class);
        if (isGenerateFromArchetype) {
            fillGeneratorDescription(generatorDescription);
        }
        dataObject.getProject().setGeneratorDescription(generatorDescription);

        updateDelegate.updateControls();
    }

    @Override
    public void archetypeChanged(MavenArchetype archetype) {
        fillGeneratorDescription(dataObject.getProject().getGeneratorDescription());
        updateDelegate.updateControls();
    }

    private void fillGeneratorDescription(GeneratorDescription generatorDescription) {
        MavenArchetype archetype = view.getArchetype();
        HashMap<String, String> options = new HashMap<>();
        options.put(GENERATION_STRATEGY_OPTION, ARCHETYPE_GENERATION_STRATEGY);
        options.put(ARCHETYPE_GROUP_ID_OPTION, archetype.getGroupId());
        options.put(ARCHETYPE_ARTIFACT_ID_OPTION, archetype.getArtifactId());
        options.put(ARCHETYPE_VERSION_OPTION, archetype.getVersion());
        if (archetype.getRepository() != null) {
            options.put(ARCHETYPE_REPOSITORY_OPTION, archetype.getRepository());
        }
        generatorDescription.setOptions(options);
    }

    private void validateCoordinates() {
        view.showArtifactIdMissingIndicator(view.getArtifactId().isEmpty());
        view.showGroupIdMissingIndicator(view.getGroupId().isEmpty());
        view.showVersionMissingIndicator(view.getVersion().isEmpty());
    }

    /** Reads single value of attribute from data-object. */
    @Nonnull
    private String getAttribute(String attrId) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        List<String> values = attributes.get(attrId);
        if (!(values == null || values.isEmpty())) {
            return values.get(0);
        }
        return "";
    }

    /** Sets single value of attribute of data-object. */
    private void setAttribute(String attrId, String value) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(attrId, Arrays.asList(value));
    }
}
