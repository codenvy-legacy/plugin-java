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
package org.eclipse.che.gradle.analyzer.model;

/**
 * Describe object that represent custom json gradle model built by {@link com.codenvy.ide.gradle.analyzer.GradleModelBuilder}.
 * Made to allow quick creating custom models that should be fetched from Gradle build.
 *
 * @author Vladyslav Zhukovskii
 */
public interface TransportableJSONMiscDefinition extends TransportableJSON {
}
