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
package com.codenvy.ide.ext.java.server;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.api.vfs.server.observation.VirtualFileEvent;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.ide.ext.java.server.core.resources.ResourceChangedEvent;
import com.codenvy.ide.ext.java.server.internal.core.JavaProject;
import com.codenvy.vfs.impl.fs.LocalFSMountStrategy;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Maintenance and create JavaProjects
 *
 * @author Evgen Vidolob
 */
@Singleton
public class JavaProjectService {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(JavaProjectService.class);

    private ConcurrentHashMap<String, JavaProject>                 cache       = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CopyOnWriteArraySet<String>> projectInWs = new ConcurrentHashMap<>();
    private LocalFSMountStrategy fsMountStrategy;
    private String               tempDir;
    private Map<String, String> options = new HashMap<>();

    @Inject
    public JavaProjectService(EventService eventService,
                              LocalFSMountStrategy fsMountStrategy,
                              @Named("project.temp") String temp) {
        eventService.subscribe(new VirtualFileEventSubscriber());
        this.fsMountStrategy = fsMountStrategy;
        tempDir = temp;
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
        options.put(JavaCore.CORE_ENCODING, "UTF-8");
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
        options.put(CompilerOptions.OPTION_TargetPlatform, JavaCore.VERSION_1_7);
        options.put(AssistOptions.OPTION_PerformVisibilityCheck, AssistOptions.ENABLED);
        options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_TaskTags, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
        options.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED);
        options.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
        options.put(JavaCore.COMPILER_PB_UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE, JavaCore.ENABLED);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
        options.put(CompilerOptions.OPTION_Process_Annotations, JavaCore.ENABLED);
        options.put(CompilerOptions.OPTION_GenerateClassFiles, JavaCore.ENABLED);
    }

    public JavaProject getOrCreateJavaProject(String wsId, String projectPath) {
        String key = wsId + projectPath;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        File mountPath;
        try {
            mountPath = fsMountStrategy.getMountPath(wsId);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
        JavaProject javaProject = new JavaProject(mountPath, projectPath, tempDir, wsId, new HashMap<>(options));
        cache.put(key, javaProject);
        if (!projectInWs.containsKey(wsId)) {
            projectInWs.put(wsId, new CopyOnWriteArraySet<String>());
        }
        projectInWs.get(wsId).add(projectPath);
        return javaProject;
    }

    public boolean isProjectDependencyExist(String wsId, String projectPath) {
        if (cache.containsKey(wsId + projectPath)) {
            return true;
        }
        File projectDepDir = new File(tempDir, wsId + projectPath);
        return projectDepDir.exists();
    }

    public void removeProject(String wsId, String projectPath) {
        JavaProject javaProject = cache.remove(wsId + projectPath);
        if (projectInWs.containsKey(wsId)) {
            projectInWs.get(wsId).remove(projectPath);
        }
        if (javaProject != null) {
            try {
                javaProject.close();
            } catch (JavaModelException e) {
                LOG.error("Error when trying close project.", e);
            }
        }
        deleteDependencyDirectory(wsId, projectPath);
    }

    public Map<String, String> getOptions() {
        return options;
    }

    private void deleteDependencyDirectory(String wsId, String projectPath) {
        File projectDepDir = new File(tempDir, wsId + projectPath);
        if (projectDepDir.exists()) {
            IoUtil.deleteRecursive(projectDepDir);
            File wsDepDir = new File(tempDir, wsId);
            if (wsDepDir.exists()) {
                String[] files = wsDepDir.list();
                if (files == null || files.length == 0) {
                    wsDepDir.delete();
                }
            }
        }
    }

    private class VirtualFileEventSubscriber implements EventSubscriber<VirtualFileEvent> {

        @Override
        public void onEvent(VirtualFileEvent event) {
            final VirtualFileEvent.ChangeType eventType = event.getType();
            final String eventWorkspace = event.getWorkspaceId();
            final String eventPath = event.getPath();
            try {
                if (eventType == VirtualFileEvent.ChangeType.DELETED) {
                    JavaProject javaProject = cache.get(eventWorkspace + eventPath);
                    if (javaProject != null) {
                        removeProject(eventWorkspace, eventPath);
                    } else if (event.isFolder()) {
                        if (isProjectDependencyExist(eventWorkspace, eventPath)) {
                            deleteDependencyDirectory(eventWorkspace, eventPath);
                        }
                    }
                } else {
                    if (projectInWs.containsKey(eventWorkspace)) {
                        for (String path : projectInWs.get(eventWorkspace)) {
                            if (eventPath.startsWith(path)) {
                                JavaProject javaProject = cache.get(eventWorkspace + path);
                                if (javaProject != null) {
                                    try {
                                        javaProject.getJavaModelManager().deltaState.resourceChanged(
                                                new ResourceChangedEvent(fsMountStrategy.getMountPath(eventWorkspace), event));
                                        javaProject.creteNewNameEnvironment();
                                    } catch (ServerException e) {
                                        LOG.error("Can't update java model", e);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (Throwable t){
                //catch all exceptions that may be happened
                LOG.error("Can't update java model", t);
            }
        }
    }
}
