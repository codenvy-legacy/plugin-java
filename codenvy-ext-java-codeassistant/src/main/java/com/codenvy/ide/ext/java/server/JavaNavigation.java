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

package com.codenvy.ide.ext.java.server;

import com.codenvy.dto.server.DtoFactory;
import com.codenvy.ide.ext.java.server.internal.core.JarEntryDirectory;
import com.codenvy.ide.ext.java.server.internal.core.JarEntryFile;
import com.codenvy.ide.ext.java.server.internal.core.JarEntryResource;
import com.codenvy.ide.ext.java.server.internal.core.JarPackageFragmentRoot;
import com.codenvy.ide.ext.java.server.internal.core.JavaProject;
import com.codenvy.ide.ext.java.server.javadoc.JavaElementLabels;
import com.codenvy.ide.ext.java.shared.Jar;
import com.codenvy.ide.ext.java.shared.JarEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.inject.Singleton;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaNavigation {
    private static final ArrayList<JarEntry> NO_ENTRIES    = new ArrayList<>(1);
    private              Gson                gson          = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    private              boolean             fFoldPackages = true;

    /**
     * Utility method to concatenate two arrays.
     *
     * @param a1
     *         the first array
     * @param a2
     *         the second array
     * @return the concatenated array
     */
    protected static Object[] concatenate(Object[] a1, Object[] a2) {
        int a1Len = a1.length;
        int a2Len = a2.length;
        if (a1Len == 0) return a2;
        if (a2Len == 0) return a1;
        Object[] res = new Object[a1Len + a2Len];
        System.arraycopy(a1, 0, res, 0, a1Len);
        System.arraycopy(a2, 0, res, a1Len, a2Len);
        return res;
    }

    private static IPackageFragment getFolded(IJavaElement[] children, IPackageFragment pack) throws JavaModelException {
        while (isEmpty(pack)) {
            IPackageFragment collapsed = findSinglePackageChild(pack, children);
            if (collapsed == null) {
                return pack;
            }
            pack = collapsed;
        }
        return pack;
    }

    private static boolean isEmpty(IPackageFragment fragment) throws JavaModelException {
        return !fragment.containsJavaResources() && fragment.getNonJavaResources().length == 0;
    }

    private static IPackageFragment findSinglePackageChild(IPackageFragment fragment, IJavaElement[] children) {
        String prefix = fragment.getElementName() + '.';
        int prefixLen = prefix.length();
        IPackageFragment found = null;
        for (int i = 0; i < children.length; i++) {
            IJavaElement element = children[i];
            String name = element.getElementName();
            if (name.startsWith(prefix) && name.length() > prefixLen && name.indexOf('.', prefixLen) == -1) {
                if (found == null) {
                    found = (IPackageFragment)element;
                } else {
                    return null;
                }
            }
        }
        return found;
    }

    public String findDeclaration(JavaProject project, String bindingKey) throws JavaModelException {
        IJavaElement originalElement = project.findElement(bindingKey, null);
        IJavaElement element = originalElement;
        while (element != null) {
            if (element instanceof ICompilationUnit) {
                ICompilationUnit unit = ((ICompilationUnit)element).getPrimary();
                return compilationUnitNavigation(unit, originalElement);
            }

            if (element instanceof IClassFile) {
                return classFileNavigation((IClassFile)element, originalElement);
            }
            element = element.getParent();
        }
        return null;
    }

    public List<Jar> getProjectDepandecyJars(JavaProject project) throws JavaModelException {
        List<Jar> jars = new ArrayList<>();
        for (IPackageFragmentRoot fragmentRoot : project.getAllPackageFragmentRoots()) {
            if (fragmentRoot instanceof JarPackageFragmentRoot) {
                Jar jar = DtoFactory.getInstance().createDto(Jar.class);
                jar.setId(fragmentRoot.hashCode());
                jar.setName(fragmentRoot.getElementName());
                jars.add(jar);
            }
        }

        return jars;
    }

    public List<JarEntry> getPackageFragmentRootContent(JavaProject project, int hash) throws JavaModelException {
        IPackageFragmentRoot packageFragmentRoot = getPackageFragmentRoot(project, hash);

        if (packageFragmentRoot == null) {
            return NO_ENTRIES;
        }

        Object[] rootContent = getPackageFragmentRootContent(packageFragmentRoot);

        return convertToJarEntry(rootContent);
    }

    private IPackageFragmentRoot getPackageFragmentRoot(JavaProject project, int hash) throws JavaModelException {
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        IPackageFragmentRoot packageFragmentRoot = null;
        for (IPackageFragmentRoot root : roots) {
            if (root.hashCode() == hash) {
                packageFragmentRoot = root;
                break;
            }
        }
        return packageFragmentRoot;
    }

    private List<JarEntry> convertToJarEntry(Object[] rootContent) {
        List<JarEntry> result = new ArrayList<>();
        for (Object o : rootContent) {
            if (o instanceof IPackageFragment) {
                JarEntry entry = DtoFactory.getInstance().createDto(JarEntry.class);
                IPackageFragment packageFragment = (IPackageFragment)o;
                entry.setName(getSpecificText((IJavaElement)o));
                entry.setPath(packageFragment.getElementName());
                entry.setType(JarEntry.JarEntryType.PACKAGE);
                result.add(entry);
            }

            if (o instanceof IClassFile) {
                JarEntry entry = DtoFactory.getInstance().createDto(JarEntry.class);
                IClassFile classFile = (IClassFile)o;
                entry.setType(JarEntry.JarEntryType.CLASS_FILE);
                entry.setName(classFile.getElementName());
                entry.setPath(classFile.getType().getFullyQualifiedName());
                result.add(entry);
            }

            if (o instanceof JarEntryResource) {
                result.add(getJarEntryResource((JarEntryResource)o));
            }
        }
        return result;
    }

    private String getSpecificText(IJavaElement element) {
        if (element instanceof IPackageFragment) {
            IPackageFragment fragment = (IPackageFragment) element;
            Object parent= getHierarchicalPackageParent(fragment);
            if (parent instanceof IPackageFragment) {
                return getNameDelta((IPackageFragment) parent, fragment);
            }
        }

        return JavaElementLabels.getElementLabel(element, 0);
    }

    private String getNameDelta(IPackageFragment parent, IPackageFragment fragment) {
        String prefix= parent.getElementName() + '.';
        String fullName= fragment.getElementName();
        if (fullName.startsWith(prefix)) {
            return fullName.substring(prefix.length());
        }
        return fullName;
    }

    public Object getHierarchicalPackageParent(IPackageFragment child) {
        String name= child.getElementName();
        IPackageFragmentRoot parent= (IPackageFragmentRoot) child.getParent();
        int index= name.lastIndexOf('.');
        if (index != -1) {
            String realParentName= name.substring(0, index);
            IPackageFragment element= parent.getPackageFragment(realParentName);
            if (element.exists()) {
                try {
                    if (fFoldPackages && isEmpty(element) && findSinglePackageChild(element, parent.getChildren()) != null) {
                        return getHierarchicalPackageParent(element);
                    }
                } catch (JavaModelException e) {
                    // ignore
                }
                return element;
            } /*else { // bug 65240
                IResource resource= element.getResource();
                if (resource != null) {
                    return resource;
                }
            }*/
        }
//        if (parent.getResource() instanceof IProject) {
//            return parent.getJavaProject();
//        }
        return parent;
    }

    private JarEntry getJarEntryResource(JarEntryResource resource) {
        JarEntry entry = DtoFactory.getInstance().createDto(JarEntry.class);
        if (resource instanceof JarEntryDirectory) {
            entry.setType(JarEntry.JarEntryType.FOLDER);
        }
        if (resource instanceof JarEntryFile) {
            entry.setType(JarEntry.JarEntryType.FILE);
        }
        entry.setName(resource.getName());
        entry.setPath(resource.getFullPath().toOSString());
        return entry;
    }

    protected Object[] getPackageFragmentRootContent(IPackageFragmentRoot root) throws JavaModelException {

        // hierarchical package mode
        ArrayList<Object> result = new ArrayList<>();
        getHierarchicalPackageChildren(root, null, result);
        Object[] nonJavaResources = root.getNonJavaResources();
        for (int i = 0; i < nonJavaResources.length; i++) {
            result.add(nonJavaResources[i]);
        }
        return result.toArray();
    }

    /* (non-Javadoc)
 * @see org.eclipse.jdt.ui.StandardJavaElementContentProvider#getPackageContent(org.eclipse.jdt.core.IPackageFragment)
 */
    protected Object[] getPackageContent(IPackageFragment fragment) throws JavaModelException {

        // hierarchical package mode
        ArrayList<Object> result = new ArrayList<Object>();

        getHierarchicalPackageChildren((IPackageFragmentRoot)fragment.getParent(), fragment, result);
        IClassFile[] classFiles = fragment.getClassFiles();
        List<IClassFile> filtered = new ArrayList<>();
        //filter inner classes
        for (IClassFile classFile : classFiles) {
            if(!classFile.getElementName().contains("$")){
                filtered.add(classFile);
            }
        }
        Object[] nonPackages = concatenate(filtered.toArray(), fragment.getNonJavaResources());
        if (result.isEmpty())
            return nonPackages;
        Collections.addAll(result, nonPackages);
        return result.toArray();
    }

    /**
     * Returns the hierarchical packages inside a given fragment or root.
     *
     * @param parent
     *         the parent package fragment root
     * @param fragment
     *         the package to get the children for or 'null' to get the children of the root
     * @param result
     *         Collection where the resulting elements are added
     * @throws JavaModelException
     *         if fetching the children fails
     */
    private void getHierarchicalPackageChildren(IPackageFragmentRoot parent, IPackageFragment fragment, Collection<Object> result)
            throws JavaModelException {
        IJavaElement[] children = parent.getChildren();
        String prefix = fragment != null ? fragment.getElementName() + '.' : ""; //$NON-NLS-1$
        int prefixLen = prefix.length();
        for (int i = 0; i < children.length; i++) {
            IPackageFragment curr = (IPackageFragment)children[i];
            String name = curr.getElementName();
            if (name.startsWith(prefix) && name.length() > prefixLen && name.indexOf('.', prefixLen) == -1) {
                if (fFoldPackages) {
                    curr = getFolded(children, curr);
                }
                result.add(curr);
            } else if (fragment == null && curr.isDefaultPackage()) {
                IJavaElement[] currChildren = curr.getChildren();
                if(currChildren != null && currChildren.length >= 1) {
                    result.add(curr);
                }
            }
        }
    }

    private String classFileNavigation(IClassFile classFile, IJavaElement element) throws JavaModelException {
        if (classFile.getSourceRange() != null) {
            JsonObject result = new JsonObject();
            result.addProperty("source", classFile.getSource());
            if (element instanceof ISourceReference) {
                ISourceRange nameRange = ((ISourceReference)element).getNameRange();
                JsonObject name = new JsonObject();
                name.addProperty("offset", nameRange.getOffset());
                name.addProperty("length", nameRange.getLength());
                result.add("nameRange", name);
            }

            return gson.toJson(result);
        }
        return null;
    }

    private String compilationUnitNavigation(ICompilationUnit unit, IJavaElement element) {
        return null;
    }

    private Object[] findJarDirectoryChildren(JarEntryDirectory directory, String path){
        String directoryPath = directory.getFullPath().toOSString();
        if(directoryPath.equals(path)){
            return directory.getChildren();
        }
        if(path.startsWith(directoryPath)){
            for (IJarEntryResource resource : directory.getChildren()) {
                String childrenPath = resource.getFullPath().toOSString();
                if(childrenPath.equals(path)){
                    return resource.getChildren();
                }
                if (path.startsWith(childrenPath) && resource instanceof JarEntryDirectory){
                    findJarDirectoryChildren((JarEntryDirectory)resource, path);
                }
            }
        }
        return null;
    }

    public List<JarEntry> getChildren(JavaProject project, int rootId, String path) throws JavaModelException {
        IPackageFragmentRoot root = getPackageFragmentRoot(project, rootId);
        if (root == null) {
            return NO_ENTRIES;
        }

        if (path.startsWith("/")) {
            // jar file and folders
            Object[] resources = root.getNonJavaResources();
            for (Object resource : resources) {
                if (resource instanceof JarEntryDirectory) {
                    JarEntryDirectory directory = (JarEntryDirectory)resource;
                    Object[] children = findJarDirectoryChildren(directory, path);
                    if(children != null) {
                        return convertToJarEntry(children);
                    }
                }
            }

        } else {
            // packages and class files
            IPackageFragment fragment = root.getPackageFragment(path);
            if (fragment == null) {
                return NO_ENTRIES;
            }
            return convertToJarEntry(getPackageContent(fragment));
        }
        return NO_ENTRIES;
    }
}
