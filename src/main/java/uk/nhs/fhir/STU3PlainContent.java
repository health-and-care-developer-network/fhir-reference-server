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
import static uk.nhs.fhir.enums.ResourceType.OPERATIONDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.STRUCTUREDEFINITION;
import static uk.nhs.fhir.enums.ResourceType.VALUESET;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.datalayer.collections.ExampleResources;
import uk.nhs.fhir.datalayer.collections.ResourceEntity;
import uk.nhs.fhir.datalayer.collections.ResourceEntityWithMultipleVersions;
import uk.nhs.fhir.datalayer.collections.SupportingArtefact;
import uk.nhs.fhir.datalayer.collections.VersionNumber;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.FHIRVersion;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.servlethelpers.RawResourceRender;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PageTemplateHelper;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates, Adam Hatherly
 */
public class STU3PlainContent extends CORSInterceptor {

    private static final Logger LOG = Logger.getLogger(STU3PlainContent.class.getName());
    private static final FHIRVersion fhirVersion = FHIRVersion.STU3;
    ResourceWebHandler myWebHandler = null;
    RawResourceRender myRawResourceRenderer = null;
    PageTemplateHelper templateHelper = null;
    private static String guidesPath = PropertyReader.getProperty("guidesPath");
    private static String templateDirectory = PropertyReader.getProperty("templateDirectory");

    public STU3PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        myRawResourceRenderer = new RawResourceRender(webber);
        templateHelper = new PageTemplateHelper();
        Velocity.init(PropertyReader.getProperties());
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
        if ((operation == READ || operation == VREAD) && resourceType == IMPLEMENTATIONGUIDE) {
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

        StringBuffer content = new StringBuffer();
        

        LOG.fine("FHIR Operation: " + operation);
        LOG.fine("Format to return to browser: " + mimeType.toString());
        
        boolean showList = true;
        String resourceName = null;
        
        if (operation != null) {
        	if (operation == READ || operation == VREAD) {
	        	if (mimeType == XML || mimeType == JSON) {
	        		resourceName = myRawResourceRenderer.renderSingleWrappedRAWResource(
	        										theRequestDetails, content, fhirVersion, resourceType, mimeType);
	        		showList = false;
	        	} else {
	        		resourceName = renderSingleResource(theRequestDetails, content, resourceType);
	        		showList = false;
	        	}
	        }
        }
        
        // We either don't have an operation, or we don't understand the operation, so
        // return a list of resources instead
        if (showList) {
        	content.append(renderListOfResources(theRequestDetails, resourceType));
        }

        String baseURL = theRequestDetails.getServerBaseForRequest();
        templateHelper.streamTemplatedHTMLresponse(theResponse, resourceType.toString(), resourceName, content, baseURL);
        
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
	    		StringBuffer content = new StringBuffer();
	    		renderConformance(content, theResponseObject, mimeType);
	    		String baseURL = theRequestDetails.getServerBaseForRequest();
	    		templateHelper.streamTemplatedHTMLresponse(theServletResponse, CONFORMANCE.toString(), null, content, baseURL);
	    		return false;
    		}
        }
        
		// Add the CORS header, and let HAPI handle the request
		addCORSResponseHeaders(theServletResponse);
		return true;
	}
    
    private void renderConformance(StringBuffer content, IBaseResource conformance, MimeType mimeType) {
    	LOG.fine("Attempting to render conformance statement");
    	String resourceContent = null;
    	if (mimeType == JSON) {
    		resourceContent = myRawResourceRenderer.getResourceAsJSON(conformance, new IdType(), fhirVersion);
    	} else {
    		resourceContent = myRawResourceRenderer.getResourceAsXML(conformance, new IdType(), fhirVersion);
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

        IdType resourceID = (IdType)theRequestDetails.getId();
        
        if (resourceType == STRUCTUREDEFINITION) {
            content.append(describeResource(resourceID, baseURL, context, "Snapshot", resourceType));
        }
        else if (resourceType == VALUESET) {
        	content.append(describeResource(resourceID, baseURL, context, "Entries", resourceType));
        }
        else if (resourceType == OPERATIONDEFINITION) {
        	content.append(describeResource(resourceID, baseURL, context, "Operation Description", resourceType));
        }
        else {
        	content.append(describeResource(resourceID, baseURL, context, "Description", resourceType));
        }
        
        // Return resource name (for breadcrumb)
        return myWebHandler.getResourceEntityByID(resourceID).getResourceName();
    }
    
    
    private String makeResourceURL(IdType resourceID, String baseURL) {
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
    private String describeResource(IdType resourceID, String baseURL, VelocityContext context, String firstTabName, ResourceType resourceType) {
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
    	
    	// NOTE: We no longer render the text section from a resource...
    	/*IResourceHelper helper = ResourceHelperFactory.getResourceHelper(fhirVersion, resourceType);
    	if (helper != null) {
	    	String textSection = helper.getTextSection(resource);
	    	context.put( "treeView", textSection );
    	}*/
    	
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
}
