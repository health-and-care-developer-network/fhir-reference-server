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
import static uk.nhs.fhir.data.metadata.ResourceType.IMPLEMENTATIONGUIDE;
import static uk.nhs.fhir.enums.ClientType.BROWSER;
import static uk.nhs.fhir.enums.ClientType.NON_BROWSER;
import static uk.nhs.fhir.enums.MimeType.JSON;
import static uk.nhs.fhir.enums.MimeType.XML;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.datalayer.ResourceNameProvider;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.page.list.ResourceListRenderer;
import uk.nhs.fhir.page.rendered.ResourcePageRenderer;
import uk.nhs.fhir.page.searchresults.SearchResultsRenderer;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.servlethelpers.RawResourceRenderer;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.ServletUtils;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates, Adam Hatherly
 */
public class PlainContent extends CORSInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PlainContent.class.getName());
    private static String guidesPath = FhirServerProperties.getProperty("guidesPath");
    private static final FhirVersion fhirVersion = FhirVersion.DSTU2;
    
    private final ResourceWebHandler myWebHandler;
    private final ResourceNameProvider resourceNameProvider;
    private final RawResourceRenderer myRawResourceRenderer;
	private final ResourceListRenderer myResourceListRenderer;
	private final ResourcePageRenderer myResourcePageRenderer;
	private final SearchResultsRenderer mySearchResultsRenderer;
	

    public PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        myRawResourceRenderer = new RawResourceRenderer(webber);
        myResourceListRenderer = new ResourceListRenderer(myWebHandler);
        myResourcePageRenderer = new ResourcePageRenderer(myWebHandler);
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
        
        String typeInRequest = theRequestDetails.getResourceName();
    	LOG.debug("Detecting type of resource: " + typeInRequest);
    	ResourceType resourceType = ResourceType.getTypeFromHAPIName(typeInRequest);
        
        LOG.info("Request received - operation: " + operation.toString() + ", type: " + resourceType.toString());
        
        // First, check if this is a request for a markdown or text file from the ImplementationGuide directory..
        if (isReadImplementationGuideRequest(operation, resourceType)) {
        	String resourceName = theRequestDetails.getId().getIdPart();
        	
        	if (resourceName.endsWith(".md") 
        	  || resourceName.endsWith(".txt")) {
            	LOG.debug("Request for a file from the ImplementationGuide path: " + resourceName);
        		ServletUtils.setResponseContentForSuccess(theResponse, "text/plain", new File(guidesPath + File.separator + resourceName));
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
        if (operation != null) {
            if (operation == METADATA) {
            	return true;
            }
        }
        
        LOG.debug("FHIR Operation: " + operation);
        LOG.debug("Format to return to browser: " + mimeType.toString());

        String baseURL = theRequestDetails.getServerBaseForRequest();
        
        String content;
        if (READ.equals(operation) 
          || VREAD.equals(operation)) {
            String resourceName = resourceNameProvider.getNameForRequestedEntity(fhirVersion, theRequestDetails);
            IIdType resourceID = theRequestDetails.getId();
            
        	if (mimeType == XML 
        	  || mimeType == JSON) {
            	IBaseResource resource = myWebHandler.getResourceByID(fhirVersion, resourceID);
                content = myRawResourceRenderer.renderSingleWrappedRAWResourceWithoutText(resource, fhirVersion, resourceName, resourceType, baseURL, mimeType);
        	} else {
                content = myResourcePageRenderer.renderSingleResource(fhirVersion, baseURL, resourceID, resourceName, resourceType);
        	}
        } else {
            // We either don't have an operation, or we don't understand the operation, so
            // return a list of resources instead
        	
        	Map<String, String[]> params = theRequestDetails.getParameters();

        	if (params.containsKey("name")
        	  || params.containsKey("name:contains")) {
                content = mySearchResultsRenderer.renderSearchResults(fhirVersion, theRequestDetails, resourceType);
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
	    		LOG.debug("Attempting to render conformance statement");
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
<<<<<<< HEAD
=======
    
    private void renderConformance(StringBuffer content, IBaseResource conformance, MimeType mimeType) {
    	LOG.fine("Attempting to render conformance statement");
    	String resourceContent = null;
    	if (mimeType == JSON) {
    		resourceContent = myRawResourceRenderer.getResourceAsJSON(conformance, new IdDt(), fhirVersion);
    	} else {
    		resourceContent = myRawResourceRenderer.getResourceAsXML(conformance, new IdDt(), fhirVersion);
    	}
    	myRawResourceRenderer.renderSingleWrappedRAWResource(resourceContent, content, mimeType);
    }

    /**
     * Code used to display a single resource as HTML when requested by a
     * browser.
     *
     * @param theRequestDetails
     * @param content
     * @param resourceType
     */
    private String renderSingleResource(RequestDetails theRequestDetails, StringBuffer content, ResourceType resourceType) {

    	VelocityContext context = new VelocityContext();
    	
    	String baseURL = theRequestDetails.getServerBaseForRequest();

        IdDt resourceID = (IdDt)theRequestDetails.getId();
        
        if (resourceType == STRUCTUREDEFINITION) {
            content.append(describeResource(resourceID, baseURL, context, "Snapshot", resourceType));
        }
        if (resourceType == VALUESET) {
        	content.append(describeResource(resourceID, baseURL, context, "Entries", resourceType));
        }
        if (resourceType == OPERATIONDEFINITION) {
        	content.append(describeResource(resourceID, baseURL, context, "Operation Description", resourceType));
        }
        if (resourceType == IMPLEMENTATIONGUIDE) {
        	content.append(describeResource(resourceID, baseURL, context, "Description", resourceType));
        }
        if (resourceType == CODESYSTEM) {
        	content.append(describeResource(resourceID, baseURL, context, "Description", resourceType));
        }
        if (resourceType == CONCEPTMAP) {
        	content.append(describeResource(resourceID, baseURL, context, "Description", resourceType));
        }
        
        // Return resource name (for breadcrumb)
        return myWebHandler.getResourceEntityByID(resourceID).getResourceName();
    }
    
    
    private String makeResourceURL(IdDt resourceID, String baseURL) {
    	ResourceEntity entity = myWebHandler.getResourceEntityByID(resourceID);
    	return entity.getVersionedUrl(baseURL);
    }
    
    /**
     * Code in here to create the HTML response to a request for a
     * StructureDefinition we hold.
     *
     * @param resourceID Name of the SD we need to describe.
     * @return
     */
    private String describeResource(IdDt resourceID, String baseURL, VelocityContext context, String firstTabName, ResourceType resourceType) {
    	IBaseResource resource = myWebHandler.getResourceByID(resourceID);
    	
    	Template template = null;
    	try {
    	  template = Velocity.getTemplate(templateDirectory + "resource-with-metadata.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
    	// Values to insert into template
    	context.put( "resource", resource );
    	context.put( "type", resourceType );
    	context.put( "baseURL", baseURL );
    	context.put( "firstTabName", firstTabName );
    	context.put( "generatedurl", makeResourceURL(resourceID, baseURL) );
    	context.put( "fhirVersion", fhirVersion);
    	
    	// List of versions
    	ResourceEntityWithMultipleVersions entity = myWebHandler.getVersionsForID(resourceID);
    	HashMap<VersionNumber, ResourceEntity> list = entity.getVersionList();
    	context.put( "versions", list );
    	
    	// Resource metadata
    	ResourceEntity metadata = myWebHandler.getResourceEntityByID(resourceID);
    	context.put( "metadata", metadata );
    	
    	// Check if we have a nice metadata table from the renderer
    	boolean hasGeneratedMetadataFromRenderer = false;
    	for (SupportingArtefact artefact : metadata.getArtefacts()) {
    		if (artefact.getArtefactType().isMetadata()) {
    			hasGeneratedMetadataFromRenderer = true;
    			context.put( "metadataType", artefact.getArtefactType().name());
    		}
    	}
    	LOG.fine("Has metadata from renderer: " + hasGeneratedMetadataFromRenderer);
    	context.put( "hasGeneratedMetadataFromRenderer", hasGeneratedMetadataFromRenderer );
    	
    	// Tree view
    	String textSection = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType).getTextSection(resource);
    	context.put( "treeView", textSection );
    	
    	// Examples
    	ExampleResources examples = myWebHandler.getExamples(resourceType + "/" + resourceID.getIdPart());
    	if (examples != null) {
    		if (examples.size() > 0) {
    			context.put( "examples", examples );
    		}
    	}
    	
    	StringWriter sw = new StringWriter();
    	template.merge( context, sw );
    	return sw.toString();
    }
    
    /**
     * Code called to render a list of resources. for example in response to a
     * url like http://host/fhir/StructureDefinition
     *
     * @param theRequestDetails
     * @param content
     * @param resourceType
     */
    private String renderListOfResources(RequestDetails theRequestDetails, ResourceType resourceType) {
    	
    	VelocityContext context = new VelocityContext();
    	Template template = null;
    	String baseURL = theRequestDetails.getServerBaseForRequest();
    	
    	Map<String, String[]> params = theRequestDetails.getParameters();
    	
    	if (params.containsKey("name") || params.containsKey("name:contains")) {
            
    		// We are showing a list of matching resources for the specified name query
    		List<ResourceEntity> list = null;
    		
    		if (params.containsKey("name")) {
            	list = myWebHandler.getAllNames(resourceType, params.get("name")[0]);
            } else if (params.containsKey("name:contains")) {
            	list = myWebHandler.getAllNames(resourceType, params.get("name:contains")[0]);
            }

            try {
          	  template = Velocity.getTemplate(templateDirectory + "search-results.vm");
          	} catch( Exception e ) {
          		e.printStackTrace();
          	}
          	
          	// Put content into template
          	context.put( "list", list );
          	context.put( "resourceType", resourceType );
          	context.put( "baseURL", baseURL );
          	
          	StringWriter sw = new StringWriter();
          	template.merge( context, sw );
          	return sw.toString();
    		
        } else {
        	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
        	HashMap<String, List<ResourceEntity>> groupedResources = myWebHandler.getAGroupedListOfResources(resourceType);
        	
        	try {
        	  template = Velocity.getTemplate(templateDirectory + "list.vm");
        	} catch( Exception e ) {
        		e.printStackTrace();
        	}
        	
        	// Put content into template
        	context.put( "groupedResources", groupedResources );
        	context.put( "resourceType", resourceType );
        	context.put( "baseURL", baseURL );
        	
        	StringWriter sw = new StringWriter();
        	template.merge( context, sw );
        	return sw.toString();
        }
    }
>>>>>>> develop
}
