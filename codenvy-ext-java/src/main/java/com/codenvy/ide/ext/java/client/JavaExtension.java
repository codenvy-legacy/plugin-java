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
package com.codenvy.ide.ext.java.client;

import com.codenvy.ide.api.action.ActionManager;
import com.codenvy.ide.api.action.DefaultActionGroup;
import com.codenvy.ide.api.constraints.Constraints;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.filetypes.FileType;
import com.codenvy.ide.api.filetypes.FileTypeRegistry;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.api.keybinding.KeyBindingAgent;
import com.codenvy.ide.api.keybinding.KeyBuilder;
import com.codenvy.ide.ext.java.client.action.NewJavaSourceFileAction;
import com.codenvy.ide.ext.java.client.action.NewPackageAction;
import com.codenvy.ide.ext.java.client.action.OpenDeclarationAction;
import com.codenvy.ide.ext.java.client.action.QuickDocumentationAction;
import com.codenvy.ide.ext.java.shared.Constants;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import static com.codenvy.ide.api.action.IdeActions.GROUP_CODE;
import static com.codenvy.ide.api.action.IdeActions.GROUP_FILE_NEW;

/** @author Evgen Vidolob */
@Extension(title = "Java", version = "3.0.0")
public class JavaExtension {


    @Inject
    public JavaExtension(FileTypeRegistry fileTypeRegistry,
                         @Named("JavaFileType") FileType javaFile,
                         @Named("JspFileType") FileType jspFile) {
        JavaResources.INSTANCE.css().ensureInjected();

        fileTypeRegistry.registerFileType(javaFile);
        fileTypeRegistry.registerFileType(jspFile);
    }

    /** For test use only. */
    public JavaExtension() {
    }

    public static native String getJavaCAPath() /*-{
        try {
            return $wnd.IDE.config.javaCodeAssistant;
        } catch (e) {
            return null;
        }

    }-*/;

    @Inject
    private void prepareActions(JavaLocalizationConstant localizationConstant,
                                NewPackageAction newPackageAction,
                                KeyBindingAgent keyBinding,
                                NewJavaSourceFileAction newJavaSourceFileAction,
                                ActionManager actionManager,
                                QuickDocumentationAction quickDocumentationAction,
                                OpenDeclarationAction openDeclarationAction) {
        // add actions in File -> New group
        actionManager.registerAction(localizationConstant.actionNewPackageId(), newPackageAction);
        actionManager.registerAction(localizationConstant.actionNewClassId(), newJavaSourceFileAction);
        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.addSeparator();
        newGroup.add(newJavaSourceFileAction);
        newGroup.add(newPackageAction);

        actionManager.registerAction("showQuickDoc", quickDocumentationAction);
        actionManager.registerAction("openJavaDeclaration", openDeclarationAction);

        DefaultActionGroup codeGroup = (DefaultActionGroup)actionManager.getAction(GROUP_CODE);
        codeGroup.add(quickDocumentationAction, Constraints.LAST);
        codeGroup.add(openDeclarationAction, Constraints.LAST);

        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('q').build(), "showQuickDoc");
        keyBinding.getGlobal().addKey(new KeyBuilder().none().charCode(57358).build(), "openJavaDeclaration");
    }

    @Inject
    private void registerIcons(IconRegistry iconRegistry, JavaResources resources) {
        // icons for project tree nodes
        iconRegistry.registerIcon(new Icon("java.package", resources.packageIcon()));
        iconRegistry.registerIcon(new Icon("java.sourceFolder", resources.sourceFolder()));
        iconRegistry.registerIcon(new Icon("java.libraries", resources.librariesIcon()));
        iconRegistry.registerIcon(new Icon("java.jar", resources.jarIcon()));
        iconRegistry.registerIcon(new Icon("java.class", resources.javaClassIcon()));
        // icon for category in Wizard
        iconRegistry.registerIcon(new Icon(Constants.JAVA_CATEGORY + ".samples.category.icon", resources.javaCategoryIcon()));
    }
}
