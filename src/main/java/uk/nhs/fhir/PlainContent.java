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

import static ca.uhn.fhir.rest.api.RestOperationTypeEnum.*;
import static uk.nhs.fhir.enums.ClientType.*;
import static uk.nhs.fhir.enums.MimeType.*;
import static uk.nhs.fhir.enums.ResourceType.*;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;
import uk.nhs.fhir.enums.ClientType;
import uk.nhs.fhir.enums.MimeType;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.PageTemplateHelper;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates, Adam Hatherly
 */
public class PlainContent extends CORSInterceptor {

    private static final Logger LOG = Logger.getLogger(PlainContent.class.getName());
    ResourceWebHandler myWebHandler = null;
    PageTemplateHelper templateHelper = null;

    public PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        templateHelper = new PageTemplateHelper();
    }
    
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
    													HttpServletResponse theResponse) {
        
    	// First detect if this is a browser, and the mime type and operation requested
    	MimeType mimeType = MimeType.getTypeFromHeader(theRequest.getParameter("_format"));
        ClientType clientType = ClientType.getTypeFromHeaders(theRequest);
        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        ResourceType resourceType = ResourceType.getTypeFromRequest(theRequestDetails);
        
        LOG.info("Request received - operation: " + operation.toString());
        LOG.info("Resource type: " + resourceType.toString());
        
        // If it is not a browser, let HAPI handle returning the resource
        if (clientType == NON_BROWSER) {
            return true;
        }

        LOG.info("This appears to be a browser, generate some HTML to return.");
        
        // If they have asked for the conformance profile then let this one through - it
        // will be caught and handled by the outgoingResponse handler instead
        if (operation != null) {
            if (operation == METADATA) {
            	return true;
            }
        }

        StringBuffer content = new StringBuffer();
        

        LOG.info("FHIR Operation: " + operation);
        LOG.info("Format to return to browser: " + mimeType.toString());
        
        boolean showList = true;
        
        if (operation != null) {
        	if (operation == READ) {
	        	if (mimeType == XML || mimeType == JSON) {
	        		renderSingleWrappedRAWResource(theRequestDetails, content, resourceType, mimeType);
	        		showList = false;
	        	} else {
	        		renderSingleResource(theRequestDetails, content, resourceType);
	        		showList = false;
	        	}
	        }
        }
        
        // We either don't have an operation, or we don't understand the operation, so
        // return a list of resources instead
        if (showList) {
        	renderListOfResources(theRequestDetails, content, resourceType);
        }

        templateHelper.streamTemplatedHTMLresponse(theResponse, resourceType, content);
        
        return false;
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
	    		LOG.info(content.toString());
	    		templateHelper.streamTemplatedHTMLresponse(theServletResponse, CONFORMANCE, content);
	    		return false;
    		}
        }
        
		// Add the CORS header, and let HAPI handle the request
		addCORSResponseHeaders(theServletResponse);
		return true;
	}
    
    private void renderSingleWrappedRAWResource(RequestDetails theRequestDetails, StringBuffer content, ResourceType resourceType, MimeType mimeType) {
        content.append(GenerateIntroSection());
        String resourceName = theRequestDetails.getId().getIdPart();
        content.append(getResourceContent(resourceName, mimeType, resourceType));
        content.append("</div>");
    }
    
    private void renderConformance(StringBuffer content, IBaseResource conformance, MimeType mimeType) {
    	LOG.info("Attempting to render conformance statement");
    	content.append(GenerateIntroSection());
    	if (mimeType == JSON) {
    		content.append(getResourceAsJSON(conformance, null));
    	} else {
    		content.append(getResourceAsXML(conformance, null));
    	}
        content.append("</div>");
    }

    /**
     * Code used to display a single resource as HTML when requested by a
     * browser.
     *
     * @param theRequestDetails
     * @param content
     * @param resourceType
     */
    private void renderSingleResource(RequestDetails theRequestDetails, StringBuffer content, ResourceType resourceType) {

        content.append(GenerateIntroSection());

        String resourceName = theRequestDetails.getId().getIdPart();

        if (resourceType == STRUCTUREDEFINITION) {
            content.append(DescribeStructureDefinition(resourceName));
        }
        if (resourceType == VALUESET) {
            content.append(DescribeValueSet(resourceName));
        }
        if (resourceType == OPERATIONDEFINITION) {
        	content.append(DescribeOperationDefinition(resourceName));
        }
        
        //content.append(GetXMLContent(resourceName));
        
        content.append("</div>");
        
    }
    
    private String getResourceContent(String resourceName, MimeType mimeType, ResourceType resourceType) {
    	
    	IBaseResource resource = null;
    	
    	// Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
    	
    	if (resourceType == STRUCTUREDEFINITION) {
    		StructureDefinition sd = myWebHandler.getSDByName(resourceName);
    		sd.setText(textElement);
    		resource = sd;
    	} else if (resourceType == VALUESET) {
    		ValueSet vs = myWebHandler.getVSByName(resourceName);
    		vs.setText(textElement);
    		resource = vs;
    	} else if (resourceType == OPERATIONDEFINITION) {
     		OperationDefinition od = myWebHandler.getOperationByName(resourceName);
     		od.setText(textElement);
     		resource = od;
     	}
        
        if (mimeType == JSON) {
        	return getResourceAsJSON(resource, resourceName);
        } else {
        	return getResourceAsXML(resource, resourceName);
        }
    }
    
    private String getResourceAsXML(IBaseResource resource, String url) {
        // Serialise it to XML
        FhirContext ctx = FhirContext.forDstu2();
        String serialised = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resource);
        // Encode it for HTML output
        String xml = serialised.trim().replaceAll("<","&lt;").replaceAll(">","&gt;");
        // Wrap it in a div and pre tag
        StringBuffer out = new StringBuffer();
        if (url != null) {
        	out.append("<p><a href='./" + url + "'>Back to rendered view</a></p>");
        }
        out.append("<div class='rawXML'><pre lang='xml'>");
        out.append(xml);
        out.append("</pre></div>");
        return out.toString();
    }
    
    private String getResourceAsJSON(IBaseResource resource, String url) {
        // Serialise it to JSON
        FhirContext ctx = FhirContext.forDstu2();
        String serialised = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
        // Encode it for HTML output
        //String xml = serialised.trim().replaceAll("<","&lt;").replaceAll(">","&gt;");
        // Wrap it in a div and pre tag
        StringBuffer out = new StringBuffer();
        if (url != null) {
        	out.append("<p><a href='./" + url + "'>Back to rendered view</a></p>");
        }
        out.append("<div class='rawXML'><pre lang='json'>");
        out.append(serialised);
        out.append("</pre></div>");
        return out.toString();
    }

    /**
     * Code in here to create the HTML response to a request for a
     * StructureDefinition we hold.
     *
     * @param resourceName Name of the SD we need to describe.
     * @return
     */
    private String DescribeStructureDefinition(String resourceName) {
        StringBuilder content = new StringBuilder();
        StructureDefinition sd;
        sd = myWebHandler.getSDByName(resourceName);
        content.append("<h2 class='resourceType'>" + sd.getName() + " (StructureDefinition)</h2>");
        content.append("<div class='resourceSummary'>");
        content.append("<ul>");
        content.append("<li>URL: " + printIfNotNull(sd.getUrl()) + "</li>");
        content.append("<li>Version: " + printIfNotNull(sd.getVersion()) + "</li>");
        content.append("<li>Name: " + printIfNotNull(sd.getName()) + "</li>");
        content.append("<li>Publisher: " + printIfNotNull(sd.getPublisher()) + "</li>");
        content.append("<li>Description: " + printIfNotNull(sd.getDescription()) + "</li>");
        content.append("<li>Requirements: " + printIfNotNull(sd.getRequirements()) + "</li>");
        content.append("<li>Status: " + printIfNotNull(sd.getStatus()) + "</li>");
        content.append("<li>Experimental: " + printIfNotNull(sd.getExperimental()) + "</li>");
        content.append("<li>Date: " + printIfNotNull(sd.getDate()) + "</li>");
        content.append("<li>FHIRVersion: " + printIfNotNull(sd.getStructureFhirVersionEnum()) + "</li>");
        content.append("<li>Show Raw Profile: <a href='./" + resourceName + "?_format=xml'>XML</a>"
        		+ " | <a href='./" + resourceName + "?_format=json'>JSON</a></li>");
        content.append("</div>");
        String textSection = sd.getText().getDivAsString();
        if (textSection != null) {
	        content.append("<div class='treeView'>");
	        content.append(textSection);
	        content.append("</div>");
        }
        return content.toString();
    }


    /**
     * Code in here to create the HTML response to a request for a
     * StructureDefinition we hold.
     *
     * @param resourceName Name of the SD we need to describe.
     * @return
     */
    private String DescribeOperationDefinition(String resourceName) {
        StringBuilder content = new StringBuilder();
        OperationDefinition od;
        od = myWebHandler.getOperationByName(resourceName);
        content.append("<h2 class='resourceType'>" + od.getName() + " (OperationDefinition)</h2>");
        content.append("<div class='resourceSummary'>");
        content.append("<ul>");
        content.append("<li>URL: " + printIfNotNull(od.getUrl()) + "</li>");
        content.append("<li>Version: " + printIfNotNull(od.getVersion()) + "</li>");
        content.append("<li>Name: " + printIfNotNull(od.getName()) + "</li>");
        content.append("<li>Publisher: " + printIfNotNull(od.getPublisher()) + "</li>");
        content.append("<li>Description: " + printIfNotNull(od.getDescription()) + "</li>");
        content.append("<li>Requirements: " + printIfNotNull(od.getRequirements()) + "</li>");
        content.append("<li>Status: " + printIfNotNull(od.getStatus()) + "</li>");
        content.append("<li>Experimental: " + printIfNotNull(od.getExperimental()) + "</li>");
        content.append("<li>Date: " + printIfNotNull(od.getDate()) + "</li>");
        content.append("<li>FHIRVersion: " + printIfNotNull(od.getStructureFhirVersionEnum()) + "</li>");
        content.append("<li>Show Raw Profile: <a href='./" + resourceName + "?_format=xml'>XML</a>"
        		+ " | <a href='./" + resourceName + "?_format=json'>JSON</a></li>");
        content.append("</div>");
        String textSection = od.getText().getDivAsString();
        if (textSection != null) {
	        content.append("<div class='operationTable'>");
	        content.append(textSection);
	        content.append("</div>");
        }
        return content.toString();
    }
    
    /**
     * Code to generate a HTML view of the named ValueSet
     *
     * @param resourceName Named resource we need to describe.
     *
     * @return
     */
    private String DescribeValueSet(String resourceName) {
        StringBuilder content = new StringBuilder();
        ValueSet valSet;
        valSet = myWebHandler.getVSByName(resourceName);
        content.append("<h2 class='resourceType'>" + valSet.getName() + " (ValueSet)</h2>");
        content.append("<div class='resourceSummary'>");
        content.append("<ul>");
        content.append("<li>URL: " + printIfNotNull(valSet.getUrl()) + "</li>");
        content.append("<li>Version: " + printIfNotNull(valSet.getVersion()) + "</li>");
        content.append("<li>Name: " + printIfNotNull(valSet.getName()) + "</li>");
        content.append("<li>Publisher: " + printIfNotNull(valSet.getPublisher()) + "</li>");
        content.append("<li>Description: " + printIfNotNull(valSet.getDescription()) + "</li>");
        content.append("<li>Requirements: " + printIfNotNull(valSet.getRequirements()) + "</li>");
        content.append("<li>Status: " + printIfNotNull(valSet.getStatus()) + "</li>");
        content.append("<li>Experimental: " + printIfNotNull(valSet.getExperimental()) + "</li>");
        content.append("<li>Date: " + printIfNotNull(valSet.getDate()) + "</li>");
        content.append("<li>FHIRVersion: " + printIfNotNull(valSet.getStructureFhirVersionEnum()) + "</li>");
        content.append("<li>Show Raw ValueSet: <a href='./" + resourceName + "?_format=xml'>XML</a>"
        		+ " | <a href='./" + resourceName + "?_format=json'>JSON</a></li>");
        content.append("</div>");
        String textSection = valSet.getText().getDivAsString();
        if (textSection != null) {
	        content.append("<div class='treeView'>");
	        content.append(textSection);
	        content.append("</div>");
        }
        return content.toString();
    }

    /**
     * Code called to render a list of resources. for example in response to a
     * url like http://host/fhir/StructureDefinition
     *
     *
     * @param theRequestDetails
     * @param content
     * @param resourceType
     */
    private void renderListOfResources(RequestDetails theRequestDetails, StringBuffer content, ResourceType resourceType) {

        Map<String, String[]> params = theRequestDetails.getParameters();

        content.append(GenerateIntroSection());

        content.append("<h2 class='resourceType'>" + resourceType + " Resources</h2>");

        content.append("<ul>");
        if (params.containsKey("name") || params.containsKey("name:contains")) {
            if (params.containsKey("name")) {
                content.append(myWebHandler.getAllNames(resourceType, params.get("name")[0]));
            }
            if (params.containsKey("name:contains")) {
                content.append(myWebHandler.getAllNames(resourceType, params.get("name:contains")[0]));
            }
        } else {
            content.append(myWebHandler.getAGroupedListOfResources(resourceType));
        }
        content.append("</ul>");
        content.append("</div>");
    }

    /**
     * Simply encapsulates multiple repeated lines used to build the start of
     * the html section
     *
     * @return
     */
    private String GenerateIntroSection() {
        StringBuilder buffer = new StringBuilder();

        String fhirServerNotice = PropertyReader.getProperty("fhirServerNotice");
        String fhirServerWarning = PropertyReader.getProperty("fhirServerWarning");

        buffer.append("<div class='fhirServerGeneratedContent'>");
        buffer.append(fhirServerWarning);
        buffer.append(fhirServerNotice);
        return buffer.toString();
    }

    /**
     * Simple helper class to avoid errors caused by null values.
     *
     * @param input
     * @return
     */
    private static Object printIfNotNull(Object input) {
        return (input == null) ? "" : input;
    }
}
