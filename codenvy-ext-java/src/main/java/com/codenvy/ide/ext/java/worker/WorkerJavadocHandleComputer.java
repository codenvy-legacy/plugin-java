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

package com.codenvy.ide.ext.java.worker;

import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.ext.java.jdt.core.dom.ASTNode;
import com.codenvy.ide.ext.java.jdt.core.dom.CompilationUnit;
import com.codenvy.ide.ext.java.jdt.core.dom.IBinding;
import com.codenvy.ide.ext.java.jdt.core.dom.IVariableBinding;
import com.codenvy.ide.ext.java.jdt.core.dom.MethodInvocation;
import com.codenvy.ide.ext.java.jdt.core.dom.NodeFinder;
import com.codenvy.ide.ext.java.jdt.core.dom.SimpleName;
import com.codenvy.ide.ext.java.jdt.core.dom.SimpleType;
import com.codenvy.ide.ext.java.jdt.internal.text.correction.JavaWordFinder;
import com.codenvy.ide.ext.java.messages.ComputeJavadocHandle;
import com.codenvy.ide.ext.java.messages.JavadocHandleComputed;
import com.google.gwt.webworker.client.messages.MessageFilter;

/**
 * @author Evgen Vidolob
 */
public class WorkerJavadocHandleComputer implements MessageFilter.MessageRecipient<ComputeJavadocHandle> {


    private CompilationUnit  cu;
    private String           source;
    private JavaParserWorker worker;

    public WorkerJavadocHandleComputer(JavaParserWorker worker) {
        this.worker = worker;
    }


    @Override
    public void onMessageReceived(ComputeJavadocHandle message) {

        String handle;
        if (cu == null || source == null) {
            handle = null;
        } else {
            WorkerDocument document = new WorkerDocument(source);
            Region word = JavaWordFinder.findWord(document, message.getOffset());
            handle = getHandle(word);
        }

        JavadocHandleComputed result = JavadocHandleComputed.make();
        result.setId(message.id()).setHandle(handle);
        worker.sendMessage(result.serialize());
    }

    private String getHandle(Region word) {
        NodeFinder nf = new NodeFinder(cu, word.getOffset(), word.getLength());
        ASTNode coveringNode = nf.getCoveredNode();
        if(coveringNode == null) {
            return null;
        }
        if (coveringNode.getNodeType() == ASTNode.MODIFIER)
            return null;
        ASTNode parentNode = coveringNode.getParent();


        if(parentNode instanceof SimpleType){
            SimpleType type = (SimpleType)parentNode;
            return type.resolveBinding().getKey();
        }
        if (parentNode instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation)parentNode;
//            IMethodBinding methodDeclaration = mi.resolveMethodBinding().getMethodDeclaration();
//            String className = methodDeclaration.getDeclaringClass().getQualifiedName();
//            ITypeBinding[] parameterTypes = methodDeclaration.getParameterTypes();
//            StringBuilder builder = new StringBuilder(className).append("::").append(methodDeclaration.getName());
//            if(parameterTypes.length != 0){
//                builder.append('(');
//            }
//            for (ITypeBinding parameterType : parameterTypes) {
//
//                BindingKey key = new BindingKey(parameterType.getKey());
//                builder.append(key.toSignature()).append(')');
//            }
//            if(builder.charAt(builder.length() - 1) == ')'){
//                builder.deleteCharAt(builder.length() - 1);
//            }
            String key = mi.resolveMethodBinding().getKey();
            String fqn = key.substring(0, key.indexOf(';'));
            String substring = key.substring(key.indexOf(';'), key.length());
            substring = substring.replaceAll("/", ".");
            return fqn + substring;

        }

        if(coveringNode instanceof SimpleName){
            SimpleName nn = (SimpleName)coveringNode;
            IBinding binding = nn.resolveBinding();
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding var = (IVariableBinding)binding;
                if (var.isField()) {
                    return var.getKey();
//                    String className = var.getDeclaringClass().getQualifiedName();
//                    return className + "#" + var.getName();
                }
            }

            if (binding.getKind() == IBinding.TYPE) {
                return binding.getKey();
            }
        }
        return null;
    }

    public void setCu(CompilationUnit cu) {
        this.cu = cu;

    }

    public void setSource(String source) {
        this.source = source;
    }
}
