package com.codenvy.ide.ext.java.server.dom;

import com.codenvy.ide.ext.java.server.javadoc.ASTProvider;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Internal helper methods that deal with {@link org.eclipse.jdt.core.dom.ASTNode}s:
 *
 * @author Evgen Vidolob
 */
public class ASTNodes {
    public static String getSimpleNameIdentifier(Name name) {
        if (name.isQualifiedName()) {
            return ((QualifiedName)name).getName().getIdentifier();
        } else {
            return ((SimpleName)name).getIdentifier();
        }
    }

    public static String asString(ASTNode node) {
        ASTFlattener flattener = new ASTFlattener();
        node.accept(flattener);
        return flattener.getResult();
    }

    /**
     * Escapes a string value to a literal that can be used in Java source.
     *
     * @param stringValue
     *         the string value
     * @return the escaped string
     * @see org.eclipse.jdt.core.dom.StringLiteral#getEscapedValue()
     */
    public static String getEscapedStringLiteral(String stringValue) {
        StringLiteral stringLiteral = AST.newAST(ASTProvider.SHARED_AST_LEVEL).newStringLiteral();
        stringLiteral.setLiteralValue(stringValue);
        return stringLiteral.getEscapedValue();
    }

    /**
     * Escapes a character value to a literal that can be used in Java source.
     *
     * @param ch
     *         the character value
     * @return the escaped string
     * @see org.eclipse.jdt.core.dom.CharacterLiteral#getEscapedValue()
     */
    public static String getEscapedCharacterLiteral(char ch) {
        CharacterLiteral characterLiteral = AST.newAST(ASTProvider.SHARED_AST_LEVEL).newCharacterLiteral();
        characterLiteral.setCharValue(ch);
        return characterLiteral.getEscapedValue();
    }

    /**
     * Adds flags to the given node and all its descendants.
     *
     * @param root
     *         The root node
     * @param flags
     *         The flags to set
     */
    public static void setFlagsToAST(ASTNode root, final int flags) {
        root.accept(new GenericVisitor(true) {
            @Override
            protected boolean visitNode(ASTNode node) {
                node.setFlags(node.getFlags() | flags);
                return true;
            }
        });
    }
}
