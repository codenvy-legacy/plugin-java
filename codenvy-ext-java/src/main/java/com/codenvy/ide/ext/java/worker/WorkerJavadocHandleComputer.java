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
package com.codenvy.ide.ext.java.worker;

import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.ext.java.jdt.core.dom.ASTNode;
import com.codenvy.ide.ext.java.jdt.core.dom.CompilationUnit;
import com.codenvy.ide.ext.java.jdt.core.dom.IBinding;
import com.codenvy.ide.ext.java.jdt.core.dom.IMethodBinding;
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


    private JavaParserWorker worker;
    private WorkerCuCache    cuCache;

    public WorkerJavadocHandleComputer(JavaParserWorker worker, WorkerCuCache cuCache) {
        this.worker = worker;
        this.cuCache = cuCache;
    }


    @Override
    public void onMessageReceived(ComputeJavadocHandle message) {
        CompilationUnit cu = cuCache.getCompilationUnit(message.getFilePath());
        String source = cuCache.getSource(message.getFilePath());
        String handle;
        if (cu == null || source == null) {
            handle = null;
        } else {
            WorkerDocument document = new WorkerDocument(source);
            Region word = JavaWordFinder.findWord(document, message.getOffset());
            handle = getHandle(word, cu);
        }

        JavadocHandleComputed result = JavadocHandleComputed.make();
        result.setId(message.id()).setHandle(handle);
        worker.sendMessage(result.serialize());
    }

    private String getHandle(Region word, CompilationUnit cu) {
        NodeFinder nf = new NodeFinder(cu, word.getOffset(), word.getLength());
        ASTNode coveringNode = nf.getCoveredNode();
        if (coveringNode == null) {
            return null;
        }
        if (coveringNode.getNodeType() == ASTNode.MODIFIER)
            return null;


        if (coveringNode instanceof SimpleName) {
            SimpleName nn = (SimpleName)coveringNode;
            IBinding binding = nn.resolveBinding();
            if (binding.getKind() == IBinding.VARIABLE) {
                IVariableBinding var = (IVariableBinding)binding;
                if (var.isField()) {
                    return var.getKey();
                }
            }


            if (binding.getKind() == IBinding.METHOD) {
                return getKeyForMethod(binding);
            }

            if (binding.getKind() == IBinding.VARIABLE) {
                return getKeyForMethod(binding);
            }
            return binding.getKey();
        }

        ASTNode parentNode = coveringNode.getParent();

        if (parentNode instanceof SimpleType) {
            SimpleType type = (SimpleType)parentNode;
            return type.resolveBinding().getKey();
        }
        if (parentNode instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation)parentNode;
            return getKeyForMethod(mi.resolveMethodBinding());

        }

        return null;
    }

    private String getKeyForMethod(IBinding binding) {
        String key = binding.getKey();
        String fqn = key.substring(0, key.indexOf(';') + 1);
        String substring = key.substring(key.indexOf(';') + 1, key.length());
        substring = substring.replaceAll("/", ".");
        if(binding instanceof IMethodBinding){
            if (((IMethodBinding)binding).isConstructor()){
                substring = substring.substring(1);
                substring = "." + binding.getName() + substring;
            }
        }
        return fqn + substring;
    }
}
