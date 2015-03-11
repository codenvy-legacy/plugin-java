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
package com.codenvy.ide.extension.gradle.client.wizard;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.server.ProjectService;
import com.codenvy.api.project.shared.dto.GeneratorDescription;
import com.codenvy.api.project.shared.dto.ImportProject;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode;
import com.codenvy.ide.api.wizard.AbstractWizardPage;
import com.codenvy.ide.dto.DtoFactory;
import com.codenvy.ide.extension.gradle.shared.GradleAttributes;
import com.codenvy.ide.gradle.DistributionType;
import com.codenvy.ide.gradle.DistributionVersion;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringMapListUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode.CREATE;
import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode.CREATE_MODULE;
import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardMode.UPDATE;
import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static com.codenvy.ide.api.projecttype.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DEFAULT_SOURCE_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DEFAULT_TEST_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.DISTRIBUTION_TYPE;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GENERATION_STRATEGY_OPTION;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.GRADLE_ID;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.SOURCE_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.TEST_FOLDER;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.WRAPPED_GENERATION_STRATEGY;
import static com.codenvy.ide.extension.gradle.shared.GradleAttributes.WRAPPER_VERSION_OPTION;
import static com.codenvy.ide.gradle.DistributionType.BUNDLED;

/**
 * Gradle new project wizard presenter.
 *
 * @author Vladyslav Zhukovskii
 */
@Singleton
public class GradlePagePresenter extends AbstractWizardPage<ImportProject> implements GradlePageView.ActionDelegate {

    private GradlePageView       view;
    private DtoFactory           dtoFactory;
    private ProjectServiceClient projectServiceClient;

    /** Create instance of {@link com.codenvy.ide.extension.gradle.client.wizard.GradlePagePresenter}. */
    @Inject
    public GradlePagePresenter(GradlePageView view,
                               DtoFactory dtoFactory,
                               ProjectServiceClient projectServiceClient) {
        super();
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.projectServiceClient = projectServiceClient;
        view.setDelegate(this);
    }

    /** {@inheritDoc} */
    @Override
    public void init(ImportProject dataObject) {
        super.init(dataObject);
        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            // set default values
            setAttribute(SOURCE_FOLDER, DEFAULT_SOURCE_FOLDER);
            setAttribute(TEST_FOLDER, DEFAULT_TEST_FOLDER);
            setAttribute(DISTRIBUTION_TYPE, BUNDLED.toString());
        } else if (CREATE_MODULE == wizardMode) {
            estimateAndSetAttributes();
        }
    }

    /** Perform project estimation. Read attributes from project via {@link com.codenvy.ide.extension.gradle.server.project.type
     * .GradleValueProviderFactory} */
    private void estimateAndSetAttributes() {
        projectServiceClient.estimateProject(
                context.get(PROJECT_PATH_KEY), GRADLE_ID,
                new AsyncRequestCallback<Map<String, List<String>>>(new StringMapListUnmarshaller()) {
                    @Override
                    protected void onSuccess(Map<String, List<String>> result) {
                        List<String> artifactIdValues = result.get(DISTRIBUTION_TYPE);
                        if (artifactIdValues != null && !artifactIdValues.isEmpty()) {
                            setAttribute(DISTRIBUTION_TYPE, artifactIdValues.get(0));
                        }

                        updateDelegate.updateControls();
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        Log.error(GradlePagePresenter.class, exception);
                    }
                }
                                            );
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompleted() {
        return !getAttribute(DISTRIBUTION_TYPE).isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        view.reset();
        ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        view.setDistributionConfigurationSectionVisibility(!(UPDATE == wizardMode || CREATE_MODULE == wizardMode));
    }

    /** {@inheritDoc} */
    @Override
    public void onGradleTypeChanged(@Nonnull DistributionType distributionType) {
        setAttribute(DISTRIBUTION_TYPE, distributionType.toString());
        if (distributionType == BUNDLED) {
            dataObject.getProject().setGeneratorDescription(dtoFactory.createDto(GeneratorDescription.class));
        }

        updateDelegate.updateControls();
    }

    /** {@inheritDoc} */
    @Override
    public void onGradleVersionChanged(@Nullable DistributionVersion distributionVersion) {
        GeneratorDescription generatorDescription = dtoFactory.createDto(GeneratorDescription.class);
        if (distributionVersion != null) {
            HashMap<String, String> options = new HashMap<>();
            options.put(GENERATION_STRATEGY_OPTION, WRAPPED_GENERATION_STRATEGY);
            options.put(WRAPPER_VERSION_OPTION, distributionVersion.getVersion());
            generatorDescription.setOptions(options);

        } else {
            generatorDescription.setOptions(null);
        }

        dataObject.getProject().setGeneratorDescription(generatorDescription);
        updateDelegate.updateControls();
    }

    /** Sets single value of attribute of data-object. */
    private void setAttribute(String attrId, String value) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(attrId, Arrays.asList(value));
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
}
