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
package com.codenvy.ide.extension.maven.server.projecttype.generators;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.UnauthorizedException;
import com.codenvy.api.core.rest.HttpJsonHelper;
import com.codenvy.api.core.util.ValueHolder;
import com.codenvy.commons.lang.Pair;
import com.codenvy.dto.server.JsonStringMapImpl;
import com.codenvy.dto.shared.JsonStringMap;
import com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.codenvy.ide.extension.maven.server.projecttype.generators.dto.GenerateTask.Status.IN_PROGRESS;

/**
 * Callable task for generating a project.
 *
 * @author Artem Zatsarynnyy
 */
class GenerateTaskCallable implements Callable<GenerateTask> {
    private static String              serviceUrl;
    private final  String              archetypeGroupId;
    private final  String              archetypeArtifactId;
    private final  String              archetypeVersion;
    private final  String              groupId;
    private final  String              artifactId;
    private final  String              version;
    private final  Map<String, String> options;

    GenerateTaskCallable(String generatorServiceUrl, String archetypeGroupId, String archetypeArtifactId, String archetypeVersion,
                         String groupId, String artifactId, String version, @Nullable Map<String, String> options) {
        this.archetypeGroupId = archetypeGroupId;
        this.archetypeArtifactId = archetypeArtifactId;
        this.archetypeVersion = archetypeVersion;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.options = options;
        serviceUrl = generatorServiceUrl + "/maven-generator-archetype";
    }

    @Override
    public GenerateTask call() throws Exception {
        final Pair<String, String> archetypeGroupIdParam = Pair.of("archetypeGroupId", archetypeGroupId);
        final Pair<String, String> archetypeArtifactIdParam = Pair.of("archetypeArtifactId", archetypeArtifactId);
        final Pair<String, String> archetypeVersionParam = Pair.of("archetypeVersion", archetypeVersion);
        final Pair<String, String> groupIdParam = Pair.of("groupId", groupId);
        final Pair<String, String> artifactIdParam = Pair.of("artifactId", artifactId);
        final Pair<String, String> versionParam = Pair.of("version", version);

        final ValueHolder<String> statusUrlHolder = new ValueHolder<>();
        JsonStringMap<String> optionsMap = options == null ? null : new JsonStringMapImpl<>(options);
        try {
            GenerateTask generateTask = HttpJsonHelper.post(GenerateTask.class, serviceUrl + "/generate", optionsMap,
                                                            archetypeGroupIdParam, archetypeArtifactIdParam, archetypeVersionParam,
                                                            groupIdParam, artifactIdParam, versionParam);
            statusUrlHolder.set(generateTask.getStatusUrl());
        } catch (IOException | UnauthorizedException | NotFoundException e) {
            throw new ServerException(e);
        }

        final String statusUrl = statusUrlHolder.get();
        try {
            for (; ; ) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                GenerateTask generateTask = HttpJsonHelper.get(GenerateTask.class, statusUrl);
                if (IN_PROGRESS != generateTask.getStatus()) {
                    return generateTask;
                }
            }
        } catch (IOException | ServerException | NotFoundException | UnauthorizedException | ForbiddenException | ConflictException e) {
            throw new ServerException(e);
        }
    }
}
