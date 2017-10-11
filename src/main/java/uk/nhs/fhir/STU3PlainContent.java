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
import static uk.nhs.fhir.enums.ResourceType.CONFORMANCE;
import static uk.nhs.fhir.enums.ResourceType.IMPLEMENTATIONGUIDE;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.datalayer.ResourceNameProvider;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.html.RawResourceTemplate;
import uk.nhs.fhir.html.ResourceWithMetadataTemplate;
import uk.nhs.fhir.resourcehandlers.ResourceHelperFactory;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.servlethelpers.RawResourceRender;
import uk.nhs.fhir.util.FHIRVersion;
import uk.nhs.fhir.util.FhirServerProperties;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PageTemplateHelper;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates, Adam Hatherly
 */
public class STU3PlainContent extends CORSInterceptor {

    private static final Logger LOG = Logger.getLogger(STU3PlainContent.class.getName());
    private static final FHIRVersion fhirVersion = FHIRVersion.STU3;
    ResourceWebHandler myWebHandler = null;
    private final ResourceNameProvider resourceNameProvider;
    RawResourceRender myRawResourceRenderer = null;
    PageTemplateHelper templateHelper = null;
    private static String guidesPath = FhirServerProperties.getProperty("guidesPath");
    private static String templateDirectory = FhirServerProperties.getProperty("templateDirectory");

    public STU3PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        myRawResourceRenderer = new RawResourceRender(webber);
        templateHelper = new PageTemplateHelper();
        Velocity.init(FhirServerProperties.getProperties());

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
        
        String wrappedContent;
        if (READ.equals(operation) 
          || VREAD.equals(operation)) {
            String resourceName = resourceNameProvider.getNameForRequestedEntity(theRequestDetails);
            
        	if (mimeType == XML || mimeType == JSON) {
                IIdType resourceID = theRequestDetails.getId();
            	IBaseResource resource = myWebHandler.getResourceByID(resourceID);
                wrappedContent = myRawResourceRenderer.renderSingleWrappedRAWResource(resource, fhirVersion, resourceName, resourceType, baseURL, mimeType);
                
        	} else {
                wrappedContent = renderSingleResource(theRequestDetails, resourceName, resourceType);
        	}
        } else {
            // We either don't have an operation, or we don't understand the operation, so
            // return a list of resources instead
            StringBuilder content = new StringBuilder();
            content.append(renderListOfResources(theRequestDetails, resourceType));
            wrappedContent = templateHelper.wrapContentInTemplate(resourceType.toString(), null, content, baseURL);
        }

        templateHelper.setResponseTextualSuccess(theResponse, wrappedContent);
        return false;
    }
    
    /**
     * Method to stream a file directly from the guide directory to the client (for files referenced
     * in ImplementationGuide resources)
     * @param theResponse
     * @param filename
     */
    private void streamFileDirectly(HttpServletResponse theResponse, String filename) {
    	LOG.fine("Request for a file from the ImplementationGuide path: " + filename);
		try {
	    	// Initialise the output
	    	PrintWriter outputStream = null;
	        theResponse.setStatus(200);
	        theResponse.setContentType("text/plain");
			outputStream = theResponse.getWriter();
	        // Send the file directly to the output
	        String content = FileLoader.loadFile(guidesPath + "/" + filename);
			outputStream.append(content);
    	} catch (IOException e) {
    		LOG.severe(e.getMessage());
		}
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

	    		templateHelper.setResponseTextualSuccess(theServletResponse, wrappedContent);
	    		return false;
    		}
        }
        
		// Add the CORS header, and let HAPI handle the request
		addCORSResponseHeaders(theServletResponse);
		return true;
	}
    
    private String renderConformance(IBaseResource conformance, MimeType mimeType, String baseURL) {
    	LOG.fine("Attempting to render conformance statement");
    	String resourceContent = myRawResourceRenderer.getRawResource(conformance, mimeType, fhirVersion);
    	
    	return new RawResourceTemplate(Optional.empty(), Optional.of(CONFORMANCE.toString()), Optional.empty(), baseURL, resourceContent, mimeType).getHtml();
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
    	LOG.fine("Has metadata from renderer: " + metadataArtefact.isPresent());

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
    		List<ResourceMetadata> list = null;
    		
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
        	HashMap<String, List<ResourceMetadata>> groupedResources = myWebHandler.getAGroupedListOfResources(resourceType);
        	
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
}
