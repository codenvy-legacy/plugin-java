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

package com.codenvy.ide.ext.java.server.core.resources;

import com.codenvy.api.vfs.server.observation.VirtualFileEvent;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.io.File;

/**
 * @author Evgen Vidolob
 */
public class ResourceDeltaImpl implements IResourceDelta {

    private VirtualFileEvent event;
    protected static int KIND_MASK = 0xFF;

    public ResourceDeltaImpl(VirtualFileEvent event) {
        this.event = event;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public void accept(IResourceDeltaVisitor iResourceDeltaVisitor) throws CoreException {

    }

    @Override
    public void accept(IResourceDeltaVisitor iResourceDeltaVisitor, boolean b) throws CoreException {

    }

    @Override
    public void accept(IResourceDeltaVisitor iResourceDeltaVisitor, int i) throws CoreException {

    }

    @Override
    public org.eclipse.core.resources.IResourceDelta findMember(IPath iPath) {
        return null;
    }

    @Override
    public org.eclipse.core.resources.IResourceDelta[] getAffectedChildren() {
        return new org.eclipse.core.resources.IResourceDelta[0];
    }

    @Override
    public org.eclipse.core.resources.IResourceDelta[] getAffectedChildren(int i) {
        return new org.eclipse.core.resources.IResourceDelta[0];
    }

    @Override
    public org.eclipse.core.resources.IResourceDelta[] getAffectedChildren(int i, int i1) {
        return new org.eclipse.core.resources.IResourceDelta[0];
    }

    @Override
    public int getFlags() {
        return 0;
    }

    @Override
    public IPath getFullPath() {
        return null;
    }

    @Override
    public int getKind() {
        return 0;
    }

    @Override
    public IMarkerDelta[] getMarkerDeltas() {
        return new IMarkerDelta[0];
    }

    @Override
    public IPath getMovedFromPath() {
        return null;
    }

    @Override
    public IPath getMovedToPath() {
        return null;
    }

    @Override
    public IPath getProjectRelativePath() {
        return null;
    }

    @Override
    public IResource getResource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAdapter(Class aClass) {
        return null;
    }
}
