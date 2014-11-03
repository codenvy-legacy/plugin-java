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

package com.codenvy.ide.ext.java.server;

import com.codenvy.commons.lang.Pair;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaProjectWatcher {

    private static JavaProjectWatcher INSTANCE;
    private ConcurrentHashMap<Pair<String, String>, CopyOnWriteArraySet<String>> openedProjects = new ConcurrentHashMap<>();
    @Inject
    private JavaProjectService projectService;

    public JavaProjectWatcher() {
        INSTANCE = this;
    }

    public static void sessionDestroyed(String sessionId) {
        if (INSTANCE == null)
            return;
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (Pair<String,String> key : INSTANCE.openedProjects.keySet()) {
            CopyOnWriteArraySet<String> sessions = INSTANCE.openedProjects.get(key);
            if (sessions.contains(sessionId)) {
                pairs.add(key);
            }
        }

        for (Pair<String, String> pair : pairs) {
            INSTANCE.projectClosed(sessionId, pair.first, pair.second);

        }
    }

    public void projectOpened(String sessionId, String wsId, String path) {
        Pair<String, String> pair = Pair.of(wsId, path);
        if (!openedProjects.containsKey(pair)) {
            openedProjects.putIfAbsent(pair, new CopyOnWriteArraySet<String>());
        }
        openedProjects.get(pair).add(sessionId);
    }

    public void projectClosed(String sessionId, String wsId, String path) {
        Pair<String, String> pair = Pair.of(wsId, path);
        if (openedProjects.containsKey(pair)) {
            CopyOnWriteArraySet<String> sessions = openedProjects.get(pair);
            sessions.remove(sessionId);
            if (sessions.size() == 0) {
                openedProjects.remove(pair, sessions);
                projectService.removeProject(pair.first, pair.second);
            }

        }
    }
}
