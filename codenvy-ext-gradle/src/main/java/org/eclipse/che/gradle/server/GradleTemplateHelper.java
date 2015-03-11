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
package org.eclipse.che.gradle.server;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.util.StringUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.gradle.DistributionVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_BUILD_GRADLE;
import static org.eclipse.che.gradle.shared.GradleAttributes.GROOVY_MIME_TYPE;
import static org.eclipse.che.gradle.shared.GradleAttributes.DEFAULT_SETTINGS_GRADLE;

/**
 * Gradle build script template helper.
 * Perform generation various script needed for project generation.
 *
 * @author Vladyslav Zhukovskii
 */
public class GradleTemplateHelper {

    private static final String BUILD_GRADLE_TEMPLATE         = "/com/codenvy/ide/extension/gradle/templates/build.gradle.ft";
    private static final String BUILD_GRADLE_WRAPPER_TEMPLATE = "/com/codenvy/ide/extension/gradle/templates/build_wrapper.gradle.ft";
    private static final String SETTINGS_GRADLE_TEMPLATE      = "/com/codenvy/ide/extension/gradle/templates/settings.gradle.ft";

    private static GradleTemplateHelper instance;

    private GradleTemplateHelper() {
    }

    public static synchronized GradleTemplateHelper getInstance() {
        if (instance == null) {
            instance = new GradleTemplateHelper();
        }

        return instance;
    }

    public FileEntry createWrappedGradleBuildFile(Project project, DistributionVersion version)
            throws NotFoundException, ServerException, ForbiddenException, ConflictException {
        return createWrappedGradleBuildFile(project.getBaseFolder(), version);
    }

    public FileEntry createWrappedGradleBuildFile(FolderEntry folder, DistributionVersion version)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException {
        Map<String, Object> attributes = new HashMap<>(2);
        attributes.put("GRADLE_VERSION", version.getVersion());
        attributes.put("GRADLE_DIST_URL", version.getDistributionUrl());

        String template = prepareTemplate(BUILD_GRADLE_WRAPPER_TEMPLATE, attributes);
        return saveFile(folder, DEFAULT_BUILD_GRADLE, template, GROOVY_MIME_TYPE);
    }

    public FileEntry createGradleBuildFile(Project project) throws NotFoundException, ServerException, ForbiddenException,
                                                                   ConflictException {
        return createGradleBuildFile(project.getBaseFolder());
    }

    public FileEntry createGradleBuildFile(FolderEntry folder)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException {
        String template = prepareTemplate(BUILD_GRADLE_TEMPLATE, null);
        return saveFile(folder, DEFAULT_BUILD_GRADLE, template, GROOVY_MIME_TYPE);
    }

    public FileEntry createGradleSettingsFile(Project project, String rootProjectName, List<String> modules)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {
        return createGradleSettingsFile(project.getBaseFolder(), rootProjectName, modules);
    }

    public FileEntry createGradleSettingsFile(FolderEntry folder, String rootProjectName, List<String> modules)
            throws NotFoundException, ServerException, ConflictException, ForbiddenException {
        Map<String, Object> attributes = new HashMap<>(2);
        attributes.put("ROOT_PROJECT_NAME", rootProjectName);
        attributes.put("MODULES", modules);

        String template = prepareTemplate(SETTINGS_GRADLE_TEMPLATE, attributes);
        return saveFile(folder, DEFAULT_SETTINGS_GRADLE, template, GROOVY_MIME_TYPE);
    }

    private String prepareTemplate(String templateName, Map<String, Object> attributes) throws NotFoundException {
        try (InputStream in = GradleTemplateHelper.class.getResourceAsStream(templateName)) {
            String templateContent = IoUtil.readStream(in);

            if (attributes == null) {
                return templateContent;
            }

            VelocityContext context = new VelocityContext();
            context.put("StringUtils", StringUtils.class);

            for (Object o : attributes.keySet()) {
                String attrName = (String)o;
                context.put(attrName, attributes.get(attrName));
            }

            StringWriter writer = new StringWriter();

            Velocity.evaluate(context, writer, "", templateContent);

            return writer.toString();
        } catch (IOException e) {
            String message = String.format("Template '%s' doesn't exist.", templateName);
            throw new NotFoundException(message);
        }
    }

    private FileEntry saveFile(FolderEntry folder, String fileName, String content, String mediaType)
            throws ServerException, ForbiddenException, ConflictException {
        FileEntry fileEntry = (FileEntry)folder.getChild(fileName);

        try {
            if (fileEntry != null) {
                String oldContent = IoUtil.readAndCloseQuietly(fileEntry.getInputStream());
                String newContent = oldContent + "\n" + content;
                fileEntry.updateContent(newContent.getBytes());
            } else {
                fileEntry = folder.createFile(fileName, content.getBytes(), mediaType);
            }
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }

        return fileEntry;
    }
}
