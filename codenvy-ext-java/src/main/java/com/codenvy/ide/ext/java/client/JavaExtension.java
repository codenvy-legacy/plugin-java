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
import com.codenvy.ide.api.editor.EditorRegistry;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.filetypes.FileType;
import com.codenvy.ide.api.filetypes.FileTypeRegistry;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.ext.java.client.action.NewJavaSourceFileAction;
import com.codenvy.ide.ext.java.client.action.NewPackageAction;
import com.codenvy.ide.ext.java.client.editor.JavaEditorProvider;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import static com.codenvy.ide.api.action.IdeActions.GROUP_FILE_NEW;

/** @author Evgen Vidolob */
@Extension(title = "Java", version = "3.0.0")
public class JavaExtension {

    @Inject
    public JavaExtension(FileTypeRegistry fileTypeRegistry,
                         EditorRegistry editorRegistry,
                         JavaEditorProvider javaEditorProvider,
                         @Named("JavaFileType") FileType javaFile) {
        JavaResources.INSTANCE.css().ensureInjected();

        editorRegistry.registerDefaultEditor(javaFile, javaEditorProvider);
        fileTypeRegistry.registerFileType(javaFile);
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
                                NewJavaSourceFileAction newJavaSourceFileAction,
                                ActionManager actionManager) {
        // add actions in File -> New group
        actionManager.registerAction(localizationConstant.actionNewPackageId(), newPackageAction);
        actionManager.registerAction(localizationConstant.actionNewClassId(), newJavaSourceFileAction);
        DefaultActionGroup newGroup = (DefaultActionGroup)actionManager.getAction(GROUP_FILE_NEW);
        newGroup.addSeparator();
        newGroup.add(newJavaSourceFileAction);
        newGroup.add(newPackageAction);
    }

    @Inject
    private void registerIcons(IconRegistry iconRegistry, JavaResources resources) {
        // icons for project tree nodes
        iconRegistry.registerIcon(new Icon("java.package", resources.packageIcon()));
        iconRegistry.registerIcon(new Icon("java.sourceFolder", resources.sourceFolder()));
        // icons for project types
        iconRegistry.registerIcon(new Icon("maven.projecttype.big.icon", "java-extension/jar_64.png"));
        // icons for file extensions
        iconRegistry.registerIcon(new Icon("maven/java.file.small.icon", resources.javaFile()));
        iconRegistry.registerIcon(new Icon("maven/xml.file.small.icon", resources.xmlFile()));
        iconRegistry.registerIcon(new Icon("maven/css.file.small.icon", resources.cssFile()));
        iconRegistry.registerIcon(new Icon("maven/js.file.small.icon", resources.jsFile()));
        iconRegistry.registerIcon(new Icon("maven/json.file.small.icon", resources.jsonFile()));
        iconRegistry.registerIcon(new Icon("maven/html.file.small.icon", resources.htmlFile()));
        iconRegistry.registerIcon(new Icon("maven/jsp.file.small.icon", resources.jspFile()));
        iconRegistry.registerIcon(new Icon("maven/gif.file.small.icon", resources.imageIcon()));
        iconRegistry.registerIcon(new Icon("maven/jpg.file.small.icon", resources.imageIcon()));
        iconRegistry.registerIcon(new Icon("maven/png.file.small.icon", resources.imageIcon()));
        // icons for file names
        iconRegistry.registerIcon(new Icon("maven/pom.xml.file.small.icon", resources.maven()));
        // icon for category in Wizard
        iconRegistry.registerIcon(new Icon("java.samples.category.icon", resources.javaCategoryIcon()));
    }
}
