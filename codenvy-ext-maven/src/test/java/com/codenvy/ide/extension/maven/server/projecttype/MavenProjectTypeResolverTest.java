package com.codenvy.ide.extension.maven.server.projecttype;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.rest.ApiExceptionMapper;
import com.codenvy.api.project.server.*;
import com.codenvy.api.vfs.server.*;
import com.codenvy.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import com.codenvy.api.vfs.server.impl.memory.MemoryMountPoint;
import com.codenvy.api.vfs.server.search.SearcherProvider;
import com.codenvy.commons.user.UserImpl;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.*;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.junit.Test;
import org.junit.Assert;

import javax.ws.rs.core.Application;
import java.util.*;

/**
 * Created by .
 */
public class MavenProjectTypeResolverTest {


    private com.codenvy.commons.env.EnvironmentContext env;
    private ResourceLauncher         launcher;
    private static final String      workspace     = "my_ws";
    private static final String      vfsUser       = "dev";
    private static final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));

    @Test
    public void testName() throws Exception {
        ProjectTypeDescriptionRegistry ptdr = new ProjectTypeDescriptionRegistry("test");
        final EventService eventService = new EventService();
        VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
        ProjectManager pm = new DefaultProjectManager(ptdr, Collections.<ValueProviderFactory>emptySet(), vfsRegistry, eventService);


        final MemoryFileSystemProvider memoryFileSystemProvider =
                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
                    @Override
                    public VirtualFileSystemUser getVirtualFileSystemUser() {
                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
                    }
                }, vfsRegistry);
        MemoryMountPoint mmp = (MemoryMountPoint)memoryFileSystemProvider.getMountPoint(true);
        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);
        final ProjectType projectType = new ProjectType("maven", "maven", "my_category");
        ptdr.registerProjectType(projectType);

        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(ProjectManager.class, pm);
        dependencies.addComponent(SearcherProvider.class, mmp.getSearcherProvider());
        dependencies.addComponent(EventService.class, eventService);

        ResourceBinder resources = new ResourceBinderImpl();
        ProviderBinder providers = new ApplicationProviderBinder();
        EverrestProcessor processor = new EverrestProcessor(resources, providers, dependencies, new EverrestConfiguration(), null);
        launcher = new ResourceLauncher(processor);

        processor.addApplication(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return java.util.Collections.<Class<?>>singleton(ProjectService.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return new HashSet<>(Arrays.asList(new ContentStreamWriter(), new ApiExceptionMapper()));
            }
        });

        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, ProviderBinder.getInstance()));

        env = com.codenvy.commons.env.EnvironmentContext.getCurrent();
        env.setUser(new UserImpl(vfsUser, vfsUser, "dummy_token", vfsUserGroups, false));
        env.setWorkspaceName(workspace);
        env.setWorkspaceId(workspace);

        MavenProjectTypeResolver mavenProjectTypeResolver = new MavenProjectTypeResolver(pm);
        mmp.getRoot().createFolder("test");
        VirtualFileEntry test = pm.getProjectsRoot(workspace).getChild("test");
        boolean resolve = mavenProjectTypeResolver.resolve((FolderEntry) test);
        Assert.assertTrue(resolve);


    }
}
