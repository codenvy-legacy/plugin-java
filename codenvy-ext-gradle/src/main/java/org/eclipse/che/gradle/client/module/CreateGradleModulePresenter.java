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
package org.eclipse.che.gradle.client.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.BuildersDescriptor;
import org.eclipse.che.api.project.shared.dto.GeneratorDescription;
import org.eclipse.che.api.project.shared.dto.NewProject;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.gradle.client.GradleLocalizationConstant;
import org.eclipse.che.gradle.client.analyze.ProjectAnalyzer;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.api.project.tree.generic.StorableNode;
import org.eclipse.che.ide.collections.StringMap;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.ide.util.loging.Log;

import javax.annotation.Nonnull;
import java.util.*;

import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_SETTINGS_GRADLE;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_TEST_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.DISTRIBUTION_TYPE;
import static org.eclipse.che.gradle.shared.GradleAttributes.GENERATION_STRATEGY_OPTION;
import static org.eclipse.che.gradle.shared.GradleAttributes.GRADLE_ID;
import static org.eclipse.che.gradle.shared.GradleAttributes.PROJECT_CONTENT_MODIFY_OPTION;
import static org.eclipse.che.gradle.shared.GradleAttributes.SIMPLE_GENERATION_STRATEGY;
import static org.eclipse.che.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static org.eclipse.che.gradle.shared.GradleAttributes.TEST_FOLDER;

/**
 * Create new Gradle module presenter.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class CreateGradleModulePresenter implements CreateGradleModuleView.ActionDelegate {

    private CreateGradleModuleView     view;
    private ProjectAnalyzer            projectAnalyzer;
    private EditorAgent                editorAgent;
    private EventBus                   eventBus;
    private GradleLocalizationConstant localization;
    private NotificationManager        notificationManager;
    private ProjectServiceClient       projectServiceClient;
    private DtoFactory                 dtoFactory;
    private ProjectDescriptor          parentProject;
    private String                     parentFolder;

    @Inject
    public CreateGradleModulePresenter(CreateGradleModuleView view,
                                       ProjectAnalyzer projectAnalyzer,
                                       EditorAgent editorAgent,
                                       EventBus eventBus,
                                       GradleLocalizationConstant localization,
                                       NotificationManager notificationManager,
                                       ProjectServiceClient projectServiceClient,
                                       DtoFactory dtoFactory) {
        this.view = view;
        this.projectAnalyzer = projectAnalyzer;
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
        this.localization = localization;
        this.notificationManager = notificationManager;
        this.projectServiceClient = projectServiceClient;
        this.dtoFactory = dtoFactory;
        view.setDelegate(this);
    }

    /** {@inheritDoc} */
    @Override
    public void createModule() {
        view.showButtonLoader(true);

        AsyncRequestCallback<ProjectDescriptor> callback = new AsyncRequestCallback<ProjectDescriptor>() {
            @Override
            protected void onSuccess(ProjectDescriptor descriptor) {
                view.close();
                view.showButtonLoader(false);
                projectAnalyzer.analyzeProject(parentProject);

                checkSettingsGrdFileOpened();
            }

            @Override
            protected void onFailure(Throwable e) {
                notificationManager.showWarning(localization.createModuleFailed());
                Log.error(CreateGradleModulePresenter.class, e);
            }
        };

        Map<String, List<String>> attributes = new HashMap<>(3);
        attributes.put(SOURCE_FOLDER, Arrays.asList(DEFAULT_SOURCE_FOLDER));
        attributes.put(TEST_FOLDER, Arrays.asList(DEFAULT_TEST_FOLDER));
        attributes.put(DISTRIBUTION_TYPE, parentProject.getAttributes().get(DISTRIBUTION_TYPE));

        Map<String, String> options = new HashMap<>(1);
        options.put(GENERATION_STRATEGY_OPTION, SIMPLE_GENERATION_STRATEGY);
        options.put(PROJECT_CONTENT_MODIFY_OPTION, "true");

        NewProject newProject = dtoFactory.createDto(NewProject.class)
                                          .withName(view.getModuleName())
                                          .withType(GRADLE_ID)
                                          .withAttributes(attributes)
                                          .withVisibility(parentProject.getVisibility())
                                          .withBuilders(dtoFactory.createDto(BuildersDescriptor.class).withDefault(GRADLE_ID))
                                          .withGeneratorDescription(dtoFactory.createDto(GeneratorDescription.class)
                                                                              .withOptions(options)); //simple

        projectServiceClient.createModule(parentFolder, view.getModuleName(), newProject, callback);
    }

    /** Shows current dialog window. */
    public void showDialog(@Nonnull StorableNode node) {
        view.show();

        this.parentProject = node.getProject().getData();
        this.parentFolder = node.getPath();
    }

    /** {@inheritDoc} */
    @Override
    public void moduleNameChanged(@Nonnull String moduleName) {
        boolean validName = NameUtils.checkProjectName(moduleName);

        view.setNameError(!validName);
        view.setCreateButtonEnabled(validName);
    }

    /** {@inheritDoc} */
    @Override
    public void onClose() {

    }

    /** Checks if settings.gradle file is opened in editor and if true update its content. */
    private void checkSettingsGrdFileOpened() {
        StringMap<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (EditorPartPresenter editor : openedEditors.getValues().asIterable()) {
            VirtualFile file = editor.getEditorInput().getFile();
            if (DEFAULT_SETTINGS_GRADLE.equals(file.getName())) {
                eventBus.fireEvent(new FileContentUpdateEvent(file.getPath()));
            }
        }
    }
}
