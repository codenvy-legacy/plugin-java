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

import com.codenvy.ide.ext.java.server.dom.ASTNodes;
import com.codenvy.ide.ext.java.server.internal.core.JavaProject;
import com.codenvy.ide.ext.java.server.javadoc.ASTProvider;
import com.codenvy.ide.ext.java.server.javadoc.HTMLPrinter;
import com.codenvy.ide.ext.java.server.javadoc.JavaDocLocations;
import com.codenvy.ide.ext.java.server.javadoc.JavaElementLabels;
import com.codenvy.ide.ext.java.server.javadoc.JavaElementLinks;
import com.codenvy.ide.ext.java.server.javadoc.JavadocContentAccess2;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Evgen Vidolob
 */
public class JavadocFinder {
    private static final long LABEL_FLAGS          = JavaElementLabels.ALL_FULLY_QUALIFIED
                                                     | JavaElementLabels.M_PRE_RETURNTYPE | JavaElementLabels.M_PARAMETER_ANNOTATIONS |
                                                     JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES |
                                                     JavaElementLabels.M_EXCEPTIONS
                                                     | JavaElementLabels.F_PRE_TYPE_SIGNATURE | JavaElementLabels.M_PRE_TYPE_PARAMETERS |
                                                     JavaElementLabels.T_TYPE_PARAMETERS
                                                     | JavaElementLabels.USE_RESOLVED;
    private static final long LOCAL_VARIABLE_FLAGS =
            LABEL_FLAGS & ~JavaElementLabels.F_FULLY_QUALIFIED | JavaElementLabels.F_POST_QUALIFIED;
    private static final long TYPE_PARAMETER_FLAGS = LABEL_FLAGS | JavaElementLabels.TP_POST_QUALIFIED;
    private static final long PACKAGE_FLAGS        = LABEL_FLAGS & ~JavaElementLabels.ALL_FULLY_QUALIFIED;
    private String baseHref;

    public JavadocFinder(String baseHref) {
        this.baseHref = baseHref;
    }

    private static long getHeaderFlags(IJavaElement element) {
        switch (element.getElementType()) {
            case IJavaElement.LOCAL_VARIABLE:
                return LOCAL_VARIABLE_FLAGS;
            case IJavaElement.TYPE_PARAMETER:
                return TYPE_PARAMETER_FLAGS;
            case IJavaElement.PACKAGE_FRAGMENT:
                return PACKAGE_FLAGS;
            default:
                return LABEL_FLAGS;
        }
    }

    private static IBinding resolveBinding(ASTNode node) {
        if (node instanceof SimpleName) {
            SimpleName simpleName = (SimpleName)node;
            // workaround for https://bugs.eclipse.org/62605 (constructor name resolves to type, not method)
            ASTNode normalized = ASTNodes.getNormalizedNode(simpleName);
            if (normalized.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY) {
                ClassInstanceCreation cic = (ClassInstanceCreation)normalized.getParent();
                IMethodBinding constructorBinding = cic.resolveConstructorBinding();
                if (constructorBinding == null)
                    return null;
                ITypeBinding declaringClass = constructorBinding.getDeclaringClass();
                if (!declaringClass.isAnonymous())
                    return constructorBinding;
                ITypeBinding superTypeDeclaration = declaringClass.getSuperclass().getTypeDeclaration();
                return resolveSuperclassConstructor(superTypeDeclaration, constructorBinding);
            }
            return simpleName.resolveBinding();

        } else if (node instanceof SuperConstructorInvocation) {
            return ((SuperConstructorInvocation)node).resolveConstructorBinding();
        } else if (node instanceof ConstructorInvocation) {
            return ((ConstructorInvocation)node).resolveConstructorBinding();
        } else {
            return null;
        }
    }

    private static IBinding resolveSuperclassConstructor(ITypeBinding superClassDeclaration, IMethodBinding constructor) {
        IMethodBinding[] methods = superClassDeclaration.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            IMethodBinding method = methods[i];
            if (method.isConstructor() && constructor.isSubsignature(method))
                return method;
        }
        return null;
    }

    private static StringBuffer addLink(StringBuffer buf, String uri, String label) {
        return buf.append(JavaElementLinks.createLink(uri, label));
    }

    private static String getImageURL(IJavaElement element) {
        String imageName = null;
        //todo
        URL imageUrl = null; //JavaPlugin.getDefault().getImagesOnFSRegistry().getImageURL(element);
        if (imageUrl != null) {
            imageName = imageUrl.toExternalForm();
        }

        return imageName;
    }

    public String findJavadoc4Handle(JavaProject project, String handle) {
        IJavaElement javaElement = JavaElementLinks.parseURI(handle, project);
        if (javaElement == null || !(javaElement instanceof IMember)) {
            return null;
        }
        return getJavadoc((IMember)javaElement);
    }

    public String findJavadoc(JavaProject project, String fqn) throws JavaModelException {
        IType element = project.findType(fqn);
        if (element == null) {
            return null;
        }
        return getJavadoc(element);
    }

    private String getJavadoc(IMember element) {
        StringBuffer buffer = new StringBuffer();
        boolean hasContents = false;
        if (element instanceof IPackageFragment || element instanceof IMember) {
            HTMLPrinter.addSmallHeader(buffer, getInfoText(element, element.getTypeRoot(), true));
            buffer.append("<br>"); //$NON-NLS-1$
            addAnnotations(buffer, element, element.getTypeRoot(), null);
            Reader reader = null;
            try {
                String content = element instanceof IMember
                                 ? JavadocContentAccess2.getHTMLContent(element, true, baseHref)
                                 : null;//JavadocContentAccess2.getHTMLContent((IPackageFragment)element);
                IPackageFragmentRoot root = (IPackageFragmentRoot)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
//                boolean isBinary = root.exists() && root.getKind() == IPackageFragmentRoot.K_BINARY;
                if (content != null) {
//                base= JavaDocLocations.getBaseURL(element, isBinary);
                    reader = new StringReader(content);
                } else {
                    String explanationForMissingJavadoc = JavaDocLocations.getExplanationForMissingJavadoc(element, root);
                    if (explanationForMissingJavadoc != null)
                        reader = new StringReader(explanationForMissingJavadoc);
                }
            } catch (CoreException ex) {
                reader = new StringReader(JavaDocLocations.handleFailedJavadocFetch(ex));
            }

            if (reader != null) {
                HTMLPrinter.addParagraph(buffer, reader);
            }
            hasContents = true;
        }

        if (!hasContents)
            return null;

        if (buffer.length() > 0) {
            //todo use url for css
            HTMLPrinter.insertPageProlog(buffer, 0, "");
//            if (base != null) {
//                int endHeadIdx= buffer.indexOf("</head>"); //$NON-NLS-1$
//                buffer.insert(endHeadIdx, "\n<base href='" + base + "'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
//            }
            HTMLPrinter.addPageEpilog(buffer);
            return buffer.toString();
        }

        return null;
    }

    private String getInfoText(IJavaElement element, ITypeRoot editorInputElement, boolean allowImage) {
        long flags = getHeaderFlags(element);
        StringBuffer label = new StringBuffer(JavaElementLinks.getElementLabel(element, flags));

//        if (element.getElementType() == IJavaElement.FIELD) {
//            String constantValue= getConstantValue((IField) element, editorInputElement);
//            if (constantValue != null) {
//                constantValue= HTMLPrinter.convertToHTMLContentWithWhitespace(constantValue);
//                IJavaProject javaProject= element.getJavaProject();
//                label.append(getFormattedAssignmentOperator(javaProject));
//                label.append(constantValue);
//            }
//        }

//		if (element.getElementType() == IJavaElement.METHOD) {
//			IMethod method= (IMethod)element;
//			//TODO: add default value for annotation type members, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=249016
//		}

        return getImageAndLabel(element, allowImage, label.toString());
    }

    public void addAnnotations(StringBuffer buf, IJavaElement element, ITypeRoot editorInputElement, IRegion hoverRegion) {
        try {
            if (element instanceof IAnnotatable) {
                String annotationString = getAnnotations(element, editorInputElement, hoverRegion);
                if (annotationString != null) {
                    buf.append("<div style='margin-bottom: 5px;'>"); //$NON-NLS-1$
                    buf.append(annotationString);
                    buf.append("</div>"); //$NON-NLS-1$
                }
            } else if (element instanceof IPackageFragment) {
//                IPackageFragment pack= (IPackageFragment) element;
//                ICompilationUnit cu= pack.getCompilationUnit(JavaModelUtil.PACKAGE_INFO_JAVA);
//                if (cu.exists()) {
//                    IPackageDeclaration[] packDecls= cu.getPackageDeclarations();
//                    if (packDecls.length > 0) {
//                        addAnnotations(buf, packDecls[0], null, null);
//                    }
//                } else {
//                    IClassFile classFile= pack.getClassFile(JavaModelUtil.PACKAGE_INFO_CLASS);
//                    if (classFile.exists()) {
//                        addAnnotations(buf, classFile.getType(), null, null);
//                    }
//                }
            }
        } catch (JavaModelException e) {
            // no annotations this time...
            buf.append("<br>"); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            // no annotations this time...
            buf.append("<br>"); //$NON-NLS-1$
        }
    }

    private String getAnnotations(IJavaElement element, ITypeRoot editorInputElement, IRegion hoverRegion)
            throws URISyntaxException, JavaModelException {
        if (!(element instanceof IPackageFragment)) {
            if (!(element instanceof IAnnotatable))
                return null;

            if (((IAnnotatable)element).getAnnotations().length == 0)
                return null;
        }

        IBinding binding;
        //TODO
        ASTNode node = null; //getHoveredASTNode(editorInputElement, hoverRegion);

        if (node == null) {
            ASTParser p = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
            p.setProject(element.getJavaProject());
            p.setBindingsRecovery(true);
            try {
                binding = p.createBindings(new IJavaElement[]{element}, null)[0];
            } catch (OperationCanceledException e) {
                return null;
            }

        } else {
            binding = resolveBinding(node);
        }

        if (binding == null)
            return null;

        IAnnotationBinding[] annotations = binding.getAnnotations();
        if (annotations.length == 0)
            return null;

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < annotations.length; i++) {
            //TODO: skip annotations that don't have an @Documented annotation?
            addAnnotation(buf, element, annotations[i]);
            buf.append("<br>"); //$NON-NLS-1$
        }

        return buf.toString();
    }

    private void addAnnotation(StringBuffer buf, IJavaElement element, IAnnotationBinding annotation) throws URISyntaxException {
        IJavaElement javaElement = annotation.getAnnotationType().getJavaElement();
        buf.append('@');
        if (javaElement != null) {
            String uri = JavaElementLinks.createURI(baseHref, javaElement);
            addLink(buf, uri, annotation.getName());
        } else {
            buf.append(annotation.getName());
        }

        IMemberValuePairBinding[] mvPairs = annotation.getDeclaredMemberValuePairs();
        if (mvPairs.length > 0) {
            buf.append('(');
            for (int j = 0; j < mvPairs.length; j++) {
                if (j > 0) {
                    buf.append(JavaElementLabels.COMMA_STRING);
                }
                IMemberValuePairBinding mvPair = mvPairs[j];
                String memberURI = JavaElementLinks.createURI(baseHref, mvPair.getMethodBinding().getJavaElement());
                addLink(buf, memberURI, mvPair.getName());
                buf.append('=');
                addValue(buf, element, mvPair.getValue());
            }
            buf.append(')');
        }
    }

    private void addValue(StringBuffer buf, IJavaElement element, Object value) throws URISyntaxException {
        // Note: To be bug-compatible with Javadoc from Java 5/6/7, we currently don't escape HTML tags in String-valued annotations.
        if (value instanceof ITypeBinding) {
            ITypeBinding typeBinding = (ITypeBinding)value;
            IJavaElement type = typeBinding.getJavaElement();
            if (type == null) {
                buf.append(typeBinding.getName());
            } else {
                String uri = JavaElementLinks.createURI(baseHref, type);
                String name = type.getElementName();
                addLink(buf, uri, name);
            }
            buf.append(".class"); //$NON-NLS-1$

        } else if (value instanceof IVariableBinding) { // only enum constants
            IVariableBinding variableBinding = (IVariableBinding)value;
            IJavaElement variable = variableBinding.getJavaElement();
            String uri = JavaElementLinks.createURI(baseHref, variable);
            String name = variable.getElementName();
            addLink(buf, uri, name);

        } else if (value instanceof IAnnotationBinding) {
            IAnnotationBinding annotationBinding = (IAnnotationBinding)value;
            addAnnotation(buf, element, annotationBinding);

        } else if (value instanceof String) {
            buf.append(ASTNodes.getEscapedStringLiteral((String)value));

        } else if (value instanceof Character) {
            buf.append(ASTNodes.getEscapedCharacterLiteral((Character)value));

        } else if (value instanceof Object[]) {
            Object[] values = (Object[])value;
            buf.append('{');
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    buf.append(JavaElementLabels.COMMA_STRING);
                }
                addValue(buf, element, values[i]);
            }
            buf.append('}');

        } else { // primitive types (except char) or null
            buf.append(String.valueOf(value));
        }
    }

    public String getImageAndLabel(IJavaElement element, boolean allowImage, String label) {
        StringBuffer buf = new StringBuffer();
        int imageWidth = 16;
        int imageHeight = 16;
        int labelLeft = 20;
        int labelTop = 2;

        buf.append("<div style='word-wrap: break-word; position: relative; "); //$NON-NLS-1$

        String imageSrcPath = allowImage ? getImageURL(element) : null;
        if (imageSrcPath != null) {
            buf.append("margin-left: ").append(labelLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
            buf.append("padding-top: ").append(labelTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
        }

        buf.append("'>"); //$NON-NLS-1$
        if (imageSrcPath != null) {
            if (element != null) {
                try {
                    String uri = JavaElementLinks.createURI(baseHref, element);
                    buf.append("<a href='").append(uri).append("'>");  //$NON-NLS-1$//$NON-NLS-2$
                } catch (URISyntaxException e) {
                    element = null; // no link
                }
            }
            StringBuffer imageStyle = new StringBuffer("border:none; position: absolute; "); //$NON-NLS-1$
            imageStyle.append("width: ").append(imageWidth).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
            imageStyle.append("height: ").append(imageHeight).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
            imageStyle.append("left: ").append(-labelLeft - 1).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$

//            // hack for broken transparent PNG support in IE 6, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=223900 :
//            buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
            String tooltip = element == null ? "" : "alt='" + "Open Declaration" + "' "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//            buf.append("<span ").append(tooltip).append("style=\"").append(imageStyle). //$NON-NLS-1$ //$NON-NLS-2$
//                    append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageSrcPath).append("')
// \"></span>\n"); //$NON-NLS-1$ //$NON-NLS-2$
//            buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$
//
//            buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
            buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='").append(imageSrcPath)
               .append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//            buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
//            buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
//            buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='").append(imageSrcPath).append
// ("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//            buf.append("<![endif]-->\n"); //$NON-NLS-1$
            if (element != null) {
                buf.append("</a>"); //$NON-NLS-1$
            }
        }

        buf.append(label);

        buf.append("</div>"); //$NON-NLS-1$
        return buf.toString();
    }
}
