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

/**
 * Highlighted Positions.
 * Mostly used for semantic highlighting.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface HighlightedPosition {

    /**
     * Returns the length of this position.
     *
     * @return the length of this position
     */
    int getLength();

    /**
     * Returns the offset of this position.
     *
     * @return the offset of this position
     */
    int getOffset();

    /**
     * Type of highlighting.
     * Used for selecting proper css style for this highlighting;
     * Example:<code>
     * staticFinalField, staticField, field, enum
     * </code>
     *
     * @return
     */
    String getType();

}
