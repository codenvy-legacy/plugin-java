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
package com.codenvy.ide.ext.java.client.editor;

import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.parts.PartPresenter;
import com.codenvy.ide.api.parts.PropertyListener;
import com.codenvy.ide.api.projecttree.generic.FileNode;
import com.codenvy.ide.ext.java.client.projecttree.PackageNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class FileSaveWatcher {

    @Inject
    private JavaParserWorker worker;

    public void editorOpened(final EditorPartPresenter editor) {
        final PropertyListener propertyListener = new PropertyListener() {
            @Override
            public void propertyChanged(PartPresenter source, int propId) {
                if (propId == EditorPartPresenter.PROP_DIRTY) {
                    if (!editor.isDirty()) {
                        FileNode file = editor.getEditorInput().getFile();
                        // file's parent always should be PackageNode
                        String fqn = ((PackageNode)file.getParent()).getQualifiedName() + '.' + file.getName().substring(0, file.getName().indexOf('.'));
                        worker.removeFanFromCache(fqn);
                    }
                }
            }
        };
        editor.addPropertyListener(propertyListener);
    }
}
