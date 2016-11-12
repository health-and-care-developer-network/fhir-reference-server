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

import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.nhs.fhir.datalayer.DataSourceFactory;
import uk.nhs.fhir.datalayer.Datasource;
import uk.nhs.fhir.datalayer.MongoIF;
import uk.nhs.fhir.resourcehandlers.DocumentReferenceProvider;
import uk.nhs.fhir.resourcehandlers.OrganizationProvider;
import uk.nhs.fhir.resourcehandlers.PatientProvider;
import uk.nhs.fhir.resourcehandlers.PractitionerProvider;
import uk.nhs.fhir.resourcehandlers.ProfileWebHandler;
import uk.nhs.fhir.resourcehandlers.StrutureDefinitionProvider;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.validator.ValidatorManager;

/**
 * This is effectively the core of a HAPI RESTFul server.
 * 
 * We create a datastore in initialize method, which we pass to each ResourceProvider
 * so that all resources can be persisted to/from the same datastore.
 * 
 * @author Tim Coates
 */
@WebServlet(urlPatterns = {"/*"}, displayName = "FHIR Servlet", loadOnStartup = 1)
public class RestfulServlet extends RestfulServer {
    private static final Logger LOG = Logger.getLogger(RestfulServlet.class.getName());
    private static final long serialVersionUID = 1L;
    Datasource dataSource = null;
    ValidatorManager vManager = null;
	private static String css = FileLoader.loadFileOnClasspath("/style.css");
	
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
    	LOG.info("Requested URI: " + request.getRequestURI());
    	
    	if (request.getRequestURI().equals("/style.css")) {
    		// Special case processing for the stylesheet as we are grabbing all URLs
    		response.setStatus(200);
            response.setContentType("text/css");
    		PrintWriter outputStream = response.getWriter();
    		outputStream.append(css);
    	} else {    	
    		super.doGet(request, response);
    	}
	}


	/**
    * This is where we start, called when our servlet is first initialised.
    * For simplicity, we do the datastore setup once here.
    * 
    * 
    * @throws ServletException 
    */
    @Override
    protected void initialize() throws ServletException {
        
        // We create an instance of our persistent layer (either MongoDB or
    	// Filesystem), which we'll pass to each resource type handler as we create them
        dataSource = DataSourceFactory.getDataSource();
        
        // We also create a validatorManager which we'll also pass to each
        // resource type handler as we create them
        try {
            vManager = ValidatorManager.getInstance();
        } catch (Throwable ex) {
            LOG.severe(ex.getMessage());
        }
                
        ProfileWebHandler webber = new ProfileWebHandler(dataSource);
        
        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new StrutureDefinitionProvider(dataSource, vManager));
        resourceProviders.add(new PatientProvider(dataSource, vManager));
        resourceProviders.add(new DocumentReferenceProvider(dataSource, vManager));
        resourceProviders.add(new PractitionerProvider(dataSource, vManager));
        resourceProviders.add(new OrganizationProvider(dataSource, vManager));
        setResourceProviders(resourceProviders);
        registerInterceptor(new PlainContent(webber));
        LOG.info("resourceProviders added");
    }
}
