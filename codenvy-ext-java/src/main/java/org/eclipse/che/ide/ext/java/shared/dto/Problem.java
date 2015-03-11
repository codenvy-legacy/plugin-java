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

package org.eclipse.che.ide.ext.java.shared.dto;


import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * DTO for {@link org.eclipse.jdt.core.compiler.IProblem}
 * Description of a Java problem, as detected by the compiler or some of the underlying
 * technology reusing the compiler.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Problem {
    /**
     * Answer the file name in which the problem was found.
     *
     * @return the file name in which the problem was found
     */
    String getOriginatingFileName();

    /**
     * Answer a localized, human-readable message string which describes the problem.
     *
     * @return a localized, human-readable message string which describes the problem
     */
    String getMessage();

    /**
     * Returns the problem id
     *
     * @return the problem id
     */
    int getID();

    /**
     * Answer back the original arguments recorded into the problem.
     *
     * @return the original arguments recorded into the problem
     */
    List<String> getArguments();

    /**
     * Answer the start position of the problem (inclusive), or -1 if unknown.
     *
     * @return the start position of the problem (inclusive), or -1 if unknown
     */
    int getSourceStart();

    /**
     * Answer the end position of the problem (inclusive), or -1 if unknown.
     *
     * @return the end position of the problem (inclusive), or -1 if unknown
     */
    int getSourceEnd();

    /**
     * Answer the line number in source where the problem begins.
     *
     * @return the line number in source where the problem begins
     */
    int getSourceLineNumber();

    int getSeverity();
}
