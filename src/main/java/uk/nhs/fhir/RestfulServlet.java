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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import uk.nhs.fhir.datalayer.DataLoaderMessages;
import uk.nhs.fhir.datalayer.DataSourceFactory;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.resourcehandlers.BundleProvider;
import uk.nhs.fhir.resourcehandlers.DocumentReferenceProvider;
import uk.nhs.fhir.resourcehandlers.ImplementationGuideProvider;
import uk.nhs.fhir.resourcehandlers.OperationDefinitionProvider;
import uk.nhs.fhir.resourcehandlers.OrganizationProvider;
import uk.nhs.fhir.resourcehandlers.PatientProvider;
import uk.nhs.fhir.resourcehandlers.PractitionerProvider;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.resourcehandlers.StrutureDefinitionProvider;
import uk.nhs.fhir.resourcehandlers.ValueSetProvider;
import uk.nhs.fhir.util.PropertyReader;
import uk.nhs.fhir.util.ServletStreamArtefact;
import uk.nhs.fhir.util.ServletStreamRawFile;

/**
 * This is effectively the core of a HAPI RESTFul server.
 *
 * We create a datastore in initialize method, which we pass to each ResourceProvider so that all resources can be persisted to/from the same datastore.
 *
 * @author Tim Coates, Adam Hatherly
 */
@WebServlet(urlPatterns = {"/*"}, displayName = "FHIR Servlet", loadOnStartup = 1)
public class RestfulServlet extends RestfulServer {

    private static final Logger LOG = Logger.getLogger(RestfulServlet.class.getName());
    private static String logLevel = PropertyReader.getProperty("logLevel");
    private static final long serialVersionUID = 1L;
    Datasource dataSource = null;

    //private static String css = FileLoader.loadFileOnClasspath("/style.css");
    //private static String hl7css = FileLoader.loadFileOnClasspath("/hl7style.css");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.info("Requested URI: " + request.getRequestURI());

        if(request.getRequestURI().endsWith(".css")) {
            // Stylesheets
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "text/css", request.getRequestURI());
        } else if (request.getRequestURI().endsWith("favicon.ico")) {
        	// favicon.ico
        	ServletStreamRawFile.streamRawFileFromClasspath(response, "image/x-icon", PropertyReader.getProperty("faviconFile"));
        } else if (request.getRequestURI().startsWith("/images/") || request.getRequestURI().startsWith("/js/")) {
        	// Image and JS files
        	ServletStreamRawFile.streamRawFileFromClasspath(response, null, request.getRequestURI());
        } else if (request.getRequestURI().startsWith("/artefact")) {
        	ServletStreamArtefact.streamArtefact(request, response, dataSource);
        } else if (request.getRequestURI().equals("/dataLoadStatusReport")) {
	    	response.setStatus(200);
			response.setContentType("text/plain");
			PrintWriter outputStream = response.getWriter();
	        outputStream.write(DataLoaderMessages.getProfileLoadMessages());
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

        // We set our logging level based on the config file property.
        LOG.setLevel(Level.INFO);

        if(logLevel.equals("INFO")) {
           LOG.setLevel(Level.INFO);
        }
        if(logLevel.equals("FINE")) {
            LOG.setLevel(Level.FINE);
        }
        if(logLevel.equals("OFF")) {
            LOG.setLevel(Level.OFF);
        }
        
        // We create an instance of our persistent layer (either MongoDB or
        // Filesystem), which we'll pass to each resource type handler as we create them
        dataSource = DataSourceFactory.getDataSource();

        ResourceWebHandler webber = new ResourceWebHandler(dataSource);

        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new StrutureDefinitionProvider(dataSource));
        resourceProviders.add(new PatientProvider(dataSource));
        resourceProviders.add(new DocumentReferenceProvider(dataSource));
        resourceProviders.add(new PractitionerProvider(dataSource));
        resourceProviders.add(new OrganizationProvider(dataSource));
        resourceProviders.add(new BundleProvider(dataSource));
        resourceProviders.add(new ValueSetProvider(dataSource));
        resourceProviders.add(new OperationDefinitionProvider(dataSource));
        resourceProviders.add(new ImplementationGuideProvider(dataSource));
        setResourceProviders(resourceProviders);
        registerInterceptor(new PlainContent(webber));
        LOG.info("resourceProviders added");
        
        setServerConformanceProvider(new CustomServerConformanceProvider());
        LOG.info("Custom Conformance provider added");
    }
}
