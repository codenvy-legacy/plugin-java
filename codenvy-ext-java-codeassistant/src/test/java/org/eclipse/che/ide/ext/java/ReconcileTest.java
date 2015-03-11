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

package org.eclipse.che.ide.ext.java;


import org.eclipse.che.jdt.internal.core.BufferManager;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Evgen Vidolob
 */
public class ReconcileTest extends BaseTest {
    protected ICompilationUnit workingCopy;
    protected ProblemRequestor problemRequestor;
    protected WorkingCopyOwner wcOwner;
    // infos for invalid results
    protected int tabs = 2;

    void setWorkingCopyContents(String contents) throws JavaModelException {
        this.workingCopy.getBuffer().setContents(contents);
        this.problemRequestor.initialize(contents.toCharArray());
    }

    @Before
    public void init() throws Exception {
        this.problemRequestor = new ProblemRequestor();
        this.wcOwner = new WorkingCopyOwner() {
            public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
                return ReconcileTest.this.problemRequestor;
            }

            @Override
            public IBuffer createBuffer(ICompilationUnit workingCopy) {
                return BufferManager.createBuffer(workingCopy);
            }
        };
        this.workingCopy = project.findType("p1.X").getCompilationUnit().getWorkingCopy(this.wcOwner, null);
        this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
    }

    @Test
    public void testName() throws Exception {
        setWorkingCopyContents(
                "package p1;\n" +
                "public class X {\n" +
                "  public void foo() {\n" +
                "  }\n" +
                "  public void foo() {\n" +
                "  }\n" +
                "}");
        this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
        assertProblems(
                "Unexpected delta",
                "----------\n" +
                "1. ERROR in /test/src/main/java/p1/X.java (at line 3)\n" +
                "	public void foo() {\n" +
                "	            ^^^^^\n" +
                "Duplicate method foo() in type X\n" +
                "----------\n" +
                "2. ERROR in /test/src/main/java/p1/X.java (at line 5)\n" +
                "	public void foo() {\n" +
                "	            ^^^^^\n" +
                "Duplicate method foo() in type X\n" +
                "----------\n");
    }

    protected void assertProblems(String message, String expected) {
        assertProblems(message, expected, this.problemRequestor);
    }

    protected void assertProblems(String message, String expected, ProblemRequestor problemRequestor) {
        String actual = Utils.convertToIndependantLineDelimiter(problemRequestor.problems.toString());
        String independantExpectedString = Utils.convertToIndependantLineDelimiter(expected);
        if (!independantExpectedString.equals(actual)) {
            System.out.println(Utils.displayString(actual, this.tabs));
        }
        org.junit.Assert.assertEquals(
                message,
                independantExpectedString,
                actual);
    }

    public static class ProblemRequestor implements IProblemRequestor {
        public StringBuffer problems;
        public int          problemCount;
        public boolean isActive = true;
        protected char[] unitSource;

        public ProblemRequestor() {
            initialize(null);
        }

        public void acceptProblem(IProblem problem) {
            Utils.appendProblem(this.problems, problem, this.unitSource, ++this.problemCount);
            this.problems.append("----------\n");
        }

        public void beginReporting() {
            this.problems.append("----------\n");
        }

        public void endReporting() {
            if (this.problemCount == 0)
                this.problems.append("----------\n");
        }

        public boolean isActive() {
            return this.isActive;
        }

        public void initialize(char[] source) {
            reset();
            this.unitSource = source;
        }

        public void reset() {
            this.problems = new StringBuffer();
            this.problemCount = 0;
        }
    }
}
