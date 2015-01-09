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
package com.codenvy.ide.ext.java.client.projecttree;

import com.codenvy.api.project.gwt.client.ProjectServiceClient;
import com.codenvy.api.project.shared.dto.ItemReference;
import com.codenvy.ide.api.editor.EditorAgent;
import com.codenvy.ide.api.event.RefreshProjectTreeEvent;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.projecttree.TreeNode;
import com.codenvy.ide.api.projecttree.generic.StorableNode;
import com.codenvy.ide.rest.DtoUnmarshallerFactory;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import javax.annotation.Nonnull;

import static com.codenvy.ide.ext.java.client.projecttree.JavaSourceFolderUtil.getSourceFolders;

/**
 * Node that represents a java package.
 *
 * @author Artem Zatsarynnyy
 */
public class PackageNode extends AbstractSourceContainerNode {

    @AssistedInject
    public PackageNode(@Assisted TreeNode<?> parent, @Assisted ItemReference data, @Assisted JavaTreeStructure treeStructure,
                       EventBus eventBus, EditorAgent editorAgent, ProjectServiceClient projectServiceClient,
                       DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(parent, data, treeStructure, eventBus, editorAgent, projectServiceClient, dtoUnmarshallerFactory);
        setDisplayIcon(iconRegistry.getIcon("java.package").getSVGImage());
    }

    @Nonnull
    @Override
    public String getId() {
        return getDisplayName().replace('.', '/');
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        if (((JavaTreeStructure)treeStructure).getSettings().isCompactEmptyPackages()) {
            final String parentPath = ((StorableNode)getParent()).getPath();
            return getPath().replaceFirst(parentPath + "/", "").replace('/', '.');
        }
        return super.getDisplayName();
    }

    /**
     * Returns the full-qualified name of the package.
     *
     * @return the full-qualified name, or an empty string for the default package
     */
    public String getQualifiedName() {
        for (String sourceFolder : getSourceFolders(this)) {
            if (getPath().startsWith(sourceFolder)) {
                return getPath().replaceFirst(sourceFolder, "").replace('/', '.');
            }
        }
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRenamable() {
        // Do not allow to rename package as simple folder.
        // This type of node needs to implement rename refactoring.
        return false;
    }

    @Override
    public void delete(final DeleteCallback callback) {
        super.delete(new DeleteCallback() {
            @Override
            public void onDeleted() {
                callback.onDeleted();

                // if parent contains one package only after deleting child node then parent may be compacted
                if (!isCompacted() && parent.getChildren().size() == 1 && parent.getChildren().get(0) instanceof PackageNode) {
                    eventBus.fireEvent(new RefreshProjectTreeEvent(parent.getParent()));
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private boolean isCompacted() {
        if (((JavaTreeStructure)treeStructure).getSettings().isCompactEmptyPackages()) {
            return getDisplayName().contains(".");
        }
        return false;
    }
}
