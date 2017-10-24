/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.datalayer.DataLoaderMessages;
import uk.nhs.fhir.datalayer.FilesystemIF;
import uk.nhs.fhir.datalayer.SharedDataSource;
import uk.nhs.fhir.page.extensions.ExtensionsListRenderer;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.resourcehandlers.stu3.CodeSystemProvider;
import uk.nhs.fhir.resourcehandlers.stu3.ConceptMapProvider;
import uk.nhs.fhir.resourcehandlers.stu3.ImplementationGuideProvider;
import uk.nhs.fhir.resourcehandlers.stu3.OperationDefinitionProvider;
import uk.nhs.fhir.resourcehandlers.stu3.StructureDefinitionProvider;
import uk.nhs.fhir.resourcehandlers.stu3.ValueSetProvider;
import uk.nhs.fhir.servlethelpers.RawResourceRender;
import uk.nhs.fhir.servlethelpers.ServletStreamArtefact;
import uk.nhs.fhir.servlethelpers.ServletStreamExample;
import uk.nhs.fhir.util.FhirContexts;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

/**
 * This is effectively the core of a HAPI RESTFul server.
 *
 * We create a datastore in initialize method, which we pass to each ResourceProvider so that all resources can be persisted to/from the same datastore.
 *
 * @author Tim Coates, Adam Hatherly
 */
@WebServlet(urlPatterns = {"/STU3/*", "/3.0.1/*"}, displayName = "FHIR Servlet", loadOnStartup = 1)
public class STU3RestfulServlet extends RestfulServer {

    private static final Logger LOG = LoggerFactory.getLogger(STU3RestfulServlet.class.getName());
    private static final FhirVersion fhirVersion = FhirVersion.STU3;
    private static final long serialVersionUID = 1L;
    private static FilesystemIF dataSource = null;
    private static ResourceWebHandler webber = null;
    private static RawResourceRender myRawResourceRenderer = null;
    
    //private static String css = FileLoader.loadFileOnClasspath("/style.css");
    //private static String hl7css = FileLoader.loadFileOnClasspath("/hl7style.css");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("STU3 Requested URI: " + request.getRequestURI());

        String requestedPath = request.getRequestURI().substring(5);
        LOG.debug("Request path: " + requestedPath);
        
        if (requestedPath.startsWith("/artefact")) {
        	ServletStreamArtefact.streamArtefact(request, response, fhirVersion, dataSource);
        } else if (requestedPath.startsWith("/Examples/")) {
        	ServletStreamExample.streamExample(request, response, fhirVersion, dataSource, myRawResourceRenderer);
        } else if (requestedPath.startsWith("/Extensions")) {
        	ExtensionsListRenderer.loadExtensions(request, response, fhirVersion, webber);
        } else if (requestedPath.equals("/dataLoadStatusReport")) {
        	String profileLoadMessages = DataLoaderMessages.getProfileLoadMessages();
			ServletUtils.setResponseContentForSuccess(response, "text/plain", profileLoadMessages);
        } else {
            super.doGet(request, response);
        }
    }

    /**
     * This is where we start, called when our servlet is first initialised. For simplicity, we do the datastore setup once here.
     *
     *
     * @throws ServletException
     */
    @Override
    protected void initialize() throws ServletException {
    	
    	// Explicitly set this as an STU3 FHIR server
    	super.setFhirContext(FhirContexts.forVersion(FhirVersion.STU3));
        
        // We create an instance of our persistent layer (either MongoDB or
        // Filesystem), which we'll pass to each resource type handler as we create them
        dataSource = SharedDataSource.get();
        webber = new ResourceWebHandler(dataSource, fhirVersion);
        myRawResourceRenderer = new RawResourceRender(webber);
        
        // Pass our resource handler to the other servlets
        IndexServlet.setResourceHandler(webber);

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new StructureDefinitionProvider(dataSource));
        //resourceProviders.add(new PatientProvider(dataSource));
        //resourceProviders.add(new DocumentReferenceProvider(dataSource));
        //resourceProviders.add(new PractitionerProvider(dataSource));
        //resourceProviders.add(new OrganizationProvider(dataSource));
        //resourceProviders.add(new BundleProvider(dataSource));
        resourceProviders.add(new ValueSetProvider(dataSource));
        resourceProviders.add(new OperationDefinitionProvider(dataSource));
        resourceProviders.add(new ImplementationGuideProvider(dataSource));
        //resourceProviders.add(new ConformanceProvider(dataSource));
        resourceProviders.add(new CodeSystemProvider(dataSource));
        resourceProviders.add(new ConceptMapProvider(dataSource));
        setResourceProviders(resourceProviders);
        registerInterceptor(new STU3PlainContent(webber));
        registerInterceptor(new RedirectionInterceptor("3.0.1", "STU3"));
        LOG.debug("resourceProviders added");
        
        //setServerConformanceProvider(new CustomServerConformanceProvider());
        //LOG.fine("Custom Conformance provider added");
        
        FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
        pp.setDefaultPageSize(Integer.parseInt(FhirServerProperties.getProperty("defaultPageSize")));
        pp.setMaximumPageSize(Integer.parseInt(FhirServerProperties.getProperty("maximumPageSize")));
        setPagingProvider(pp);
    }
}
