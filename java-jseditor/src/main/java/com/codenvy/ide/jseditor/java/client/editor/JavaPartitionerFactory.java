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
package com.codenvy.ide.jseditor.java.client.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.codenvy.ide.ext.java.jdt.JavaPartitions;
import com.codenvy.ide.jseditor.client.partition.DefaultPartitioner;
import com.codenvy.ide.jseditor.client.partition.DocumentPartitioner;
import com.codenvy.ide.jseditor.client.partition.DocumentPositionMap;
import com.codenvy.ide.jseditor.client.partition.PartitionScanner;
import com.codenvy.ide.jseditor.client.partition.PartitionerFactory;

/**
 * Factory of document partitioner for java documents.
 */
public class JavaPartitionerFactory implements PartitionerFactory {

    /** Array with legal content types. */
    public static final List<String> LEGAL_CONTENT_TYPES = Collections.unmodifiableList(Arrays.asList(
            JavaPartitions.JAVA_DOC,
            JavaPartitions.JAVA_MULTI_LINE_COMMENT,
            JavaPartitions.JAVA_SINGLE_LINE_COMMENT,
            JavaPartitions.JAVA_STRING,
            JavaPartitions.JAVA_CHARACTER
    ));

    private final PartitionScanner scanner;

    @Inject
    public JavaPartitionerFactory(final JavaPartitionScanner scanner,
                                  final DocumentPositionMap documentPositionMap) {
        this.scanner = scanner;
    }

    @Override
    public DocumentPartitioner create(final DocumentPositionMap documentPositionMap) {
        return new DefaultPartitioner(this.scanner,
                                      LEGAL_CONTENT_TYPES,
                                      documentPositionMap);
    }

}
