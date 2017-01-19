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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.TagList;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.rest.server.provider.dstu2.ServerConformanceProvider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

/**
 * Class used to generate html content when a request comes from a browser.
 *
 * @author Tim Coates
 */
public class PlainContent extends InterceptorAdapter {

    private static final Logger LOG = Logger.getLogger(PlainContent.class.getName());
    ResourceWebHandler myWebHandler = null;
    private String SDtemplate = null;
    private String VStemplate = null;
    private String ServerConformanceTemplate = null;

    public PlainContent(ResourceWebHandler webber) {
        myWebHandler = webber;
        SDtemplate = FileLoader.loadFileOnClasspath("/template/profiles.html");
        VStemplate = FileLoader.loadFileOnClasspath("/template/valuesets.html");
        ServerConformanceTemplate = FileLoader.loadFileOnClasspath("/template/serverconformance.html");
    }

    protected boolean isBrowser(HttpServletRequest theRequest) {
    	String mimes = theRequest.getHeader("accept");
    	// Check if this has come from a browser
        if (mimes == null) {
            LOG.info("No accept header set, assume a non-browser client.");
            return false;
        } else {
            if (mimes.contains("html") == false) {
                LOG.info("Accept header set, but without html, so assume a non-browser client.");
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
    													HttpServletResponse theResponse) {
        

        String format = theRequest.getParameter("_format");
        
        if (!isBrowser(theRequest)) {
            return true;
        }

        LOG.info("This appears to be a browser, generate some HTML to return.");
        
        // We don't have any rendered version of the conformance profile as yet, so just return it in XML
        if (theRequestDetails.getOperation() != null) {
            if (theRequestDetails.getOperation().equals("metadata")) {
            	//format = "xml";
            	return true;
            }
        }
        
        // See if they have asked for an XML version, or the HTML rendered version
        if (format == null) {
        	format = "html";
        }

        StringBuffer content = new StringBuffer();
        String resourceType = theRequestDetails.getResourceName();

        RestOperationTypeEnum operation = theRequestDetails.getRestOperationType();
        LOG.info("FHIR Operation:" + operation);
        LOG.info("Format to return to browser:" + format);
        
            if (operation == RestOperationTypeEnum.READ) {
            	if (format.equals("html")) {
            		renderSingleResource(theRequestDetails, content, resourceType);
            	} else if (format.equals("xml")) {
            		renderSingleWrappedRAWResource(theRequestDetails, content, resourceType);
            	}
            } else {
                renderListOfResources(theRequestDetails, content, resourceType);
            }

            streamHTMLresponse(theResponse, resourceType, content);

        
        return false;
    }
    
    protected void streamHTMLresponse(HttpServletResponse theResponse, String resourceType, StringBuffer content) {
    	try {
	    	// Initialise the output
	    	PrintWriter outputStream = null;
	        theResponse.setStatus(200);
	        theResponse.setContentType("text/html");
			outputStream = theResponse.getWriter();
	
	        // Put the content into our template
	        String outputString = null;
	        if (resourceType == null) {
	        	outputString = SDtemplate;
	        } else {
	            if (resourceType.equals("StructureDefinition")) {
	                outputString = SDtemplate;
	            } else if (resourceType.equals("ValueSet")) {
	                outputString = VStemplate;
	            } else if (resourceType.equals("Conformance")) {
	                outputString = ServerConformanceTemplate;
	            }
	        }
	
	        outputString = outputString.replaceFirst("\\{\\{PAGE-CONTENT\\}\\}", content.toString());
	
	        // Send it to the output
	        outputStream.append(outputString);
    	} catch (IOException e) {
    		LOG.severe(e.getMessage());
		}
    }
    
    @Override
    public boolean outgoingResponse(RequestDetails theRequestDetails, IBaseResource theResponseObject, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    	
    	if (!isBrowser(theServletRequest)) {
    		addResponseHeaders(theServletResponse);
    		return true;
    	} else {
    		// Check if this is a request for the conformance resource from a browser - if so, wrap it and return it
    		StringBuffer content = new StringBuffer();
    		renderConformance(theRequestDetails, theServletRequest, content, theResponseObject);
    		LOG.info(content.toString());
    		streamHTMLresponse(theServletResponse, "Conformance", content);
    		return false;
    	}
	}
    
    private void renderSingleWrappedRAWResource(RequestDetails theRequestDetails, StringBuffer content, String resourceType) {
        content.append(GenerateIntroSection());
        String resourceName = theRequestDetails.getId().getIdPart();
        content.append(getResourceContent(resourceName));
        content.append("</div>");
    }
    
    private void renderConformance(RequestDetails theRequestDetails, HttpServletRequest theRequest, StringBuffer content, IBaseResource conformance) {
    	LOG.info("Attempting to render conformance statement");
    	content.append(GenerateIntroSection());
    	content.append(getResourceAsXML(conformance, null));
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
    private void renderSingleResource(RequestDetails theRequestDetails, StringBuffer content, String resourceType) {

        content.append(GenerateIntroSection());

        String resourceName = theRequestDetails.getId().getIdPart();

        if (resourceType.equals("StructureDefinition")) {
            content.append(DescribeStructureDefinition(resourceName));
        }
        if (resourceType.equals("ValueSet")) {
            content.append(DescribeValueSet(resourceName));
        }
        if (resourceType.equals("OperationDefinition")) {
            throw new NotImplementedException("Code not yet written for OperationDefinition resources...");
        }
        
        //content.append(GetXMLContent(resourceName));
        
        content.append("</div>");
        
    }
    
    private String getResourceContent(String resourceName) {
        StructureDefinition sd = myWebHandler.getSDByName(resourceName);
        // Clear out the generated text
        NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv("");
        sd.setText(textElement);
        return getResourceAsXML(sd, sd.getName());
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
        	out.append("<p><a href='./" + url + "'>Back to rendered profile</a></p>");
        }
        out.append("<div class='rawXML'><pre lang='xml'>");
        out.append(xml);
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
        content.append("<li>FHIRVersion: " + printIfNotNull(sd.getFhirVersion()) + "</li>");
        content.append("<li>Show Raw Profile: <a href='./" + sd.getName() + "?_format=xml'>XML</a></li>");
        content.append("</div>");
        content.append("<div class='treeView'>");
        content.append(sd.getText().getDivAsString());
        content.append("</div>");
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
        //content.append("<li>FHIRVersion: " + printIfNotNull(valSet.getFhirVersion()) + "</li>");
        content.append("</div>");
        content.append("<div class='treeView'>");
        content.append(valSet.getText().getDivAsString());
        content.append("</div>");
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
    private void renderListOfResources(RequestDetails theRequestDetails, StringBuffer content, String resourceType) {

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
            content.append(myWebHandler.getAllGroupedStructureDefinitionNames(resourceType));
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
    
    @Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, Bundle theResponseObject,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
			throws AuthenticationException {
    	addResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}

	@Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, HttpServletRequest theServletRequest,
			HttpServletResponse theServletResponse) throws AuthenticationException {
		addResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theServletRequest, theServletResponse);
	}

	@Override
	public boolean outgoingResponse(RequestDetails theRequestDetails, TagList theResponseObject,
			HttpServletRequest theServletRequest, HttpServletResponse theServletResponse)
			throws AuthenticationException {
		addResponseHeaders(theServletResponse);
		return super.outgoingResponse(theRequestDetails, theResponseObject, theServletRequest, theServletResponse);
	}
	
	protected void addResponseHeaders(HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
	}
}
