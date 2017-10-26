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
import static uk.nhs.fhir.data.metadata.ResourceType.CONFORMANCE;
import static uk.nhs.fhir.data.metadata.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ClientType.BROWSER;
import static uk.nhs.fhir.enums.ClientType.NON_BROWSER;
import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.enums.MimeType.XML;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.SupportingArtefact;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.datalayer.ResourceNameProvider;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.page.list.ResourceListTemplate;
import uk.nhs.fhir.page.raw.RawResourceTemplate;
import uk.nhs.fhir.page.rendered.ResourceWithMetadataTemplate;
import uk.nhs.fhir.page.searchresults.SearchResultsTemplate;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.servlethelpers.RawResourceRender;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates, Adam Hatherly
 */
public class STU3PlainContent extends CORSInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(STU3PlainContent.class.getName());
    
    private static final String guidesPath = FhirServerProperties.getProperty("guidesPath");

    private static final FhirVersion fhirVersion = FhirVersion.STU3;
    
    private final ResourceWebHandler myWebHandler;
    private final ResourceNameProvider resourceNameProvider;
    private final RawResourceRender myRawResourceRenderer;

    public STU3PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        myRawResourceRenderer = new RawResourceRender(webber);

		this.resourceNameProvider = new ResourceNameProvider(myWebHandler); 
    }
    
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
    													HttpServletResponse theResponse) {
        
    	// First detect if this is a browser, and the mime type and operation requested
    	MimeType mimeType = MimeType.getTypeFromHeader(theRequest.getParameter("_format"));
        ClientType clientType = ClientType.getTypeFromHeaders(theRequest);
        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        
        String typeInRequest = theRequestDetails.getResourceName();
    	LOG.debug("Detecting type of resource: " + typeInRequest);
    	ResourceType resourceType = ResourceType.getTypeFromHAPIName(typeInRequest);
        
        LOG.info("Request received - operation: " + operation.toString() + ", type: " + resourceType.toString());
        
        // First, check if this is a request for a markdown or text file from the ImplementationGuide directory..
        if ((operation == READ || operation == VREAD)
          && resourceType == IMPLEMENTATIONGUIDE) {
        	String resourceName = theRequestDetails.getId().getIdPart();
        	if (resourceName.endsWith(".md") || resourceName.endsWith(".txt")) {
        		streamFileDirectly(theResponse, resourceName);
        		return false;
        	}
        }
        
        // If it is not a browser, let HAPI handle returning the resource
        if (clientType == NON_BROWSER) {
            return true;
        }

        LOG.debug("This appears to be a browser, generate some HTML to return.");
        
        // If they have asked for the conformance profile then let this one through - it
        // will be caught and handled by the outgoingResponse handler instead
        if (METADATA.equals(operation)) {
        	return true;
        }

        LOG.debug("FHIR Operation: " + operation);
        LOG.debug("Format to return to browser: " + mimeType.toString());
        
        String baseURL = theRequestDetails.getServerBaseForRequest();
        
        String wrappedContent;
        if (READ.equals(operation) 
          || VREAD.equals(operation)) {
            String resourceName = resourceNameProvider.getNameForRequestedEntity(theRequestDetails);
            
        	if (mimeType == XML || mimeType == JSON) {
                IIdType resourceID = theRequestDetails.getId();
            	IBaseResource resource = myWebHandler.getResourceByID(resourceID);
                wrappedContent = myRawResourceRenderer.renderSingleWrappedRAWResourceWithoutText(resource, fhirVersion, resourceName, resourceType, baseURL, mimeType);
                
        	} else {
                wrappedContent = renderSingleResource(theRequestDetails, resourceName, resourceType);
        	}
        } else {
            // We either don't have an operation, or we don't understand the operation, so
            // return a list of resources instead
        	
        	Map<String, String[]> params = theRequestDetails.getParameters();

        	if (params.containsKey("name")
        	  || params.containsKey("name:contains")) {
                wrappedContent = renderSearchResults(theRequestDetails, resourceType);
        	} else {
                wrappedContent = renderResourceList(theRequestDetails, resourceType);
        	}
        }

        ServletUtils.setResponseContentForSuccess(theResponse, "text/html", wrappedContent);
        return false;
    }
    
    private String renderSearchResults(RequestDetails theRequestDetails, ResourceType resourceType) {
		
		Map<String, String[]> params = theRequestDetails.getParameters();
		
		// We are showing a list of matching resources for the specified name query
		List<ResourceMetadata> list;
		if (params.containsKey("name")) {
        	list = myWebHandler.getAllNames(resourceType, params.get("name")[0]);
        } else if (params.containsKey("name:contains")) {
        	list = myWebHandler.getAllNames(resourceType, params.get("name:contains")[0]);
        } else {
        	throw new IllegalStateException("Expected name or name:contains to be present in params");
        }

        String baseURL = theRequestDetails.getServerBaseForRequest();

		return new SearchResultsTemplate(resourceType, baseURL, list).getHtml();
	}

	/**
     * e.g. http://host/fhir/StructureDefinition
     * @param theRequestDetails
     * @param resourceType
     * @return
     */
    private String renderResourceList(RequestDetails theRequestDetails, ResourceType resourceType) {
        String baseURL = theRequestDetails.getServerBaseForRequest();
    	
    	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
    	HashMap<String, List<ResourceMetadata>> groupedResources = myWebHandler.getAGroupedListOfResources(resourceType);
    	
    	return new ResourceListTemplate(resourceType, baseURL, groupedResources).getHtml();
	}
    
    /**
     * Method to stream a file directly from the guide directory to the client (for files referenced
     * in ImplementationGuide resources)
     * @param theResponse
     * @param filename
     */
    private void streamFileDirectly(HttpServletResponse theResponse, String filename) {
    	LOG.debug("Request for a file from the ImplementationGuide path: " + filename);
		ServletUtils.setResponseContentForSuccess(theResponse, "text/plain", new File(guidesPath + File.separator + filename));
    }
    
    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    	
    	// First detect if this is a browser, and the mime type and operation requested
    	MimeType mimeType = MimeType.getTypeFromHeader(theServletRequest.getParameter("_format"));
        ClientType clientType = ClientType.getTypeFromHeaders(theServletRequest);
        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        
        // If this is a request from a browser for the conformance resource, render and wrap it in HTML
        if (operation != null) {
        	if (operation == METADATA && clientType == BROWSER) {
	    		String baseURL = theRequestDetails.getServerBaseForRequest();
	    		String wrappedContent = renderConformance(theResponseObject, mimeType, baseURL);

	    		ServletUtils.setResponseContentForSuccess(theServletResponse, "text/html", wrappedContent);
	    		return false;
    		}
        }
        
		// Add the CORS header, and let HAPI handle the request
		addCORSResponseHeaders(theServletResponse);
		return true;
	}
    
    private String renderConformance(IBaseResource conformance, MimeType mimeType, String baseURL) {
    	LOG.debug("Attempting to render conformance statement");
    	String resourceContent = myRawResourceRenderer.getRawResource(conformance, mimeType, fhirVersion);
    	
    	return new RawResourceTemplate(Optional.of(CONFORMANCE.toString()), Optional.empty(), baseURL, resourceContent, mimeType).getHtml();
    }

    /**
     * Code used to display a single resource as HTML when requested by a
     * browser.
     *
     * @param theRequestDetails
     * @param content
     * @param resourceType
     */
    private String renderSingleResource(RequestDetails theRequestDetails, String resourceName, ResourceType resourceType) {
    	
    	String baseURL = theRequestDetails.getServerBaseForRequest();
        IdType resourceID = (IdType)theRequestDetails.getId();
        String firstTabName = getFirstTabName(resourceType);
        
        return describeResource(resourceName, resourceID, baseURL, firstTabName, resourceType);
    }
    
    private String getFirstTabName(ResourceType resourceType) {
    	switch (resourceType) {
	        case STRUCTUREDEFINITION:
	        	return "Snapshot";
	        case VALUESET:
	        	return "Entries";
	        case OPERATIONDEFINITION:
	        	return "Operation Description";
	    	default:
	    		return "Description";
    	}
	}

	/**
     * Code in here to create the HTML response to a request for a
     * StructureDefinition we hold.
     *
     * @param resourceID Name of the SD we need to describe.
     * @return
     */
    private String describeResource(String resourceName, IIdType resourceID, String baseURL, String firstTabName, ResourceType resourceType) {
    	IBaseResource resource = myWebHandler.getResourceByID(resourceID);

    	// List of versions
    	ResourceEntityWithMultipleVersions entity = myWebHandler.getVersionsForID(resourceID);
    	HashMap<VersionNumber, ResourceMetadata> versionsList = entity.getVersionList();

    	// Resource metadata
    	ResourceMetadata resourceMetadata = myWebHandler.getResourceEntityByID(resourceID);

    	// Check if we have a nice metadata table from the renderer
    	Optional<SupportingArtefact> metadataArtefact = 
			resourceMetadata.getArtefacts()
				.stream()
				.filter(artefact -> artefact.getArtefactType().isMetadata())
				.findAny();
    	LOG.debug("Has metadata from renderer: " + metadataArtefact.isPresent());

    	// Tree view
    	String textSection = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType).getTextSection(resource);

    	// Examples
    	ExampleResources examplesList = myWebHandler.getExamples(resourceType + "/" + resourceID.getIdPart());
    	Optional<ExampleResources> examples = 
    		(examplesList == null 
    		  || examplesList.isEmpty()) ? 
    			Optional.empty() : 
    			Optional.of(examplesList);
    	
    	return new ResourceWithMetadataTemplate(resourceType.toString(), resourceName, baseURL, resource, firstTabName,
    		versionsList, resourceMetadata, metadataArtefact, textSection, examples).getHtml();
    }
}
