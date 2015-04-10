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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.gwt.user.client.ui.ToggleButton;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.ext.java.jdi.shared.Location;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Provides methods which allow change view representation of debugger panel. Also the interface contains inner action delegate
 * interface which provides methods which allows react on user's actions.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public interface DebuggerView extends View<DebuggerView.ActionDelegate> {
    /** Needs for delegate some function into Debugger view. */
    public interface ActionDelegate extends BaseActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Resume button. */
        void onResumeButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Remove all breakpoints button. */
        void onRemoveAllBreakpointsButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Disconnect button. */
        void onDisconnectButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Step into button. */
        void onStepIntoButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Step over button. */
        void onStepOverButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Step return button. */
        void onStepReturnButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Change value button. */
        void onChangeValueButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the Evaluate expression button. */
        void onEvaluateExpressionButtonClicked();

        /** Performs any actions appropriate in response to the user having pressed the expand button in variables tree. */
        void onExpandVariablesTree();

        /**
         * Performs any actions appropriate in response to the user having selected variable in variables tree.
         *
         * @param variable
         *         variable that is selected
         */
        void onSelectedVariableElement(@Nonnull DebuggerVariable variable);
    }

    /**
     * Sets information about the execution point.
     *
     * @param absentInformation
     *         availability status for variables
     * @param location
     *         information about the execution point
     */
    public void setExecutionPoint(boolean absentInformation, @Nonnull Location location);

    /**
     * Sets variables.
     *
     * @param variables
     *         available variables
     */
    void setVariables(@Nonnull List<DebuggerVariable> variables);

    /**
     * Sets breakpoints.
     *
     * @param breakPoints
     *         available breakpoints
     */
    void setBreakpoints(@Nonnull List<Breakpoint> breakPoints);

    /**
     * Sets java virtual machine name and version.
     *
     * @param name
     *         virtual machine name
     */
    void setVMName(@Nonnull String name);

    /**
     * Sets whether Resume button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableResumeButton(boolean isEnable);

    /**
     * Sets whether Remove all breakpoints button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableRemoveAllBreakpointsButton(boolean isEnable);

    /**
     * Sets whether Disconnect button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableDisconnectButton(boolean isEnable);

    /**
     * Sets whether Step into button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableStepIntoButton(boolean isEnable);

    /** Change state for StepIntoButton. */
    boolean resetStepIntoButton(boolean state);

    /**
     * Sets whether Step over button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableStepOverButton(boolean isEnable);

    /** Change state for StepOverButton. */
    boolean resetStepOverButton(boolean state);

    /**
     * Sets whether Step return button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableStepReturnButton(boolean isEnable);

    /** Change  state for StepReturnButton. */
    boolean resetStepReturnButton(boolean state);

    /**
     * Sets whether Change value button is enabled.
     *
     * @param button
     *         the instance of button widget
     * @param state
     *         the new state of button
     */
    public boolean setButtonState(@Nonnull ToggleButton button, boolean state);

    /**
     * Sets whether Change value button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableChangeValueButtonEnable(boolean isEnable);

    /**
     * Sets whether Evaluate expression button is enabled.
     *
     * @param isEnable
     *         <code>true</code> to enable the button, <code>false</code> to disable it
     */
    void setEnableEvaluateExpressionButtonEnable(boolean isEnable);

    /**
     * Sets title.
     *
     * @param title
     *         title of view
     */
    void setTitle(@Nonnull String title);

    /** Update contents for selected variable. */
    void updateSelectedVariable();

    /**
     * Add elements into selected variable.
     *
     * @param variables
     *         variable what need to add into
     */
    void setVariablesIntoSelectedVariable(@Nonnull List<DebuggerVariable> variables);
}
