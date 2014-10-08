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
package com.codenvy.ide.extension.maven.client.wizard;

import com.codenvy.api.project.shared.dto.ProjectDescriptor;
import com.codenvy.ide.api.projecttype.wizard.ProjectWizard;
import com.codenvy.ide.api.wizard.AbstractWizardPage;
import com.codenvy.ide.api.wizard.Wizard;
import com.codenvy.ide.collections.Jso;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.rest.AsyncRequestCallback;
import com.codenvy.ide.rest.StringUnmarshaller;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenPagePresenter extends AbstractWizardPage implements MavenPageView.ActionDelegate {

    protected MavenPageView             view;
    protected EventBus                  eventBus;
    private   MavenPomServiceClient     pomReaderClient;
    private   Map<String, List<String>> attributes;

    @Inject
    public MavenPagePresenter(MavenPageView view, EventBus eventBus, MavenPomServiceClient pomReaderClient) {
        super("Maven project settings", null);
        this.view = view;
        this.eventBus = eventBus;
        this.pomReaderClient = pomReaderClient;
        view.setDelegate(this);
    }

    @Nullable
    @Override
    public String getNotice() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        boolean isArtifactIdCompleted = !view.getArtifactId().equals("");
        boolean isGroupIdCompleted = !view.getGroupId().equals("");
        boolean isVersionFieldCompleted = !view.getVersion().equals("");
        boolean isCompleted = isArtifactIdCompleted && isGroupIdCompleted && isVersionFieldCompleted;

        view.showArtifactIdMissingIndicator(!isArtifactIdCompleted);
        view.showGroupIdMissingIndicator(!isGroupIdCompleted);
        view.showVersionMissingIndicator(!isVersionFieldCompleted);
        return isCompleted;
    }

    @Override
    public void focusComponent() {
    }

    @Override
    public void removeOptions() {
    }

    @Override
    public void setUpdateDelegate(@Nonnull Wizard.UpdateDelegate delegate) {
        super.setUpdateDelegate(delegate);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
        view.reset();

        // setting project name from the main wizard page
        String projectName = wizardContext.getData(ProjectWizard.PROJECT_NAME);
        if (projectName != null) {
            view.setArtifactId(projectName);
            view.setGroupId(projectName);
            scheduleTextChanges();
        }

        ProjectDescriptor projectUpdate = wizardContext.getData(ProjectWizard.PROJECT_FOR_UPDATE);
        ProjectDescriptor project = wizardContext.getData(ProjectWizard.PROJECT);
        attributes = project.getAttributes();
        attributes.put(MavenAttributes.SOURCE_FOLDER, Arrays.asList("src/main/java"));
        attributes.put(MavenAttributes.TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        project.setBuilder("maven");
        if (projectUpdate != null) {
            List<String> artifactIdAttr = attributes.get(MavenAttributes.ARTIFACT_ID);
            if (artifactIdAttr != null) {
                view.setArtifactId(artifactIdAttr.get(0));
                view.setGroupId(attributes.get(MavenAttributes.GROUP_ID).get(0));
                view.setVersion(attributes.get(MavenAttributes.VERSION).get(0));
                view.setPackaging(attributes.get(MavenAttributes.PACKAGING).get(0));
                scheduleTextChanges();
            } else {
                pomReaderClient.readPomAttributes(project.getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
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
        attributes.put(MavenAttributes.PACKAGING, Arrays.asList(view.getPackaging()));
        delegate.updateControls();
    }

    @Override
    public void setPackaging(String packaging) {
        ProjectDescriptor project = wizardContext.getData(ProjectWizard.PROJECT);
        attributes.put(MavenAttributes.PACKAGING, Arrays.asList(packaging));
        if ("war".equals(packaging)) {
            project.setRunner("JavaWeb");
        }
        if ("jar".equals(packaging)) {
            project.setRunner("JavaStandalone");
        }
        if ("pom".equals(packaging)) {
            attributes.remove(MavenAttributes.SOURCE_FOLDER);
            attributes.remove(MavenAttributes.TEST_SOURCE_FOLDER);
        } else {
            attributes.put(MavenAttributes.SOURCE_FOLDER, Arrays.asList("src/main/java"));
            attributes.put(MavenAttributes.TEST_SOURCE_FOLDER, Arrays.asList("src/test/java"));
        }
    }
}
