/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.codenvy.ide.ext.java.server.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.WeakHashSet;
import org.eclipse.jdt.internal.core.util.WeakHashSetOfCharArray;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Evgen Vidolob
 */
public class JavaModelManager {

    public static boolean       ZIP_ACCESS_VERBOSE             = false;
    public static boolean VERBOSE = false;
    /**
     * A set of java.io.Files used as a cache of external jars that
     * are known to be existing.
     * Note this cache is kept for the whole session.
     */
    public static HashSet<File> existingExternalFiles          = new HashSet<>();
    /**
     * A set of external files ({@link #existingExternalFiles}) which have
     * been confirmed as file (i.e. which returns true to {@link java.io.File#isFile()}.
     * Note this cache is kept for the whole session.
     */
    public static HashSet<File> existingExternalConfirmedFiles = new HashSet<>();
//    /**
//     * The singleton manager
//     */
//    private static JavaModelManager MANAGER                        = new JavaModelManager();
    /**
     * List of IPath of jars that are known to be invalid - such as not being a valid/known format
     */
    private Set<IPath> invalidArchives;

    /**
     * Set of elements which are out of sync with their buffers.
     */
    protected HashSet elementsOutOfSynchWithBuffers = new HashSet(11);

    /**
     * A cache of opened zip files per thread.
     * (for a given thread, the object value is a HashMap from IPath to java.io.ZipFile)
     */
    private ThreadLocal<ZipCache> zipFiles = new ThreadLocal<>();

    /*
 * Temporary cache of newly opened elements
 */
    private ThreadLocal            temporaryCache   = new ThreadLocal();

    /* whether an AbortCompilationUnit should be thrown when the source of a compilation unit cannot be retrieved */
    public ThreadLocal abortOnMissingSource = new ThreadLocal();

    /*
     * Pools of symbols used in the Java model.
     * Used as a replacement for String#intern() that could prevent garbage collection of strings on some VMs.
     */
    private WeakHashSet            stringSymbols    = new WeakHashSet(5);
    private WeakHashSetOfCharArray charArraySymbols = new WeakHashSetOfCharArray(5);
    /**
     * Infos cache.
     */
    private JavaModelCache cache;

    /**
     * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy handle) to PerWorkingCopyInfo.
     * NOTE: this object itself is used as a lock to synchronize creation/removal of per working copy infos
     */
    protected Map perWorkingCopyInfos = new HashMap(5);

//    public static JavaModelManager getJavaModelManager() {
//        return MANAGER;
//    }

    public JavaModelManager() {
        // initialize Java model cache
        this.cache = new JavaModelCache();
    }

    public synchronized char[] intern(char[] array) {
        return this.charArraySymbols.add(array);
    }

    public synchronized String intern(String s) {
        // make sure to copy the string (so that it doesn't hold on the underlying char[] that might be much bigger than necessary)
        return (String)this.stringSymbols.add(new String(s));

        // Note1: String#intern() cannot be used as on some VMs this prevents the string from being garbage collected
        // Note 2: Instead of using a WeakHashset, one could use a WeakHashMap with the following implementation
        // 			   This would costs more per entry (one Entry object and one WeakReference more))

		/*
		WeakReference reference = (WeakReference) this.symbols.get(s);
		String existing;
		if (reference != null && (existing = (String) reference.get()) != null)
			return existing;
		this.symbols.put(s, new WeakReference(s));
		return s;
		*/
    }

    /**
     * Returns the set of elements which are out of synch with their buffers.
     */
    protected HashSet getElementsOutOfSynchWithBuffers() {
        return this.elementsOutOfSynchWithBuffers;
    }

    /**
     * Helper method - returns the targeted item (IResource if internal or java.io.File if external),
     * or null if unbound
     * Internal items must be referred to using container relative paths.
     */
    public static Object getTarget(IPath path, boolean checkResourceExistence) {
        File externalFile = new File(path.toOSString());
        if (!checkResourceExistence) {
            return externalFile;
        } else if (existingExternalFilesContains(externalFile)) {
            return externalFile;
        } else {
            if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                System.out.println("(" + Thread.currentThread() + ") [JavaModel.getTarget...)] Checking existence of " +
                                   path.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (externalFile.isFile()) { // isFile() checks for existence (it returns false if a directory)
                // cache external file
                existingExternalFilesAdd(externalFile);
                return externalFile;
            } else {
                if (externalFile.exists()) {
                    existingExternalFilesAdd(externalFile);
                    return externalFile;
                }
            }
        }
        return null;
    }

    private synchronized static void existingExternalFilesAdd(File externalFile) {
        existingExternalFiles.add(externalFile);
    }

    private synchronized static boolean existingExternalFilesContains(File externalFile) {
        return existingExternalFiles.contains(externalFile);
    }

    /**
     * Flushes the cache of external files known to be existing.
     */
    public static void flushExternalFileCache() {
        existingExternalFiles = new HashSet<>();
        existingExternalConfirmedFiles = new HashSet<>();
    }

    /**
     * Helper method - returns whether an object is afile (i.e. which returns true to {@link java.io.File#isFile()}.
     */
    public static boolean isFile(Object target) {
        return getFile(target) != null;
    }

    /**
     * Helper method - returns the file item (i.e. which returns true to {@link java.io.File#isFile()},
     * or null if unbound
     */
    public static synchronized File getFile(Object target) {
        if (existingExternalConfirmedFiles.contains(target))
            return (File)target;
        if (target instanceof File) {
            File f = (File)target;
            if (f.isFile()) {
                existingExternalConfirmedFiles.add(f);
                return f;
            }
        }

        return null;
    }

    /**
     * Returns the open ZipFile at the given path. If the ZipFile
     * does not yet exist, it is created, opened, and added to the cache
     * of open ZipFiles.
     * <p/>
     * The path must be a file system path if representing an external
     * zip/jar, or it must be an absolute workspace relative path if
     * representing a zip/jar inside the workspace.
     *
     * @throws org.eclipse.core.runtime.CoreException
     *         If unable to create/open the ZipFile
     */
    public ZipFile getZipFile(IPath path) throws CoreException {

        if (isInvalidArchive(path))
            throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.status_IOException, new ZipException()));

        ZipCache zipCache;
        ZipFile zipFile;
        if ((zipCache = this.zipFiles.get()) != null
            && (zipFile = zipCache.getCache(path)) != null) {
            return zipFile;
        }
        File localFile = null;
//        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//        IResource file = root.findMember(path);
//        if (file != null) {
//            // internal resource
//            URI location;
//            if (file.getType() != IResource.FILE || (location = file.getLocationURI()) == null) {
//                throw new CoreException(
//                        new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.bind(Messages.file_notFound, path.toString()), null));
//            }
//            localFile = Util.toLocalFile(location, null*//*no progress availaible*//*);
//            if (localFile == null)
//                throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.bind(Messages.file_notFound,
// path.toString()), null));
//        } else {
//            // external resource -> it is ok to use toFile()
        localFile = path.toFile();
//        }
        if (!localFile.exists()) {
            throw new CoreException(
                    new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.bind(Messages.file_notFound, path.toString()), null));
        }

        try {
            if (ZIP_ACCESS_VERBOSE) {
                System.out.println("(" + Thread.currentThread() + ") [JavaModelManager.getZipFile(IPath)] Creating ZipFile on " +
                                   localFile); //$NON-NLS-1$ //$NON-NLS-2$
            }
            zipFile = new ZipFile(localFile);
            if (zipCache != null) {
                zipCache.setCache(path, zipFile);
            }
            return zipFile;
        } catch (IOException e) {
            addInvalidArchive(path);
            throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.status_IOException, e));
        }
    }

    public boolean isInvalidArchive(IPath path) {
        return this.invalidArchives != null && this.invalidArchives.contains(path);
    }

    public void removeFromInvalidArchiveCache(IPath path) {
        if (this.invalidArchives != null) {
            this.invalidArchives.remove(path);
        }
    }

    public void addInvalidArchive(IPath path) {
        // unlikely to be null
        if (this.invalidArchives == null) {
            this.invalidArchives = Collections.synchronizedSet(new HashSet<IPath>());
        }
        if (this.invalidArchives != null) {
            this.invalidArchives.add(path);
        }
    }

    public ICompilationUnit[] getWorkingCopies(DefaultWorkingCopyOwner primary, boolean b) {
        return null;
    }

    public ICompilationUnit[] getWorkingCopies(WorkingCopyOwner workingCopyOwner, boolean b) {
        return null;
    }

    /**
     *  Returns the info for the element.
     */
    public synchronized Object getInfo(IJavaElement element) {
        HashMap tempCache = (HashMap)this.temporaryCache.get();
        if (tempCache != null) {
            Object result = tempCache.get(element);
            if (result != null) {
                return result;
            }
        }
        return this.cache.getInfo(element);
    }

    /**
     *  Returns the info for this element without
     *  disturbing the cache ordering.
     */
    protected synchronized Object peekAtInfo(IJavaElement element) {
        HashMap tempCache = (HashMap)this.temporaryCache.get();
        if (tempCache != null) {
            Object result = tempCache.get(element);
            if (result != null) {
                return result;
            }
        }
        return this.cache.peekAtInfo(element);
    }

    /*
	 * Removes all cached info for the given element (including all children)
	 * from the cache.
	 * Returns the info for the given element, or null if it was closed.
	 */
    public synchronized Object removeInfoAndChildren(JavaElement element) throws JavaModelException {
        Object info = this.cache.peekAtInfo(element);
        if (info != null) {
            boolean wasVerbose = false;
            try {
                if (org.eclipse.jdt.internal.core.JavaModelCache.VERBOSE) {
                    String elementType;
                    switch (element.getElementType()) {
                        case IJavaElement.JAVA_PROJECT:
                            elementType = "project"; //$NON-NLS-1$
                            break;
                        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                            elementType = "root"; //$NON-NLS-1$
                            break;
                        case IJavaElement.PACKAGE_FRAGMENT:
                            elementType = "package"; //$NON-NLS-1$
                            break;
                        case IJavaElement.CLASS_FILE:
                            elementType = "class file"; //$NON-NLS-1$
                            break;
                        case IJavaElement.COMPILATION_UNIT:
                            elementType = "compilation unit"; //$NON-NLS-1$
                            break;
                        default:
                            elementType = "element"; //$NON-NLS-1$
                    }
                    System.out.println(Thread.currentThread() + " CLOSING "+ elementType + " " + element.toStringWithAncestors());  //$NON-NLS-1$//$NON-NLS-2$
                    wasVerbose = true;
                    JavaModelCache.VERBOSE = false;
                }
                element.closing(info);
                if (element instanceof IParent) {
                    closeChildren(info);
                }
                this.cache.removeInfo(element);
                if (wasVerbose) {
                    System.out.println(this.cache.toStringFillingRation("-> ")); //$NON-NLS-1$
                }
            } finally {
                JavaModelCache.VERBOSE = wasVerbose;
            }
            return info;
        }
        return null;
    }

    /*
 * Returns whether there is a temporary cache for the current thread.
 */
    public boolean hasTemporaryCache() {
        return this.temporaryCache.get() != null;
    }

    /**
     * Returns the temporary cache for newly opened elements for the current thread.
     * Creates it if not already created.
     */
    public HashMap getTemporaryCache() {
        HashMap result = (HashMap)this.temporaryCache.get();
        if (result == null) {
            result = new HashMap();
            this.temporaryCache.set(result);
        }
        return result;
    }

    /*
	 * Puts the infos in the given map (keys are IJavaElements and values are JavaElementInfos)
	 * in the Java model cache in an atomic way if the info is not already present in the cache.
	 * If the info is already present in the cache, it depends upon the forceAdd parameter.
	 * If forceAdd is false it just returns the existing info and if true, this element and it's children are closed and then
	 * this particular info is added to the cache.
	 */
    protected synchronized Object putInfos(IJavaElement openedElement, Object newInfo, boolean forceAdd, Map newElements) {
        // remove existing children as the are replaced with the new children contained in newElements
        Object existingInfo = this.cache.peekAtInfo(openedElement);
        if (existingInfo != null && !forceAdd) {
            // If forceAdd is false, then it could mean that the particular element
            // wasn't in cache at that point of time, but would have got added through
            // another thread. In that case, removing the children could remove it's own
            // children. So, we should not remove the children but return the already existing
            // info.
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=372687
            return existingInfo;
        }
        if (openedElement instanceof IParent) {
            closeChildren(existingInfo);
        }

        // Need to put any JarPackageFragmentRoot in first.
        // This is due to the way the LRU cache flushes entries.
        // When a JarPackageFragment is flushed from the LRU cache, the entire
        // jar is flushed by removing the JarPackageFragmentRoot and all of its
        // children (see ElementCache.close()). If we flush the JarPackageFragment
        // when its JarPackageFragmentRoot is not in the cache and the root is about to be
        // added (during the 'while' loop), we will end up in an inconsistent state.
        // Subsequent resolution against package in the jar would fail as a result.
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=102422
        // (theodora)
        for(Iterator it = newElements.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry)it.next();
            IJavaElement element = (IJavaElement)entry.getKey();
            if (element instanceof JarPackageFragmentRoot) {
                Object info = entry.getValue();
                it.remove();
                this.cache.putInfo(element, info);
            }
        }

        Iterator iterator = newElements.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            this.cache.putInfo((IJavaElement) entry.getKey(), entry.getValue());
        }
        return newInfo;
    }
    private void closeChildren(Object info) {
        if (info instanceof JavaElementInfo) {
            IJavaElement[] children = ((JavaElementInfo)info).getChildren();
            for (int i = 0, size = children.length; i < size; ++i) {
                JavaElement child = (JavaElement) children[i];
                try {
                    child.close();
                } catch (JavaModelException e) {
                    // ignore
                }
            }
        }
    }
    /*
 * Returns the per-working copy info for the given working copy at the given path.
 * If it doesn't exist and if create, add a new per-working copy info with the given problem requestor.
 * If recordUsage, increment the per-working copy info's use count.
 * Returns null if it doesn't exist and not create.
 */
    public PerWorkingCopyInfo getPerWorkingCopyInfo(CompilationUnit workingCopy,boolean create, boolean recordUsage, IProblemRequestor problemRequestor) {
        synchronized(this.perWorkingCopyInfos) { // use the perWorkingCopyInfo collection as its own lock
            WorkingCopyOwner owner = workingCopy.owner;
            Map workingCopyToInfos = (Map)this.perWorkingCopyInfos.get(owner);
            if (workingCopyToInfos == null && create) {
                workingCopyToInfos = new HashMap();
                this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
            }

            PerWorkingCopyInfo info = workingCopyToInfos == null ? null : (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
            if (info == null && create) {
                info= new PerWorkingCopyInfo(workingCopy, problemRequestor);
                workingCopyToInfos.put(workingCopy, info);
            }
            if (info != null && recordUsage) info.useCount++;
            return info;
        }
    }

    /*
 * Resets the temporary cache for newly created elements to null.
 */
    public void resetTemporaryCache() {
        this.temporaryCache.set(null);
    }

    public synchronized String cacheToString(String prefix) {
        return this.cache.toStringFillingRation(prefix);
    }

    public void closeZipFile(ZipFile zipFile) {
        if (zipFile == null) return;
        if (this.zipFiles.get() != null) {
            return; // zip file will be closed by call to flushZipFiles
        }
        try {
            if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                System.out.println("(" + Thread.currentThread() + ") [JavaModelManager.closeZipFile(ZipFile)] Closing ZipFile on " +zipFile.getName()); //$NON-NLS-1$	//$NON-NLS-2$
            }
            zipFile.close();
        } catch (IOException e) {
            // problem occured closing zip file: cannot do much more
        }
    }

    /**
     * Define a zip cache object.
     */
    static class ZipCache {
        Object owner;
        private Map<IPath, ZipFile> map;

        ZipCache(Object owner) {
            this.map = new HashMap<>();
            this.owner = owner;
        }

        public void flush() {
            Thread currentThread = Thread.currentThread();
            for (ZipFile zipFile : this.map.values()) {
                try {
                    if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
                        System.out.println("(" + currentThread + ") [JavaModelManager.flushZipFiles()] Closing ZipFile on " +
                                           zipFile.getName()); //$NON-NLS-1$//$NON-NLS-2$
                    }
                    zipFile.close();
                } catch (IOException e) {
                    // problem occured closing zip file: cannot do much more
                }
            }
        }

        public ZipFile getCache(IPath path) {
            return this.map.get(path);
        }

        public void setCache(IPath path, ZipFile zipFile) {
            this.map.put(path, zipFile);
        }
    }

    public static class PerWorkingCopyInfo implements IProblemRequestor {
        int useCount = 0;
        IProblemRequestor                             problemRequestor;
        CompilationUnit workingCopy;

        public PerWorkingCopyInfo(CompilationUnit workingCopy, IProblemRequestor problemRequestor) {
            this.workingCopy = workingCopy;
            this.problemRequestor = problemRequestor;
        }

        public void acceptProblem(IProblem problem) {
            IProblemRequestor requestor = getProblemRequestor();
            if (requestor == null) return;
            requestor.acceptProblem(problem);
        }

        public void beginReporting() {
            IProblemRequestor requestor = getProblemRequestor();
            if (requestor == null) return;
            requestor.beginReporting();
        }

        public void endReporting() {
            IProblemRequestor requestor = getProblemRequestor();
            if (requestor == null) return;
            requestor.endReporting();
        }

        public IProblemRequestor getProblemRequestor() {
            if (this.problemRequestor == null && this.workingCopy.owner != null) {
                return this.workingCopy.owner.getProblemRequestor(this.workingCopy);
            }
            return this.problemRequestor;
        }

        public ICompilationUnit getWorkingCopy() {
            return this.workingCopy;
        }

        public boolean isActive() {
            IProblemRequestor requestor = getProblemRequestor();
            return requestor != null && requestor.isActive();
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Info for "); //$NON-NLS-1$
            buffer.append(((JavaElement)this.workingCopy).toStringWithAncestors());
            buffer.append("\nUse count = "); //$NON-NLS-1$
            buffer.append(this.useCount);
            buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
            buffer.append(this.problemRequestor);
            if (this.problemRequestor == null) {
                IProblemRequestor requestor = getProblemRequestor();
                buffer.append("\nOwner problem requestor:\n  "); //$NON-NLS-1$
                buffer.append(requestor);
            }
            return buffer.toString();
        }
    }
}
