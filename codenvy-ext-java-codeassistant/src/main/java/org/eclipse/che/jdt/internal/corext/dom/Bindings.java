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
package org.eclipse.che.jdt.internal.corext.dom;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * JDT-UI-internal helper methods that deal with {@link org.eclipse.jdt.core.dom.IBinding}s:
 * <ul>
 * <li>additional operations on {@link org.eclipse.jdt.core.dom.IBinding}s and subtypes</li>
 * <li>finding corresponding elements in the type hierarchy</li>
 * <li>resolve bindings from a family of {@link org.eclipse.jdt.core.dom.ASTNode} types</li>
 * </ul>
 *
 * @see JDTUIHelperClasses
 */
public class Bindings {

    public static final String ARRAY_LENGTH_FIELD_BINDING_STRING = "(array type):length";//$NON-NLS-1$

    private Bindings() {
        // No instance
    }

    /**
     * Returns the type binding of the node's enclosing type declaration.
     *
     * @param node
     *         an AST node
     * @return the type binding of the node's parent type declaration, or <code>null</code>
     */
    public static ITypeBinding getBindingOfParentType(ASTNode node) {
        while (node != null) {
            if (node instanceof AbstractTypeDeclaration) {
                return ((AbstractTypeDeclaration)node).resolveBinding();
            } else if (node instanceof AnonymousClassDeclaration) {
                return ((AnonymousClassDeclaration)node).resolveBinding();
            }
            node = node.getParent();
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the given type is a super type of a candidate.
     * <code>true</code> is returned if the two type bindings are identical.
     * <p/>
     * <p><b>Warning:</b> With the addition of generics, this method is valid in less
     * cases than before. Consider using {@link TypeRules#canAssign(ITypeBinding, ITypeBinding)}
     * if you're dealing with types of variables. The classical notion of supertypes
     * only makes sense if you really need to walk the type hierarchy but don't need to play
     * the assignment rules.</p>
     *
     * @param possibleSuperType
     *         the type to inspect
     * @param type
     *         the type whose super types are looked at
     * @return <code>true</code> iff <code>possibleSuperType</code> is
     * a super type of <code>type</code> or is equal to it
     */
    public static boolean isSuperType(ITypeBinding possibleSuperType, ITypeBinding type) {
        return isSuperType(possibleSuperType, type, true);
    }

    /**
     * Returns <code>true</code> if the given type is a super type of a candidate.
     * <code>true</code> is returned if the two type bindings are identical (TODO)
     *
     * @param possibleSuperType
     *         the type to inspect
     * @param type
     *         the type whose super types are looked at
     * @param considerTypeArguments
     *         if <code>true</code>, consider type arguments of <code>type</code>
     * @return <code>true</code> iff <code>possibleSuperType</code> is
     * a super type of <code>type</code> or is equal to it
     */
    public static boolean isSuperType(ITypeBinding possibleSuperType, ITypeBinding type, boolean considerTypeArguments) {
        if (type.isArray() || type.isPrimitive()) {
            return false;
        }
        if (!considerTypeArguments) {
            type = type.getTypeDeclaration();
        }
        if (Bindings.equals(type, possibleSuperType)) {
            return true;
        }
        ITypeBinding superClass = type.getSuperclass();
        if (superClass != null) {
            if (isSuperType(possibleSuperType, superClass, considerTypeArguments)) {
                return true;
            }
        }

        if (possibleSuperType.isInterface()) {
            ITypeBinding[] superInterfaces = type.getInterfaces();
            for (int i = 0; i < superInterfaces.length; i++) {
                if (isSuperType(possibleSuperType, superInterfaces[i], considerTypeArguments)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks if the two bindings are equals. Also works across binding environments.
     *
     * @param b1
     *         first binding treated as <code>this</code>. So it must
     *         not be <code>null</code>
     * @param b2
     *         the second binding.
     * @return boolean
     */
    public static boolean equals(IBinding b1, IBinding b2) {
        return b1.isEqualTo(b2);
    }
}
