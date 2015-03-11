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
package org.eclipse.che.jdt;

import org.eclipse.che.api.builder.BuildStatus;
import org.eclipse.che.api.builder.dto.BuildOptions;
import org.eclipse.che.api.builder.dto.BuildTaskDescriptor;
import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.DownloadPlugin;
import org.eclipse.che.api.core.util.HttpDownloadPlugin;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.vfs.server.util.DeleteOnCloseFileInputStream;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.jdt.internal.core.JavaProject;
import org.eclipse.che.jdt.internal.core.SearchableEnvironment;
import org.eclipse.che.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.name.Named;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CodenvyCompilationUnitResolver;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Rest service for WorkerNameEnvironment
 * The name environment provides a callback API that the compiler can use to look up types, compilation units, and packages in the
 * current environment
 *
 * @author Evgen Vidolob
 */
@javax.ws.rs.Path("java-name-environment/{ws-id}")
public class RestNameEnvironment {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(RestNameEnvironment.class);

    @Inject
    private JavaProjectService javaProjectService;

    @Context
    private HttpServletRequest request;

    @Inject
    @Named("project.temp")
    private String temp;

    @PathParam("ws-id")
    @Inject
    private String wsId;

    @Inject
    @Named("api.endpoint")
    private String apiUrl;

    private DownloadPlugin downloadPlugin = new HttpDownloadPlugin();
    private ExecutorService executor;

    @PostConstruct
    public void start() {
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("-DependencyChecker-").setDaemon(true).build());
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @javax.ws.rs.Path("findTypeCompound")
    @SuppressWarnings("unchecked")
    public String findTypeCompound(@QueryParam("compoundTypeName") String compoundTypeName, @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();
        try {
            NameEnvironmentAnswer answer = environment.findType(getCharArrayFrom(compoundTypeName));
            if (answer == null && compoundTypeName.contains("$")) {
                String innerName = compoundTypeName.substring(compoundTypeName.indexOf('$') + 1, compoundTypeName.length());
                compoundTypeName = compoundTypeName.substring(0, compoundTypeName.indexOf('$'));
                answer = environment.findType(getCharArrayFrom(compoundTypeName));
                if (!answer.isCompilationUnit()) return null;
                ICompilationUnit compilationUnit = answer.getCompilationUnit();
                CompilationUnit result = getCompilationUnit(javaProject, environment, compilationUnit);
                AbstractTypeDeclaration o = (AbstractTypeDeclaration)result.types().get(0);
                ITypeBinding typeBinding = o.resolveBinding();

                for (ITypeBinding binding : typeBinding.getDeclaredTypes()) {
                    if (binding.getBinaryName().endsWith(innerName)) {
                        typeBinding = binding;
                        break;
                    }
                }
                Map<TypeBinding, ?> bindings = (Map<TypeBinding, ?>)result.getProperty("compilerBindingsToASTBindings");
                SourceTypeBinding binding = null;
                for (Map.Entry<TypeBinding, ?> entry : bindings.entrySet()) {
                    if (entry.getValue().equals(typeBinding)) {
                        binding = (SourceTypeBinding)entry.getKey();
                        break;
                    }
                }
                return TypeBindingConvector.toJsonBinaryType(binding);
            }

            return processAnswer(answer, javaProject, environment);
        } catch (JavaModelException e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Can't parse class: ", e);
            }
            throw new WebApplicationException();
        }
    }

    private JavaProject getJavaProject(String projectPath) {
        return javaProjectService.getOrCreateJavaProject(wsId, projectPath);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @javax.ws.rs.Path("findType")
    public String findType(@QueryParam("typename") String typeName, @QueryParam("packagename") String packageName,
                           @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();

        NameEnvironmentAnswer answer = environment.findType(typeName.toCharArray(), getCharArrayFrom(packageName));
        try {
            return processAnswer(answer, javaProject, environment);
        } catch (JavaModelException e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Can't parse class: ", e);
            }
            throw new WebApplicationException(e);
        }
    }

    @GET
    @javax.ws.rs.Path("package")
    @Produces("text/plain")
    public String isPackage(@QueryParam("packagename") String packageName, @QueryParam("parent") String parentPackageName,
                            @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();
        return String.valueOf(environment.isPackage(getCharArrayFrom(parentPackageName), packageName.toCharArray()));
    }

    @GET
    @Path("findPackages")
    @Produces(MediaType.APPLICATION_JSON)
    public String findPackages(@QueryParam("packagename") String packageName, @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();
        JsonSearchRequester requestor = new JsonSearchRequester();
        environment.findPackages(packageName.toCharArray(), requestor);
        return requestor.toJsonString();
    }

    @GET
    @javax.ws.rs.Path("findConstructor")
    @Produces(MediaType.APPLICATION_JSON)
    public String findConstructorDeclarations(@QueryParam("prefix") String prefix,
                                              @QueryParam("camelcase") boolean camelCaseMatch,
                                              @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();
        JsonSearchRequester searchRequester = new JsonSearchRequester();
        environment.findConstructorDeclarations(prefix.toCharArray(), camelCaseMatch, searchRequester, null);
        return searchRequester.toJsonString();
    }

    @GET
    @javax.ws.rs.Path("findTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public String findTypes(@QueryParam("qualifiedname") String qualifiedName, @QueryParam("findmembers") boolean findMembers,
                            @QueryParam("camelcase") boolean camelCaseMatch,
                            @QueryParam("searchfor") int searchFor,
                            @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();
        JsonSearchRequester searchRequester = new JsonSearchRequester();
        environment.findTypes(qualifiedName.toCharArray(), findMembers, camelCaseMatch, searchFor, searchRequester);
        return searchRequester.toJsonString();
    }

    @GET
    @javax.ws.rs.Path("findExactTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public String findExactTypes(@QueryParam("missingsimplename") String missingSimpleName, @QueryParam("findmembers") boolean findMembers,
                                 @QueryParam("searchfor") int searchFor,
                                 @QueryParam("projectpath") String projectPath) {
        JavaProject javaProject = getJavaProject(projectPath);
        SearchableEnvironment environment = javaProject.getNameEnvironment();
        JsonSearchRequester searchRequester = new JsonSearchRequester();
        environment.findExactTypes(missingSimpleName.toCharArray(), findMembers, searchFor, searchRequester);
        return searchRequester.toJsonString();
    }

    //TODO in future it is strongly recommended to rewrite mechanism of updating dependencies!

    @POST
    @Path("/dependency/binaries")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public BuildTaskDescriptor updateBinaryDependency(@QueryParam("project") final String projectPath,
                                                      @QueryParam("module") final String modulePath,
                                                      @QueryParam("force") boolean force,
                                                      BuildOptions buildOptions)
            throws ForbiddenException, IOException, ConflictException, ServerException, UnauthorizedException,
                   NotFoundException {
        if (javaProjectService.isProjectDependencyExist(wsId, modulePath) && !force) {
            BuildTaskDescriptor descriptor = DtoFactory.getInstance().createDto(BuildTaskDescriptor.class);
            descriptor.setStatus(BuildStatus.SUCCESSFUL);
            return descriptor;
        }

        BuildOptions dtoBuildOptions = DtoFactory.getInstance().createDto(BuildOptions.class)
                                                 .withOptions(buildOptions.getOptions())
                                                 .withBuilderName(buildOptions.getBuilderName())
                                                 .withIncludeDependencies(buildOptions.isIncludeDependencies())
                                                 .withSkipTest(buildOptions.isSkipTest())
                                                 .withTargets(buildOptions.getTargets());

        final BuildTaskDescriptor taskDescriptor = HttpJsonHelper.request(BuildTaskDescriptor.class,
                                                                          String.format("%s/builder/%s/dependencies", apiUrl, wsId),
                                                                          "POST",
                                                                          dtoBuildOptions,
                                                                          Pair.of("project", projectPath));

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BuildTaskDescriptor finishedBuildStatus = waitTaskFinish(taskDescriptor);
                    if (finishedBuildStatus.getStatus() != BuildStatus.SUCCESSFUL) {
                        LOG.info("Something with user project, received build status {}.", finishedBuildStatus.getStatus());
                        return;
                    }

                    javaProjectService.removeProject(wsId, modulePath);
                    File projectDepDir = new File(temp, wsId + modulePath);
                    if (!projectDepDir.mkdirs()) {
                        LOG.info("Directory {} already exists.", projectDepDir.getPath());
                    }

                    Link downloadLink = LinksHelper.findLink("download result", finishedBuildStatus.getLinks());
                    if (downloadLink != null) {
                        File zip = new File(projectDepDir, "dependencies.zip");
                        downloadPlugin.download(downloadLink.getHref(), zip.getParentFile(), zip.getName(), true);
                        ZipUtils.unzip(new DeleteOnCloseFileInputStream(zip), projectDepDir);
                    }

                    //create JavaProject adn put it into cache
                    javaProjectService.getOrCreateJavaProject(wsId, projectPath);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });

        return taskDescriptor;
    }

    @POST
    @Path("/dependency/sources")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public BuildTaskDescriptor updateSourceDependency(@QueryParam("project") final String projectPath,
                                                      @QueryParam("module") final String modulePath,
                                                      BuildOptions buildOptions)
            throws ForbiddenException, IOException, ConflictException, NotFoundException, ServerException, UnauthorizedException {

        BuildOptions dtoBuildOptions = DtoFactory.getInstance().createDto(BuildOptions.class)
                                                 .withOptions(buildOptions.getOptions())
                                                 .withBuilderName(buildOptions.getBuilderName())
                                                 .withIncludeDependencies(buildOptions.isIncludeDependencies())
                                                 .withSkipTest(buildOptions.isSkipTest())
                                                 .withTargets(buildOptions.getTargets());

        final BuildTaskDescriptor taskDescriptor = HttpJsonHelper.request(BuildTaskDescriptor.class,
                                                                          String.format("%s/builder/%s/dependencies", apiUrl, wsId),
                                                                          "POST",
                                                                          dtoBuildOptions,
                                                                          Pair.of("project", projectPath));

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BuildTaskDescriptor finishedBuildStatus = waitTaskFinish(taskDescriptor);
                    if (finishedBuildStatus.getStatus() != BuildStatus.SUCCESSFUL) {
                        LOG.info("Something with user project, received build status {}.", finishedBuildStatus.getStatus());
                        return;
                    }
                    File projectDepDir = new File(temp, wsId + modulePath);
                    if (!projectDepDir.mkdirs()) {
                        LOG.info("Directory {} already exists.", projectDepDir.getPath());
                    }

                    File projectSourcesJars = new File(projectDepDir, "sources");
                    if (!projectSourcesJars.mkdirs()) {
                        LOG.info("Directory {} already exists.", projectSourcesJars.getPath());
                    }

                    Link downloadLink = LinksHelper.findLink("download result", finishedBuildStatus.getLinks());
                    if (downloadLink != null) {
                        File zip = new File(projectSourcesJars, "sources.zip");
                        downloadPlugin.download(downloadLink.getHref(), zip.getParentFile(), zip.getName(), true);
                        ZipUtils.unzip(new DeleteOnCloseFileInputStream(zip), projectSourcesJars);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });

        return taskDescriptor;
    }

    @Nonnull
    private BuildTaskDescriptor waitTaskFinish(@Nonnull BuildTaskDescriptor buildDescription) throws Exception {
        BuildTaskDescriptor request = buildDescription;
        final int sleepTime = 500;

        Link statusLink = LinksHelper.findLink("get status", buildDescription.getLinks());

        if (statusLink != null) {
            while (request.getStatus() == BuildStatus.IN_PROGRESS || request.getStatus() == BuildStatus.IN_QUEUE) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
                request = HttpJsonHelper.request(BuildTaskDescriptor.class, statusLink);
            }
        }

        return request;
    }

    private String processAnswer(NameEnvironmentAnswer answer, IJavaProject project, INameEnvironment environment)
            throws JavaModelException {
        if (answer == null) return null;
        if (answer.isBinaryType()) {
            IBinaryType binaryType = answer.getBinaryType();
            return BinaryTypeConvector.toJsonBinaryType(binaryType);
        } else if (answer.isCompilationUnit()) {
            ICompilationUnit compilationUnit = answer.getCompilationUnit();
            return getSourceTypeInfo(project, environment, compilationUnit);
        } else if (answer.isSourceType()) {
            ISourceType[] sourceTypes = answer.getSourceTypes();
            if (sourceTypes.length == 1) {
                ISourceType sourceType = sourceTypes[0];
                SourceTypeElementInfo elementInfo = (SourceTypeElementInfo)sourceType;
                IType handle = elementInfo.getHandle();
                org.eclipse.jdt.core.ICompilationUnit unit = handle.getCompilationUnit();
                return getSourceTypeInfo(project, environment, (ICompilationUnit)unit);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getSourceTypeInfo(IJavaProject project, INameEnvironment environment, ICompilationUnit compilationUnit)
            throws JavaModelException {
        CompilationUnit result = getCompilationUnit(project, environment, compilationUnit);

        BindingASTVisitor visitor = new BindingASTVisitor();
        result.accept(visitor);
        Map<TypeBinding, ?> bindings = (Map<TypeBinding, ?>)result.getProperty("compilerBindingsToASTBindings");
        SourceTypeBinding binding = null;
        for (Map.Entry<TypeBinding, ?> entry : bindings.entrySet()) {
            if (entry.getValue().equals(visitor.typeBinding)) {
                binding = (SourceTypeBinding)entry.getKey();
                break;
            }
        }
        if (binding == null) return null;
        return TypeBindingConvector.toJsonBinaryType(binding);
    }

    private CompilationUnit getCompilationUnit(IJavaProject project, INameEnvironment environment,
                                               ICompilationUnit compilationUnit) throws JavaModelException {
        int flags = 0;
        flags |= org.eclipse.jdt.core.ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
        flags |= org.eclipse.jdt.core.ICompilationUnit.IGNORE_METHOD_BODIES;
        flags |= org.eclipse.jdt.core.ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
        HashMap<String, String> opts = new HashMap<>(javaProjectService.getOptions());
        CompilationUnitDeclaration compilationUnitDeclaration =
                CodenvyCompilationUnitResolver.resolve(compilationUnit, project, environment, opts, flags, null);
        return CodenvyCompilationUnitResolver.convert(
                compilationUnitDeclaration,
                compilationUnit.getContents(),
                flags, opts);
    }

    private char[][] getCharArrayFrom(String list) {
        if (list.isEmpty()) {
            return null;
        }
        String[] strings = list.split(",");
        char[][] arr = new char[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            arr[i] = s.toCharArray();
        }
        return arr;
    }
}
