/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.java.jdt.internal.corext.fix;

import com.codenvy.ide.ext.java.jdt.codeassistant.api.IProblemLocation;
import com.codenvy.ide.ext.java.jdt.core.JavaCore;
import com.codenvy.ide.ext.java.jdt.core.dom.AST;
import com.codenvy.ide.ext.java.jdt.core.dom.ASTNode;
import com.codenvy.ide.ext.java.jdt.core.dom.AbstractTypeDeclaration;
import com.codenvy.ide.ext.java.jdt.core.dom.AnonymousClassDeclaration;
import com.codenvy.ide.ext.java.jdt.core.dom.ClassInstanceCreation;
import com.codenvy.ide.ext.java.jdt.core.dom.CompilationUnit;
import com.codenvy.ide.ext.java.jdt.core.dom.EnumConstantDeclaration;
import com.codenvy.ide.ext.java.jdt.core.dom.Modifier;
import com.codenvy.ide.ext.java.jdt.core.dom.TypeDeclaration;
import com.codenvy.ide.ext.java.jdt.core.dom.rewrite.ASTRewrite;
import com.codenvy.ide.ext.java.jdt.internal.corext.refactoring.code.CompilationUnitChange;
import com.codenvy.ide.ext.java.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import com.codenvy.ide.ext.java.jdt.internal.text.correction.CorrectionMessages;
import com.codenvy.ide.ext.java.jdt.refactoring.Change;
import com.codenvy.ide.ext.java.jdt.refactoring.NullChange;
import com.codenvy.ide.ext.java.jdt.text.Document;
import com.codenvy.ide.ext.java.jdt.text.edits.MultiTextEdit;
import com.codenvy.ide.ext.java.jdt.text.edits.TextEditGroup;
import com.codenvy.ide.runtime.Assert;
import com.codenvy.ide.runtime.CoreException;
import com.codenvy.ide.runtime.IStatus;
import com.codenvy.ide.runtime.Status;

import java.util.ArrayList;

public class UnimplementedCodeFix extends CompilationUnitRewriteOperationsFix {

    public static final class MakeTypeAbstractOperation extends CompilationUnitRewriteOperation {

        private final TypeDeclaration fTypeDeclaration;

        public MakeTypeAbstractOperation(TypeDeclaration typeDeclaration) {
            fTypeDeclaration = typeDeclaration;
        }

        /** {@inheritDoc} */
        @Override
        public void rewriteAST(CompilationUnitRewrite cuRewrite) throws CoreException {
            AST ast = cuRewrite.getAST();
            ASTRewrite rewrite = cuRewrite.getASTRewrite();
            Modifier newModifier = ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
            TextEditGroup textEditGroup =
                    createTextEditGroup(CorrectionMessages.INSTANCE.UnimplementedCodeFix_TextEditGroup_label(), cuRewrite);
            rewrite.getListRewrite(fTypeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY).insertLast(newModifier,
                                                                                                     textEditGroup);

            //TODO
            //         LinkedProposalPositionGroup group = new LinkedProposalPositionGroup("modifier"); //$NON-NLS-1$
            //         group.addPosition(rewrite.track(newModifier), !linkedProposalPositions.hasLinkedPositions());
            //         linkedProposalPositions.addPositionGroup(group);
        }
    }

    public static ICleanUpFix createCleanUp(CompilationUnit root, boolean addMissingMethod, boolean makeTypeAbstract,
                                            IProblemLocation[] problems, Document document) {
        Assert.isLegal(!addMissingMethod || !makeTypeAbstract);
        if (!addMissingMethod && !makeTypeAbstract)
            return null;

        if (problems.length == 0)
            return null;

        ArrayList<CompilationUnitRewriteOperation> operations = new ArrayList<CompilationUnitRewriteOperation>();

        for (int i = 0; i < problems.length; i++) {
            IProblemLocation problem = problems[i];
            if (addMissingMethod) {
                ASTNode typeNode = getSelectedTypeNode(root, problem);
                if (typeNode != null && !isTypeBindingNull(typeNode)) {
                    operations.add(new AddUnimplementedMethodsOperation(typeNode));
                }
            } else {
                ASTNode typeNode = getSelectedTypeNode(root, problem);
                if (typeNode instanceof TypeDeclaration) {
                    operations.add(new MakeTypeAbstractOperation((TypeDeclaration)typeNode));
                }
            }
        }

        if (operations.size() == 0)
            return null;

        String label;
        if (addMissingMethod) {
            label = CorrectionMessages.INSTANCE.UnimplementedMethodsCorrectionProposal_description();
        } else {
            label = CorrectionMessages.INSTANCE.UnimplementedCodeFix_MakeAbstractFix_label();
        }
        return new UnimplementedCodeFix(label, root, operations.toArray(new CompilationUnitRewriteOperation[operations
                .size()]), document);
    }

    public static IProposableFix createAddUnimplementedMethodsFix(final CompilationUnit root, IProblemLocation problem,
                                                                  final Document document) {
        ASTNode typeNode = getSelectedTypeNode(root, problem);
        if (typeNode == null)
            return null;

        if (isTypeBindingNull(typeNode))
            return null;

        AddUnimplementedMethodsOperation operation = new AddUnimplementedMethodsOperation(typeNode);
        if (operation.getMethodsToImplement().length > 0) {
            return new UnimplementedCodeFix(
                    CorrectionMessages.INSTANCE.UnimplementedMethodsCorrectionProposal_description(), root,
                    new CompilationUnitRewriteOperation[]{operation}, document);
        } else {
            return new IProposableFix() {
                public CompilationUnitChange createChange() throws CoreException {
                    CompilationUnitChange change =
                            new CompilationUnitChange(
                                    CorrectionMessages.INSTANCE.UnimplementedMethodsCorrectionProposal_description(), document) {
                                @Override
                                public Change perform() throws CoreException {
                                    //TODO
                                    //							Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                                    //							String dialogTitle= CorrectionMessages.UnimplementedMethodsCorrectionProposal_description;
                                    //							IStatus status= getStatus();
                                    //							ErrorDialog.openError(shell, dialogTitle,
                                    // CorrectionMessages.UnimplementedCodeFix_DependenciesErrorMessage, status);

                                    return new NullChange();
                                }
                            };
                    change.setEdit(new MultiTextEdit());
                    return change;
                }

                public String getAdditionalProposalInfo() {
                    return new String();
                }

                public String getDisplayString() {
                    return CorrectionMessages.INSTANCE.UnimplementedMethodsCorrectionProposal_description();
                }

                public IStatus getStatus() {
                    return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID,
                                      CorrectionMessages.INSTANCE.UnimplementedCodeFix_DependenciesStatusMessage());
                }
            };
        }
    }

    public static UnimplementedCodeFix createMakeTypeAbstractFix(CompilationUnit root, IProblemLocation problem,
                                                                 Document document) {
        ASTNode typeNode = getSelectedTypeNode(root, problem);
        if (!(typeNode instanceof TypeDeclaration))
            return null;

        TypeDeclaration typeDeclaration = (TypeDeclaration)typeNode;
        MakeTypeAbstractOperation operation = new MakeTypeAbstractOperation(typeDeclaration);

        //TODO
        String label =
                CorrectionMessages.INSTANCE.ModifierCorrectionSubProcessor_addabstract_description(typeDeclaration.getName()
                                                                                                                  .getIdentifier());
        //         CorrectionMessages.INSTANCE.ModifierCorrectionSubProcessor_addabstract_description(
        //            BasicElementLabels.getJavaElementName(typeDeclaration.getName().getIdentifier()));
        return new UnimplementedCodeFix(label, root, new CompilationUnitRewriteOperation[]{operation}, document);
    }

    public static ASTNode getSelectedTypeNode(CompilationUnit root, IProblemLocation problem) {
        ASTNode selectedNode = problem.getCoveringNode(root);
        if (selectedNode == null)
            return null;

        if (selectedNode.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) { // bug 200016
            selectedNode = selectedNode.getParent();
        }

        if (selectedNode.getLocationInParent() == EnumConstantDeclaration.NAME_PROPERTY) {
            selectedNode = selectedNode.getParent();
        }
        if (selectedNode.getNodeType() == ASTNode.SIMPLE_NAME
            && selectedNode.getParent() instanceof AbstractTypeDeclaration) {
            return selectedNode.getParent();
        } else if (selectedNode.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
            return ((ClassInstanceCreation)selectedNode).getAnonymousClassDeclaration();
        } else if (selectedNode.getNodeType() == ASTNode.ENUM_CONSTANT_DECLARATION) {
            EnumConstantDeclaration enumConst = (EnumConstantDeclaration)selectedNode;
            if (enumConst.getAnonymousClassDeclaration() != null)
                return enumConst.getAnonymousClassDeclaration();
            return enumConst;
        } else {
            return null;
        }
    }

    private static boolean isTypeBindingNull(ASTNode typeNode) {
        if (typeNode instanceof AbstractTypeDeclaration) {
            AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration)typeNode;
            if (abstractTypeDeclaration.resolveBinding() == null)
                return true;

            return false;
        } else if (typeNode instanceof AnonymousClassDeclaration) {
            AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration)typeNode;
            if (anonymousClassDeclaration.resolveBinding() == null)
                return true;

            return false;
        } else if (typeNode instanceof EnumConstantDeclaration) {
            return false;
        } else {
            return true;
        }
    }

    public UnimplementedCodeFix(String name, CompilationUnit compilationUnit,
                                CompilationUnitRewriteOperation[] fixRewriteOperations, Document document) {
        super(name, compilationUnit, fixRewriteOperations, document);
    }
}
