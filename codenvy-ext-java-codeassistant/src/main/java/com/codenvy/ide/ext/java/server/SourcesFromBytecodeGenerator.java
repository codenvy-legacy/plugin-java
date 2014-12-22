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

import com.codenvy.ide.ext.java.server.javadoc.JavaElementLabelComposer;
import com.codenvy.ide.ext.java.server.javadoc.JavaElementLabels;
import com.google.inject.Singleton;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class SourcesFromBytecodeGenerator {

    public static final  String METHOD_BODY = " /* compiled code */ ";
    private static final String COMMENT     = new String(
            "\n // Failed to get sources. Instead, stub sources have been generated.\n // Implementation of methods is unavailable.\n");
    private static final String TAB         = "    ";

    public String generateSource(IType type) throws JavaModelException {
        StringBuilder builder = new StringBuilder();
        builder.append(COMMENT);
        builder.append("package ").append(type.getPackageFragment().getElementName()).append(";\n");

        generateType(type, builder, TAB);
        return builder.toString();
    }

    private void generateType(IType type, StringBuilder builder, String indent) throws JavaModelException {
        builder.append(indent.substring(TAB.length()));
        builder.append(getModifiers(type.getFlags())).append(' ').append(getJavaType(type)).append(' ').append(type.getElementName());
        if (!"java.lang.Object".equals(type.getSuperclassName()) && !"java.lang.Enum".equals(type.getSuperclassName())) {
            builder.append(" extends ").append(type.getSuperclassName());
        }
        if (type.getSuperInterfaceNames().length != 0) {
            builder.append(" implements ");
            for (String interfaceFqn : type.getSuperInterfaceNames()) {
                builder.append(interfaceFqn).append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(" {\n");


        List<IField> fields = new ArrayList<>();
        if(type.isEnum()) {
            builder.append(indent);
            for (IField field : type.getFields()) {
                if (field.isEnumConstant()) {
                    builder.append(field.getElementName()).append(", ");
                } else {
                    fields.add(field);
                }
            }
            if (", ".equals(builder.substring(builder.length() - 2))) {
                builder.delete(builder.length() - 2, builder.length());
            }
            builder.append(";\n");

        } else {
            fields.addAll(Arrays.asList(type.getFields()));
        }

        for (IField field : fields) {
            if(Flags.isSynthetic(field.getFlags())){
                continue;
            }
            builder.append(indent).append(getModifiers(field.getFlags()));
            if (builder.charAt(builder.length() - 1) != ' ') {
                builder.append(' ');
            }

            builder.append(Signature.toCharArray(field.getTypeSignature().toCharArray())).append(' ')
                   .append(field.getElementName());
            if (field.getConstant() != null) {
                builder.append(" = ");
                if (field.getConstant() instanceof String) {
                    builder.append('"').append(field.getConstant()).append('"');
                } else {
                    builder.append(field.getConstant());
                }
            }
            builder.append(";\n");
        }
        builder.append('\n');

        for (IMethod method : type.getMethods()) {
            if(method.getElementName().equals("<clinit>")){
                continue;
            }

            builder.append(indent).append(getModifiers(method.getFlags()));

            if (builder.charAt(builder.length() - 1) != ' ') {
                builder.append(' ');
            }
//            for (String typeParameter : method.getTypeParameterSignatures()) {
//                builder.append(Signature.toCharArray(("<" + typeParameter + ">").toCharArray())).append(" ");
//            }
            ITypeParameter[] typeParameters = method.getTypeParameters();
            for (ITypeParameter typeParameter : typeParameters) {
                StringBuffer buffer = new StringBuffer();
                new JavaElementLabelComposer(buffer).appendTypeParameterLabel(typeParameter, /*JavaElementLabels.M_PARAMETER_TYPES | */JavaElementLabels.M_FULLY_QUALIFIED /*| JavaElementLabels.T_FULLY_QUALIFIED| JavaElementLabels.P_COMPRESSED | JavaElementLabels.USE_RESOLVED*/);
                System.out.println(buffer.toString());
            }
//            if(method instanceof JavaElement){
//                IBinaryMethod elementInfo = (IBinaryMethod)((JavaElement)method).getElementInfo();
//                char[] signature = elementInfo.getGenericSignature();
//                if(signature != null) {
//                    System.out.println(Arrays.toString(Signature.getTypeParameters(new String(signature))));
//                }
//            }
            if (!method.isConstructor()) {
                builder.append(Signature.toCharArray(method.getReturnType().toCharArray())).append(' ');
            }
            builder.append(method.getElementName());
            builder.append('(');
            for (ILocalVariable variable : method.getParameters()) {
                builder.append(Signature.toString(variable.getTypeSignature()));
                builder.append(' ').append(variable.getElementName()).append(", ");

            }

            if (builder.charAt(builder.length() - 1) == ' ') {
                builder.delete(builder.length() - 2, builder.length());
            }
            builder.append(')');
            String[] exceptionTypes = method.getExceptionTypes();
            if (exceptionTypes != null && exceptionTypes.length != 0) {
                builder.append(' ').append("throws ");
                for (String exceptionType : exceptionTypes) {
                    builder.append(Signature.toCharArray(exceptionType.toCharArray())).append(", ");
                }
                builder.delete(builder.length() - 2, builder.length());
            }
            builder.append(" {").append(METHOD_BODY).append("}\n\n");
        }
        for (IType iType : type.getTypes()) {
            generateType(iType, builder, indent + indent);
        }
        builder.append(indent.substring(TAB.length()));
        builder.append("}\n");
    }

    private String getJavaType(IType type) throws JavaModelException {
        if (type.isAnnotation()) {
            return "@interface";
        }
        if (type.isClass()) {
            return "class";
        }

        if (type.isInterface()) {
            return "interface";
        }

        if (type.isEnum()) {
            return "enum";
        }

        return "can't determine type";
    }

    private String getModifiers(int flags) {
        StringBuilder modifiers = new StringBuilder();
        //package private modifier has no string representation

        if (Flags.isPublic(flags)) {
            modifiers.append("public ");
        }

        if (Flags.isProtected(flags)) {
            modifiers.append("protected ");
        }

        if (Flags.isPrivate(flags)) {
            modifiers.append("private ");
        }

        if(Flags.isStatic(flags)) {
            modifiers.append("static ");
        }

        if(Flags.isAbstract(flags)){
            modifiers.append("abstract ");
        }

        if(Flags.isFinal(flags)){
            modifiers.append("final ");
        }

        if(Flags.isNative(flags)){
            modifiers.append("native ");
        }

        if(Flags.isSynchronized(flags)){
            modifiers.append("synchronized ");
        }

        int len = modifiers.length();
        if (len == 0)
            return "";
        modifiers.setLength(len - 1);
        return modifiers.toString();
    }

}
