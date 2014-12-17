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

import com.codenvy.ide.api.mvp.View;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.extension.maven.client.wizard.MavenArchetype;
import com.google.inject.ImplementedBy;

/**
 * @author Evgen Vidolob
 */
@ImplementedBy(CreateMavenModuleViewImpl.class)
public interface CreateMavenModuleView extends View<CreateMavenModuleView.ActionDelegate> {

    MavenArchetype getArchetype();

    void setArchetypes(Array<MavenArchetype> archetypes);

    void enableArchetypes(boolean enabled);

    boolean isGenerateFromArchetypeSelected();

    void setParentArtifactId(String artifactId);

    void setGroupId(String groupId);

    void setVersion(String version);

    void setCreateButtonEnabled(boolean enabled);

    void setNameError(boolean hasError);

    void setArtifactIdError(boolean hasError);

    void reset();

    String getPackaging();

    String getGroupId();

    String getVersion();

    void setPackagingVisibility(boolean visible);

    void close();

    void showButtonLoader(boolean showLoader);

    void clearArchetypes();

    public interface ActionDelegate{

        void onClose();

        void create();

        void projectNameChanged(String name);

        void artifactIdChanged(String artifactId);

        void generateFromArchetypeChanged(boolean isGenerateFromArchetype);
    }

    void show();
}
