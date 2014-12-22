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

package com.codenvy.ide.ext.java.shared;

import com.codenvy.dto.shared.DTO;

/**
 * @author Evgen Vidolob
 */
@DTO
public interface OpenDeclarationDescriptor {
    int getLibId();

    void setLibId(int libId);

    int getOffset();

    void setOffset(int offset);

    int getLength();

    void setLength(int length);

    String getPath();

    void setPath(String path);

    boolean isBinary();

    void setBinary(boolean binary);

}
