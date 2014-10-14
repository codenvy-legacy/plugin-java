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
package com.codenvy.ide.maven.tools;

import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.core.util.CommandLine;
import com.codenvy.api.core.util.LineConsumer;
import com.codenvy.api.core.util.ProcessUtil;
import com.codenvy.api.vfs.server.VirtualFile;
import com.google.common.io.ByteStreams;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A smattering of useful methods to work with the Maven POM.
 *
 * @author Artem Zatsarynnyy
 * @author andrew00x
 * @author Eugene Voevodin
 */
public class MavenUtils {
    public static final Pattern MAVEN_LOGGER_PREFIX_REMOVER = Pattern.compile("(\\[INFO\\]|\\[WARNING\\]|\\[DEBUG\\]|\\[ERROR\\])\\s+(.*)");
    @Inject
    @Named("packaging2file-extension")
    private static Map<String, String> packagingToFileExtensionMapping;
    /** Internal Maven POM reader. */
    private static MavenXpp3Reader pomReader = new MavenXpp3Reader();
    /** Internal Maven POM writer. */
    private static MavenXpp3Writer pomWriter = new MavenXpp3Writer();

    /** Not instantiable. */
    private MavenUtils() {
    }

    public static String removeLoggerPrefix(String origin) {
        final Matcher matcher = MAVEN_LOGGER_PREFIX_REMOVER.matcher(origin);
        if (matcher.matches()) {
            return origin.substring(matcher.start(2));
        }
        return origin;
    }

    /** Get file extension of artifact by packaging, e.g. <packaging>play</packaging> */
    public static String getFileExtensionByPackaging(String packaging) {
        if (packagingToFileExtensionMapping == null) {
            return null;
        }
        return packagingToFileExtensionMapping.get(packaging);
    }

    /**
     * Get description of maven project.
     *
     * @param sources
     *         maven project directory. Note: Must contains pom.xml file.
     * @return description of maven project
     * @throws IOException
     *         if an i/o error occurs
     */
    public static Model getModel(java.io.File sources) throws IOException {
        return doReadModel(new java.io.File(sources, "pom.xml"));
    }

    /**
     * Read description of maven project.
     *
     * @param pom
     *         path to pom.xml file
     * @return description of maven project
     * @throws IOException
     *         if an i/o error occurs
     */
    public static Model readModel(java.io.File pom) throws IOException {
        return doReadModel(pom);
    }

    /**
     * Read description of maven project.
     *
     * @param reader
     *         {@link Reader} to read content of pom.xml file.
     * @return description of maven project
     * @throws IOException
     *         if an i/o error occurs
     */
    public static Model readModel(Reader reader) throws IOException {
        try {
            return pomReader.read(reader, true);
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    /**
     * Read description of maven project.
     *
     * @param stream
     *         {@link InputStream} to read content of pom.xml file.
     * @return description of maven project
     * @throws IOException
     *         if an i/o error occurs
     */
    public static Model readModel(InputStream stream) throws IOException {
        try {
            return pomReader.read(stream, true);
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    /**
     * Read description of maven project.
     *
     * @param pom
     *         {@link VirtualFile} to read content of pom.xml file.
     * @return description of maven project
     * @throws IOException
     *         if an i/o error occurs
     * @throws ForbiddenException
     *         if {@code pom} isn't a file
     * @throws ServerException
     *         if other error occurs
     */
    public static Model readModel(VirtualFile pom) throws IOException, ForbiddenException, ServerException {
        try (InputStream stream = pom.getContent().getStream()) {
            return pomReader.read(stream, true);
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get description of maven project and all its modules if any as plain list.
     *
     * @param sources
     *         maven project directory. Note: Must contains pom.xml file.
     * @return description of maven project
     * @throws IOException
     *         if an i/o error occurs
     */
    public static List<Model> getModules(java.io.File sources) throws IOException {
        return getModules(getModel(sources));
    }

    public static List<Model> getModules(Model model) throws IOException {
        final List<Model> l = new LinkedList<>();
        addModules(model, l);
        return l;
    }

    private static void addModules(Model model, List<Model> l) throws IOException {
        if (model.getPackaging().equals("pom")) {
            for (String module : model.getModules()) {
                final java.io.File pom = new java.io.File(new java.io.File(model.getProjectDirectory(), module), "pom.xml");
                if (pom.exists()) {
                    final Model child = readModel(pom);
                    final Parent parent = newParent(model.getGroupId(), model.getArtifactId(), model.getVersion());
                    parent.setRelativePath(child.getProjectDirectory().toPath().relativize(model.getPomFile().toPath()).toString());
                    child.setParent(parent);
                    l.add(child);
                    addModules(child, l);
                }
            }
        }
    }

    /**
     * Writes a specified {@link Model} to the path from which this model has been read.
     *
     * @param model
     *         model to write
     * @throws IOException
     *         if an i/o error occurs
     * @throws IllegalStateException
     *         if method {@code model.getPomFile()} returns {@code null}
     */
    public static void writeModel(Model model) throws IOException {
        final java.io.File pom = model.getPomFile();
        if (pom == null) {
            throw new IllegalStateException("Unable to write a model. Unknown path.");
        }
        writeModel(model, pom);
    }

    /**
     * Writes a specified {@link Model} to the specified {@link java.io.File}.
     *
     * @param model
     *         model to write
     * @param pom
     *         path to the file to write a model
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void writeModel(Model model, java.io.File pom) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(pom.toPath(), Charset.forName("UTF-8"))) {
            writeModel(model, writer);
        }
    }

    /**
     * Writes a specified {@link Model} to the specified {@link OutputStream}.
     *
     * @param model
     *         model to write
     * @param output
     *         {@link OutputStream} to write a model
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void writeModel(Model model, OutputStream output) throws IOException {
        pomWriter.write(output, model);
    }

    /**
     * Writes a specified {@link Model} to the specified {@link Writer}.
     *
     * @param model
     *         model to write
     * @param output
     *         {@link Writer} to write a model
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void writeModel(Model model, Writer output) throws IOException {
        pomWriter.write(output, model);
    }

    /**
     * Writes a specified {@link Model} to the specified {@link VirtualFile}.
     *
     * @param model
     *         model to write
     * @param output
     *         {@link VirtualFile} to write a model
     * @throws IOException
     *         if an i/o error occurs
     * @throws ForbiddenException
     *         if {@code pom} isn't a file
     * @throws ServerException
     *         if other error occurs
     */
    public static void writeModel(Model model, VirtualFile output) throws IOException, ForbiddenException, ServerException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeModel(model, bout);
        output.updateContent(new ByteArrayInputStream(bout.toByteArray()), null);
    }

    /**
     * Add dependency to the specified pom.xml.
     *
     * @param pom
     *         pom.xml path
     * @param dependency
     *         POM of artifact to add as dependency
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void addDependency(java.io.File pom, Model dependency) throws IOException {
        addDependency(pom, dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), null);
    }

    /**
     * Add dependency to the specified pom.xml.
     *
     * @param pom
     *         pom.xml path
     * @param dependency
     *         POM of artifact to add as dependency
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void addDependency(java.io.File pom, Dependency dependency) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(addDependencies(pomBytes, dependency), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Add dependency to the specified pom.xml.
     *
     * @param pom
     *         pom.xml file
     * @param dependency
     *         POM of artifact to add as dependency
     * @throws IOException
     *         if an i/o error occurs
     * @throws ForbiddenException
     *         if {@code pom} isn't a file
     * @throws ServerException
     *         if other error occurs
     */
    public static void addDependency(VirtualFile pom, Dependency dependency) throws IOException, ForbiddenException, ServerException {
        addDependency(new java.io.File(pom.getPath()), dependency);
    }

    /**
     * Add set of dependencies to the specified pom.xml.
     *
     * @param pom
     *         pom.xml path
     * @param dependencies
     *         POM of artifact to add as dependency
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void addDependencies(java.io.File pom, Dependency... dependencies) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(addDependencies(pomBytes, dependencies), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Add set of dependencies to the specified pom.xml.
     *
     * @param pom
     *         pom.xml file
     * @param dependencies
     *         POM of artifact to add as dependency
     * @throws IOException
     *         if an i/o error occurs
     * @throws ForbiddenException
     *         if {@code pom} isn't a file
     * @throws ServerException
     *         if other error occurs
     */
    public static void addDependencies(VirtualFile pom, Dependency... dependencies) throws IOException,
                                                                                           ForbiddenException,
                                                                                           ServerException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(addDependencies(content, dependencies)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Add dependency to the specified pom.xml.
     *
     * @param pom
     *         pom.xml path
     * @param groupId
     *         groupId
     * @param artifactId
     *         artifactId
     * @param version
     *         artifact version
     * @throws IOException
     *         if an i/o error occurs
     */
    public static void addDependency(java.io.File pom, String groupId, String artifactId, String version, String scope) throws IOException {
        addDependency(pom, newDependency(groupId, artifactId, version, scope));
    }

    /**
     * Add dependency to the specified pom.xml.
     *
     * @param pom
     *         pom.xml file
     * @param groupId
     *         groupId
     * @param artifactId
     *         artifactId
     * @param version
     *         artifact version
     * @throws IOException
     *         if an i/o error occurs
     * @throws ForbiddenException
     *         if {@code pom} isn't a file
     * @throws ServerException
     *         if other error occurs
     */
    public static void addDependency(VirtualFile pom, String groupId, String artifactId, String version, String scope)
            throws IOException, ForbiddenException, ServerException {
        addDependency(pom, newDependency(groupId, artifactId, version, scope));
    }

    /**
     * Parses lines of maven output of command 'mvn dependency:list', e.g. com.codenvy.platform-api:codenvy-api-factory:jar:0.26.0:compile.
     * Maven dependency plugin sources: org.apache.maven.plugin.dependency.utils.DependencyStatusSets.getOutput(boolean, boolean, boolean)
     *
     * @param line
     *         raw line. Line may contain prefix '[INFO]'
     * @return parsed dependency model
     */
    public static MavenArtifact parseMavenArtifact(String line) {
        if (line != null) {
            final String[] segments = removeLoggerPrefix(line).split(":");
            if (segments.length >= 5) {
                final String groupId = segments[0];
                final String artifactId = segments[1];
                final String type = segments[2];
                final String classifier;
                final String version;
                final String scope;
                if (segments.length == 5) {
                    version = segments[3];
                    classifier = null;
                    scope = segments[4];
                } else {
                    version = segments[4];
                    classifier = segments[3];
                    scope = segments[5];
                }
                return new MavenArtifact(groupId, artifactId, type, classifier, version, scope);
            }
        }
        return null;
    }

    /**
     * Returns an execution command to launch Maven. If Maven home
     * environment variable isn't set then 'mvn' will be returned
     * since it's assumed that 'mvn' should be in PATH variable.
     *
     * @return an execution command to launch Maven
     */
    public static String getMavenExecCommand() {
        final java.io.File mvnHome = getMavenHome();
        if (mvnHome != null) {
            final String mvn = "bin" + java.io.File.separatorChar + "mvn";
            return new java.io.File(mvnHome, mvn).getAbsolutePath(); // use Maven home directory if it's set
        } else {
            return "mvn"; // otherwise 'mvn' should be in PATH variable
        }
    }

    /**
     * Returns Maven home directory.
     *
     * @return Maven home directory
     */
    public static java.io.File getMavenHome() {
        final String m2HomeEnv = System.getenv("M2_HOME");
        if (m2HomeEnv == null) {
            return null;
        }
        final java.io.File m2Home = new java.io.File(m2HomeEnv);
        return m2Home.exists() ? m2Home : null;
    }

    /** Get groupId of artifact. If artifact doesn't have groupId this method checks parent artifact for groupId. */
    public static String getGroupId(Model model) {
        String groupId = model.getGroupId();
        if (groupId == null) {
            final Parent parent = model.getParent();
            if (parent != null) {
                groupId = parent.getGroupId();
            }
        }
        return groupId;
    }

    /** Get version of artifact. If artifact doesn't have version this method checks parent artifact for version. */
    public static String getVersion(Model model) {
        String version = model.getVersion();
        if (version == null) {
            final Parent parent = model.getParent();
            if (parent != null) {
                version = parent.getVersion();
            }
        }
        return version;
    }

    /** Get source directories. */
    public static List<String> getSourceDirectories(Model model) {
        List<String> list = new LinkedList<>();
        Build build = model.getBuild();
        if (build != null) {
            if (build.getSourceDirectory() != null) {
                list.add(build.getSourceDirectory());
            } else if (build.getTestSourceDirectory() != null) {
                list.add(build.getTestSourceDirectory());
            }
        }
        if (list.isEmpty()) {
            list.add("src/main/java");
            list.add("src/test/java");
        }
        return list;
    }

    /** Get source directories. */
    public static List<String> getSourceDirectories(VirtualFile pom) throws ServerException, IOException, ForbiddenException {
        return getSourceDirectories(readModel(pom));
    }

    /** Get source directories. */
    public static List<String> getSourceDirectories(java.io.File pom) throws IOException {
        return getSourceDirectories(readModel(pom));
    }

    /** Get resource directories. */
    public static List<String> getResourceDirectories(Model model) {
        List<String> list = new LinkedList<>();
        Build build = model.getBuild();

        if (build != null) {
            if (build.getResources() != null && !build.getResources().isEmpty()) {
                for (Resource resource : build.getResources())
                    list.add(resource.getDirectory());
            }
        }
        if (list.isEmpty()) {
            list.add("src/main/resources");
            list.add("src/test/resources");
        }
        return list;
    }

    /** Get resource directories. */
    public static List<String> getResourceDirectories(VirtualFile pom) throws ServerException, IOException, ForbiddenException {
        return getResourceDirectories(readModel(pom));
    }

    /** Get resource directories. */
    public static List<String> getResourceDirectories(java.io.File pom) throws IOException {
        return getResourceDirectories(readModel(pom));
    }

    /** Creates new {@link Dependency} instance. */
    public static Dependency newDependency(String groupId, String artifactId, String version, String scope) {
        final Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setScope(scope);
        return dependency;
    }

    /** Creates new {@link Model} instance. */
    public static Model newModel(Parent parent, String groupId, String artifactId, String version, String packaging) {
        final Model model = new Model();
        model.setParent(parent);
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(packaging);
        return model;
    }

    /** Creates new {@link Parent} instance. */
    public static Parent newParent(String groupId, String artifactId, String version) {
        final Parent parent = new Parent();
        parent.setGroupId(groupId);
        parent.setArtifactId(artifactId);
        parent.setVersion(version);
        return parent;
    }

    private static Model doReadModel(java.io.File pom) throws IOException {
        final Model model;
        try (Reader reader = Files.newBufferedReader(pom.toPath(), Charset.forName("UTF-8"))) {
            model = pomReader.read(reader, true);
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
        model.setPomFile(pom);
        return model;
    }

    public static Map<String, String> getMavenVersionInformation() throws IOException {
        final Map<String, String> versionInfo = new HashMap<>();
        final LineConsumer cmdOutput = new LineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
                String key = null;
                int keyEnd = 0;
                int valueStart = 0;
                final int l = line.length();
                if (line.startsWith("Apache Maven")) {
                    key = "Maven version";
                } else {
                    while (keyEnd < l) {
                        if (line.charAt(keyEnd) == ':') {
                            valueStart = keyEnd + 1;
                            break;
                        }
                        keyEnd++;
                    }
                    if (keyEnd > 0) {
                        key = line.substring(0, keyEnd);
                    }
                }
                if (key != null) {
                    while (valueStart < l && Character.isWhitespace(line.charAt(valueStart))) {
                        valueStart++;
                    }
                    if ("Maven version".equals(key)) {
                        int valueEnd = valueStart;
                        // Don't show version details, e.g. (0728685237757ffbf44136acec0402957f723d9a; 2013-09-17 18:22:22+0300)
                        while (valueEnd < l && '(' != line.charAt(valueEnd)) {
                            valueEnd++;
                        }
                        final String value = line.substring(valueStart, valueEnd).trim();
                        versionInfo.put(key, value);
                    } else {
                        final String value = line.substring(valueStart);
                        versionInfo.put(key, value);
                    }
                }
            }

            @Override
            public void close() throws IOException {
            }
        };
        readMavenVersionInformation(cmdOutput);
        return versionInfo;
    }

    private static void readMavenVersionInformation(LineConsumer cmdOutput) throws IOException {
        final CommandLine commandLine = new CommandLine(getMavenExecCommand()).add("-version");
        final ProcessBuilder processBuilder = new ProcessBuilder().command(commandLine.toShellCommand()).redirectErrorStream(true);
        final Process process = processBuilder.start();
        ProcessUtil.process(process, cmdOutput, LineConsumer.DEV_NULL);
    }

    /** Checks is specified project is codenvy extension. */
    public static boolean isCodenvyExtensionProject(java.io.File workDir) throws IOException {
        return isCodenvyExtensionProject(MavenUtils.getModel(workDir));
    }

    public static boolean isCodenvyExtensionProject(Model pom) {
        for (Dependency dependency : pom.getDependencies()) {
            if ("codenvy-ide-api".equals(dependency.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set artifactId to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setArtifactId(VirtualFile pom, String artifactId) throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/artifactId", artifactId)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set artifactId to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setArtifactId(java.io.File pom, String artifactId) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/artifactId", artifactId), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Get artifactId of artifact
     */
    public static String getArtifactId(java.io.File pom) throws IOException {
        return readModel(pom).getArtifactId();
    }

    /**
     * Set groupId to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setGroupId(VirtualFile pom, String groupId) throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/groupId", groupId)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set groupId to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setGroupId(java.io.File pom, String groupId) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/groupId", groupId), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Get groupId of artifact. If artifact doesn't have groupId this method checks parent artifact for groupId.
     */
    public static String getGroupId(java.io.File pom) throws IOException {
        return getGroupId(readModel(pom));
    }


    /**
     * Set version to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setVersion(VirtualFile pom, String version) throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/version", version)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set version to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setVersion(java.io.File pom, String version) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/version", version), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Get artifact version
     */
    public static String getVersion(java.io.File pom) throws IOException {
        return getVersion(readModel(pom));
    }

    /**
     * Set packaging to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setPackaging(File pom, String packaging) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/packaging", packaging), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set packaging to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setPackaging(VirtualFile pom, String packaging) throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/packaging", packaging)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set parent artifact Id to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setParentArtifactId(File pom, String parentArtifactId) throws IOException {
        checkAndCreateParent(pom);
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/parent/artifactId", parentArtifactId), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set parent artifact Id to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setParentArtifactId(VirtualFile pom, String parentArtifactId)
            throws ServerException, ForbiddenException, IOException {
        checkAndCreateParent(pom);
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/parent/artifactId", parentArtifactId)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set parent group Id to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setParentGroupId(File pom, String parentGroupId) throws IOException {
        checkAndCreateParent(pom);
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/parent/groupId", parentGroupId), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set parent group Id to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setParentGroupId(VirtualFile pom, String parentGroupId) throws ServerException, ForbiddenException, IOException {
        checkAndCreateParent(pom);
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/parent/groupId", parentGroupId)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }


    /**
     * Set parent version to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setParentVersion(File pom, String parentVersion) throws IOException {
        checkAndCreateParent(pom);
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(setContent(pomBytes, "project/parent/version", parentVersion), pom);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Set parent version to artifact. Should be used to avoid pom.xml reformatting or destruction
     */
    public static void setParentVersion(VirtualFile pom, String parentVersion) throws ServerException, ForbiddenException, IOException {
        checkAndCreateParent(pom);
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(setContent(content, "project/parent/version", parentVersion)), null);
        } catch (XMLStreamException xmlEx) {
            throw new IOException(xmlEx);
        }
    }

    /**
     * Add maven module to modules list.
     *
     * @param pom
     *         the pom
     * @param moduleName
     *         the module name
     */
    public static void addModule(VirtualFile pom, String moduleName)
            throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(addTagValue(content, moduleName, "module", "modules", "project", "modules")), null);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Add maven module to modules list.
     *
     * @param pom
     *         the pom
     * @param moduleName
     *         the module name
     */
    public static void addModule(File pom, String moduleName)
            throws ServerException, ForbiddenException, IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(addTagValue(pomBytes, moduleName, "module", "modules", "project", "modules"), pom);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets maven source folder.
     *
     * @param pom
     *         the pom
     * @param srcPath
     *         the src path
     */
    public static void setSourceFolder(VirtualFile pom, String srcPath) throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(addTagValue(content, srcPath, "sourceDirectory", "build", "project", "build")),
                              null);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets maven source folder.
     *
     * @param pom
     *         the pom
     * @param srcPath
     *         the src path
     */
    public static void setSourceFolder(File pom, String srcPath) throws IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(addTagValue(pomBytes, srcPath, "sourceDirectory", "build", "project", "build"), pom);

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets test source folder.
     *
     * @param pom the pom
     * @param srcPath the src path
     */
    public static void setTestSourceFolder(VirtualFile pom, String srcPath) throws ServerException, ForbiddenException, IOException {
        byte[] content = new byte[(int)pom.getContent().getLength()];
        ByteStreams.readFully(pom.getContent().getStream(), content);
        try {
            pom.updateContent(new ByteArrayInputStream(addTagValue(content, srcPath, "testSourceDirectory", "build", "project", "build")),
                              null);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets test source folder.
     *
     * @param pom the pom
     * @param srcPath the src path
     */
    public static void setTestSourceFolder(File pom, String srcPath) throws ServerException, ForbiddenException, IOException {
        final byte[] pomBytes = com.google.common.io.Files.toByteArray(pom);
        try {
            com.google.common.io.Files.write(addTagValue(pomBytes, srcPath, "testSourceDirectory", "build", "project", "build"), pom);

        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static void checkAndCreateParent(VirtualFile pom) throws ServerException, IOException, ForbiddenException {
        Model model = readModel(pom);
        if (model.getParent() == null) {
            byte[] content = new byte[(int)pom.getContent().getLength()];
            ByteStreams.readFully(pom.getContent().getStream(), content);
            try {
                pom.updateContent(new ByteArrayInputStream(setContent(content, "project/parent", "\n")), null);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }
    }

    private static void checkAndCreateParent(File pom) throws IOException {
        Model model = readModel(pom);
        if (model.getParent() == null) {
            byte[] bytes = com.google.common.io.Files.toByteArray(pom);
            try {
                com.google.common.io.Files.write(setContent(bytes, "project/parent", "\n"), pom);
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }
    }

    private static byte[] addTagValue(byte[] source, String tagValue, String tagName, String parentTagName, String... tagPath)
            throws IOException, XMLStreamException {

        final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(source));
        final String[] currentPath = new String[tagPath.length];
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        boolean found = false;
        boolean applied = false;
        int level = 0;
        int instructionEnd = 0;
        while (!applied && reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (level < currentPath.length) {
                        currentPath[level] = reader.getLocalName();
                    }
                    ++level;
                    if (level == tagPath.length && Arrays.equals(tagPath, currentPath)) {
                        found = true;
                    }
                    instructionEnd = reader.getLocation().getCharacterOffset();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (found && level == tagPath.length && Arrays.equals(currentPath, tagPath)) {
                        result.write(source, 0, instructionEnd);
                        result.write(("\n        " + wrapInTag(tagName, tagValue)).getBytes());
                        result.write(source, instructionEnd, source.length - instructionEnd);
                        applied = true;
                    } else if (level == 1 && currentPath[0].equals("project")) {
                        result.write(source, 0, instructionEnd);
                        result.write(("\n    <" + parentTagName + ">\n").getBytes());
                        result.write(("        " + wrapInTag(tagName, tagValue)).getBytes());
                        result.write(("\n    </" + parentTagName + ">").getBytes());
                        result.write(source, instructionEnd, source.length - instructionEnd);
                        applied = true;
                    }
                    instructionEnd = reader.getLocation().getCharacterOffset();
                    --level;
                    break;
            }
        }
        return result.toByteArray();
    }

    private static byte[] addDependencies(byte[] source, Dependency... dependencies) throws IOException, XMLStreamException {
        if (dependencies.length == 0) {
            throw new IllegalArgumentException("At least one dependency required");
        }
        final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(source));
        final String[] dependenciesPath = new String[]{"project", "dependencies"};
        final String[] currentPath = new String[dependenciesPath.length];
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        boolean found = false;
        boolean applied = false;
        int level = 0;
        int instructionEnd = 0;
        while (!applied && reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (level < currentPath.length) {
                        currentPath[level] = reader.getLocalName();
                    }
                    ++level;
                    if (level == dependenciesPath.length && Arrays.equals(dependenciesPath, currentPath)) {
                        found = true;
                    }
                    instructionEnd = reader.getLocation().getCharacterOffset();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (found && level == dependenciesPath.length && Arrays.equals(currentPath, dependenciesPath)) {
                        result.write(source, 0, instructionEnd);
                        for (Dependency dependency : dependencies) {
                            result.write(toString(dependency).getBytes());
                        }
                        result.write(source, instructionEnd, source.length - instructionEnd);
                        applied = true;
                    } else if (level == 1 && currentPath[0].equals("project")) {
                        result.write(source, 0, instructionEnd);
                        result.write("\n    <dependencies>".getBytes());
                        for (Dependency dependency : dependencies) {
                            result.write(toString(dependency).getBytes());
                        }
                        result.write("\n    </dependencies>".getBytes());
                        result.write(source, instructionEnd, source.length - instructionEnd);
                        applied = true;
                    }
                    instructionEnd = reader.getLocation().getCharacterOffset();
                    --level;
                    break;
            }
        }
        return result.toByteArray();
    }

    private static byte[] setContent(byte[] source,
                                     String tagPath,
                                     String newContent) throws IOException, XMLStreamException {
        final String[] path = tagPath.split("/");
        final String[] parentPath = Arrays.copyOf(path, path.length - 1);
        final String[] currentPath = new String[parentPath.length];
        final String targetTag = path[path.length - 1];
        final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(source));
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        boolean found = false;
        boolean applied = false;
        int level = 0;
        int instructionEnd = 0;
        int textLength = 0;
        while (!applied && reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (level == parentPath.length && targetTag.equals(reader.getLocalName()) && Arrays.equals(parentPath, currentPath)) {
                        found = true;
                    } else if (level < currentPath.length) {
                        currentPath[level] = reader.getLocalName();
                    }

                    instructionEnd = reader.getLocation().getCharacterOffset();

                    // check character at offset
                    if (found) {
                        char currentChar = (char)source[instructionEnd - 1];
                        while (currentChar != '>' && instructionEnd > 0) {
                            instructionEnd--;
                            currentChar = (char)source[instructionEnd - 1];
                        }
                    }
                    ++level;
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (found) {
                        textLength = reader.getTextLength();
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (found) {
                        result.write(source, 0, instructionEnd);
                        result.write(newContent.getBytes());
                        final int offset = instructionEnd + textLength;
                        result.write(source, offset, source.length - offset);
                        applied = true;
                    } else if (level == parentPath.length && Arrays.equals(parentPath, currentPath)) {
                        // write the content of the source element before the element
                        result.write(source, 0, instructionEnd);
                        // indent
                        for (int i = 0; i < level * 4; ++i) {
                            result.write(' ');
                        }
                        // write the element by appending < and >
                        result.write(wrapInTag(targetTag, newContent).getBytes());
                        result.write('\n');
                        // we add everything else (starting from the remaining element
                        result.write(source, instructionEnd, source.length - instructionEnd);
                        applied = true;
                    }
                    instructionEnd = reader.getLocation().getCharacterOffset();
                    --level;
                    break;
            }
        }
        return result.toByteArray();
    }

    private static String wrapInTag(String tagName, String content) {
        return '<' + tagName + '>' + content + "</" + tagName + '>';
    }

    private static String toString(Dependency dependency) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n        <dependency>");
        if (dependency.getArtifactId() != null) {
            sb.append("\n            <artifactId>").append(dependency.getArtifactId()).append("</artifactId>");
        }
        if (dependency.getGroupId() != null) {
            sb.append("\n            <groupId>").append(dependency.getGroupId()).append("</groupId>");
        }
        if (dependency.getVersion() != null) {
            sb.append("\n            <version>").append(dependency.getVersion()).append("</version>");
        }
        if (dependency.getScope() != null) {
            sb.append("\n            <scope>").append(dependency.getScope()).append("</scope>");
        }
        sb.append("\n        </dependency>");
        return sb.toString();
    }
}
