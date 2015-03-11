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
package org.eclipse.che.gradle.client;

import com.google.gwt.i18n.client.Messages;

/** @author Vladyslav Zhukovskii */
public interface GradleLocalizationConstant extends Messages {

    /** Analyze Project */

    @Key("analyze.project.action.text")
    String analyzeProjectActionText();

    @Key("analyze.project.action.description")
    String analyzeProjectActionDescription();

    @Key("analyze.project.executed")
    String analyzeProjectExecuted();

    @Key("analyze.project.execution.failed")
    String analyzeProjectExecutionFailed();

    @Key("analyze.project.execution.success")
    String analyzeProjectExecutionSuccess();

    @Key("analyze.project.failed")
    String analyzeProjectFailed();

    @Key("analyze.project.regeneration.executed")
    String analyzeProjectRegenerationExecuted();

    @Key("analyze.project.regeneration.execution.successful")
    String analyzeProjectRegenerationExecutionSuccessful();

    @Key("analyze.project.regeneration.execution.failed")
    String analyzeProjectRegenerationExecutionFailed();



    /** Custom Build */

    @Key("custom.build.action.text")
    String customBuildActionText();

    @Key("custom.build.action.description")
    String customBuildActionDescription();

    @Key("custom.build.view.caption")
    String customBuildViewCaption();

    @Key("custom.build.view.skip.test")
    String customBuildViewSkipTest();

    @Key("custom.build.view.refresh.dependencies")
    String customBuildViewRefreshDependencies();

    @Key("custom.build.view.offline")
    String customBuildViewOffline();



    /** Create New Module */

    @Key("create.module.action.text")
    String createModuleActionText();

    @Key("create.module.action.description")
    String createModuleActionDescription();

    @Key("create.module.failed")
    String createModuleFailed();

    @Key("create.module.view.cation")
    String createModuleViewCaption();

    @Key("create.module.view.new.module")
    String createModuleViewNewModule();



    /** Task List */

    @Key("show.tasks.action.text")
    String showTasksActionText();

    @Key("show.tasks.action.description")
    String showTasksActionDescription();

    @Key("show.tasks.title")
    String showTasksTitle();

    @Key("show.tasks.view.title")
    String showTasksViewTitle();

    @Key("show.tasks.please.wait.for.build")
    String showTasksPleasWaitForBuild();


    /** New Project Wizard */

    @Key("wizard.use.gradle.distribution")
    String wizardUseGradleDistribution();

    @Key("wizard.use.gradle.wrapper.version")
    String wizardUseGradleWrapperVersion();

    @Key("wizard.nothing.to.configure")
    String wizardNothingToConfigure();


    /** Commons */

    @Key("update.dependencies.action.text")
    String updateDependenciesActionText();

    @Key("update.dependencies.action.description")
    String updateDependenciesActionDescription();
}
