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
package org.eclipse.che.gradle.analyzer.internal;

import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.annotations.Description;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.PrintWriter;

/**
 * @author Vladyslav Zhukovskyi
 */
@Description("Internal Gradle REST API")
@Path("internal/gradle")
public class SlaveAnalyzerService extends Service {

    @Inject
    Analyzer analyzer;

    @GenerateLink(rel = Constants.LINK_REL_RUN)
    @Path("run")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AnalysisProcessDescriptor run(@Description("Descriptor") AnalyzeRequest request) throws Exception {
        final AnalysisProcess process = analyzer.execute(request);

        return getDescriptor(process, getServiceContext());
    }

    @GET
    @Path("status/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalysisProcessDescriptor getStatus(@PathParam("id") Long id) throws Exception {
        final AnalysisProcess process = analyzer.getProcess(id);

        return getDescriptor(process, getServiceContext());
    }

    @POST
    @Path("stop/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AnalysisProcessDescriptor stop(@PathParam("id") Long id) throws Exception {
        final AnalysisProcess process = analyzer.getProcess(id);
        process.stop();

        return getDescriptor(process, getServiceContext());
    }

    @GET
    @Path("logs/{id}")
    public void getLogs(@PathParam("id") Long id,
                        @Context HttpServletResponse httpServletResponse) throws Exception {
        final AnalysisProcess process = analyzer.getProcess(id);
        final Throwable error = process.getError();

        if (error != null) {
            final PrintWriter output = httpServletResponse.getWriter();
            httpServletResponse.setContentType("text/plain");
            if (error instanceof AnalyzerException) {
                // expect ot have nice messages from our API
                output.write(error.getMessage());
            } else {
                error.printStackTrace(output);
            }
            output.flush();
        } else {
            final AnalysisLogger logger = process.getLogger();
            final PrintWriter output = httpServletResponse.getWriter();
            httpServletResponse.setContentType(logger.getContentType());
            logger.getLogs(output);
            output.flush();
        }
    }

    private AnalysisProcessDescriptor getDescriptor(AnalysisProcess process, ServiceContext restfulRequestContext) {

        //TODO
        return null;
    }
}
