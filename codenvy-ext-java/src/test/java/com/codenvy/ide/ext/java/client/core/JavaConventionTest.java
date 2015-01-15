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
package com.codenvy.ide.ext.java.client.core;

import com.codenvy.ide.ext.java.jdt.core.JavaConventions;
import com.codenvy.ide.ext.java.jdt.core.JavaCore;
import com.codenvy.ide.runtime.IStatus;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vitaly Parfonov
 */
public class JavaConventionTest {


    @Test
    public void testCompilationShouldBeOk() {
        IStatus status = JavaConventions.validateCompilationUnitName("Test.java", JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                                     JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        Assert.assertNotNull(status);
        System.out.println(status.getMessage());
        Assert.assertEquals(IStatus.OK, status.getSeverity());
    }


    @Test
    public void testPackageNameShouldBeOk() {
        IStatus status = JavaConventions.validatePackageName("com.ua", JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                             JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        Assert.assertNotNull(status);
        Assert.assertEquals(IStatus.OK, status.getSeverity());
    }


    @Test
    public void testCompilationUnitNotLatin() {
        IStatus status = JavaConventions.validateCompilationUnitName("бла.java", JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                                  JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        Assert.assertNotNull(status);
        Assert.assertEquals(IStatus.ERROR, status.getSeverity());
    }


    @Test
    public void testPackageNameNotLatin() {
        IStatus status = JavaConventions.validatePackageName("бла", JavaCore.getOption(JavaCore.COMPILER_SOURCE),
                                                             JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        Assert.assertNotNull(status);
        Assert.assertNotNull(status.getMessage());
        Assert.assertEquals(IStatus.ERROR, status.getSeverity());
    }


}