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

import static ca.uhn.fhir.rest.api.RestOperationTypeEnum.METADATA;
import static ca.uhn.fhir.rest.api.RestOperationTypeEnum.READ;
import static ca.uhn.fhir.rest.api.RestOperationTypeEnum.VREAD;
import static uk.nhs.fhir.enums.ClientType.BROWSER;
import static uk.nhs.fhir.enums.ClientType.NON_BROWSER;
import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.enums.MimeType.XML;
import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.datalayer.ResourceNameProvider;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.page.list.ResourceListRenderer;
import uk.nhs.fhir.page.rendered.ResourcePageRenderer;
import uk.nhs.fhir.page.searchresults.SearchResultsRenderer;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.servlethelpers.RawResourceRender;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.ServletUtils;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates, Adam Hatherly
 */
public class PlainContent extends CORSInterceptor {

    private static final Logger LOG = Logger.getLogger(PlainContent.class.getName());
    private static String guidesPath = FhirServerProperties.getProperty("guidesPath");
    private static final FHIRVersion fhirVersion = FHIRVersion.DSTU2;
    
    private final ResourceWebHandler myWebHandler;
    private final ResourceNameProvider resourceNameProvider;
    private final RawResourceRender myRawResourceRenderer;
	private final ResourceListRenderer myResourceListRenderer;
	private final ResourcePageRenderer myResourcePageRenderer;
	private final SearchResultsRenderer mySearchResultsRenderer;
	

    public PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        myRawResourceRenderer = new RawResourceRender(webber);
        myResourceListRenderer = new ResourceListRenderer(myWebHandler);
        myResourcePageRenderer = new ResourcePageRenderer(fhirVersion, myWebHandler);
        mySearchResultsRenderer = new SearchResultsRenderer(myWebHandler);
        
		this.resourceNameProvider = new ResourceNameProvider(myWebHandler); 
    }
    
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
    													HttpServletResponse theResponse) {
        
    	// First detect if this is a browser, and the mime type and operation requested
    	MimeType mimeType = MimeType.getTypeFromHeader(theRequest.getParameter("_format"));
        ClientType clientType = ClientType.getTypeFromHeaders(theRequest);
        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        ResourceType resourceType = ResourceType.getTypeFromRequest(theRequestDetails);
        
        LOG.info("Request received - operation: " + operation.toString() + ", type: " + resourceType.toString());
        
        // First, check if this is a request for a markdown or text file from the ImplementationGuide directory..
        if (isReadImplementationGuideRequest(operation, resourceType)) {
        	String resourceName = theRequestDetails.getId().getIdPart();
        	
        	if (resourceName.endsWith(".md") 
        	  || resourceName.endsWith(".txt")) {
            	LOG.fine("Request for a file from the ImplementationGuide path: " + resourceName);
        		ServletUtils.setResponseContentForSuccess(theResponse, "text/plain", new File(guidesPath + "/" + resourceName));
        		return false;
        	}
        }
        
        // If it is not a browser, let HAPI handle returning the resource
        if (clientType == NON_BROWSER) {
            return true;
        }

        LOG.fine("This appears to be a browser, generate some HTML to return.");
        
        // If they have asked for the conformance profile then let this one through - it
        // will be caught and handled by the outgoingResponse handler instead
        if (operation != null) {
            if (operation == METADATA) {
            	return true;
            }
        }
        
        LOG.fine("FHIR Operation: " + operation);
        LOG.fine("Format to return to browser: " + mimeType.toString());

        String baseURL = theRequestDetails.getServerBaseForRequest();
        
        String content;
        if (READ.equals(operation) 
          || VREAD.equals(operation)) {
            String resourceName = resourceNameProvider.getNameForRequestedEntity(theRequestDetails);
            IIdType resourceID = theRequestDetails.getId();
            
        	if (mimeType == XML || mimeType == JSON) {
            	IBaseResource resource = myWebHandler.getResourceByID(resourceID);
                content = myRawResourceRenderer.renderSingleWrappedRAWResourceWithoutText(resource, fhirVersion, resourceName, resourceType, baseURL, mimeType);
        	} else {
                content = myResourcePageRenderer.renderSingleResource(baseURL, resourceID, resourceName, resourceType);
        	}
        } else {
            // We either don't have an operation, or we don't understand the operation, so
            // return a list of resources instead
        	
        	Map<String, String[]> params = theRequestDetails.getParameters();

        	if (params.containsKey("name")
        	  || params.containsKey("name:contains")) {
                content = mySearchResultsRenderer.renderSearchResults(theRequestDetails, resourceType);
        	} else {
                content = myResourceListRenderer.renderResourceList(theRequestDetails, resourceType);
        	}
        }

        ServletUtils.setResponseContentForSuccess(theResponse, "text/html", content);
        return false;
    }
    
    private boolean isReadImplementationGuideRequest(RestOperationTypeEnum operation, ResourceType resourceType) {
    	return (operation.equals(READ) || operation.equals(VREAD)) 
    	  && resourceType.equals(IMPLEMENTATIONGUIDE);
	}
    
    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    	
    	// First detect if this is a browser, and the mime type and operation requested
    	MimeType mimeType = MimeType.getTypeFromHeader(theServletRequest.getParameter("_format"));
        ClientType clientType = ClientType.getTypeFromHeaders(theServletRequest);
        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        
        // If this is a request from a browser for the conformance resource, render and wrap it in HTML
        if (operation != null) {
        	if (operation == METADATA
        	  && clientType == BROWSER) {
	    		String baseURL = theRequestDetails.getServerBaseForRequest();
	    		LOG.fine("Attempting to render conformance statement");
	    		Optional<String> resourceName = Optional.empty();
	    		String renderedConformance = myRawResourceRenderer.renderSingleWrappedRAWResource(theResponseObject, fhirVersion, 
	    				resourceName, ResourceType.CONFORMANCE, baseURL, mimeType);
                
	    		ServletUtils.setResponseContentForSuccess(theServletResponse, "text/html", renderedConformance);
	    		return false;
    		}
        }
        
		// Add the CORS header, and let HAPI handle the request
		addCORSResponseHeaders(theServletResponse);
		return true;
	}
}
