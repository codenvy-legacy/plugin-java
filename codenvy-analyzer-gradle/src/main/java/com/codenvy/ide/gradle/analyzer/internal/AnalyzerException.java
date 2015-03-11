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
package com.codenvy.ide.gradle.analyzer.internal;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.rest.shared.dto.ServiceError;

/**
 * @author Vladyslav Zhukovskyi
 */
@SuppressWarnings("serial")
public class AnalyzerException extends ServerException {
    public AnalyzerException(String message) {
        super(message);
    }

    public AnalyzerException(ServiceError serviceError) {
        super(serviceError);
    }

    public AnalyzerException(Throwable cause) {
        super(cause);
    }

    public AnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }
}
