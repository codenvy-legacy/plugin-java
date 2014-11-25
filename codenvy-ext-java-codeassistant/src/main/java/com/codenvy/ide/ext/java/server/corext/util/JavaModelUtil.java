package com.codenvy.ide.ext.java.server.corext.util;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Evgen Vidolob
 */
public class JavaModelUtil {
    /**
     * The name of the package-info.java file.
     * @since 3.8
     */
    public static final String PACKAGE_INFO_JAVA= "package-info.java"; //$NON-NLS-1$

    /**
     * The name of the package-info.class file.
     * @since 3.9
     */
    public static final String PACKAGE_INFO_CLASS= "package-info.class"; //$NON-NLS-1$

    /**
     * The name of the package.html file.
     * @since 3.9
     */
    public static final String PACKAGE_HTML= "package.html"; //$NON-NLS-1$

    /**
     * @param type the type to test
     * @return <code>true</code> iff the type is an interface or an annotation
     * @throws org.eclipse.jdt.core.JavaModelException thrown when the field can not be accessed
     */
    public static boolean isInterfaceOrAnnotation(IType type) throws JavaModelException {
        return type.isInterface();
    }

    /**
     * Returns the package fragment root of <code>IJavaElement</code>. If the given
     * element is already a package fragment root, the element itself is returned.
     * @param element the element
     * @return the package fragment root of the element or <code>null</code>
     */
    public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
        return (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    }

    public static boolean isPolymorphicSignature(IMethod method) {
        return method.getAnnotation("java.lang.invoke.MethodHandle$PolymorphicSignature").exists(); //$NON-NLS-1$
    }
}
