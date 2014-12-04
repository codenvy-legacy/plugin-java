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
package com.codenvy.ide.ext.java.jdi.client;

import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.debug.DebuggerManager;
import com.codenvy.ide.ext.java.jdi.client.actions.DebugAction;
import com.codenvy.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import com.codenvy.ide.ext.java.jdi.client.fqn.FqnResolverFactory;
import com.codenvy.ide.ext.java.jdi.client.fqn.JavaFqnResolver;
import com.codenvy.ide.extension.maven.shared.MavenAttributes;
import com.codenvy.ide.extension.runner.client.RunnerLocalizationConstant;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.codenvy.ide.MimeType.APPLICATION_JAVA;
import static com.codenvy.ide.api.action.IdeActions.GROUP_RUN;
import static com.codenvy.ide.api.action.IdeActions.GROUP_RUN_CONTEXT_MENU;
import static com.codenvy.ide.api.constraints.Anchor.AFTER;

/**
 * Extension allows debug Java web applications.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Java Debugger", version = "3.0.0")
public class JavaRuntimeExtension {
    /** Channel for the messages containing debugger events. */
    public static final String EVENTS_CHANNEL     = "debugger:events:";
    /** Channel for the messages containing message which informs about debugger is disconnected. */
    public static final String DISCONNECT_CHANNEL = "debugger:disconnected:";

    @Inject
    public JavaRuntimeExtension(ActionManager actionManager,
                                DebugAction debugAction,
                                DebuggerManager debuggerManager,
                                DebuggerPresenter debuggerPresenter,
                                FqnResolverFactory resolverFactory,
                                JavaFqnResolver javaFqnResolver,
                                JavaRuntimeLocalizationConstant localizationConstant,
                                RunnerLocalizationConstant runnerLocalizationConstants) {
        // register actions
        actionManager.registerAction(localizationConstant.debugAppActionId(), debugAction);

        // add actions in main menu
        DefaultActionGroup runMenuActionGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);
        runMenuActionGroup.add(debugAction, new Constraints(AFTER, runnerLocalizationConstants.customRunAppActionId()));

        // add actions in context menu
        DefaultActionGroup runContextGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RUN_CONTEXT_MENU);
        runContextGroup.add(debugAction);

        debuggerManager.registeredDebugger(MavenAttributes.MAVEN_ID, debuggerPresenter);
        debuggerManager.registeredDebugger(com.codenvy.ide.Constants.CODENVY_PLUGIN_ID, debuggerPresenter);
        resolverFactory.addResolver(APPLICATION_JAVA, javaFqnResolver);
    }
}