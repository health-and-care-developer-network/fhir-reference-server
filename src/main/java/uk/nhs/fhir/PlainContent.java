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

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.fhir.resourcehandlers.ProfileWebHandler;
import uk.nhs.fhir.util.FileLoader;
import uk.nhs.fhir.util.PropertyReader;

/**
 *
 * @author Tim Coates
 */
public class PlainContent extends InterceptorAdapter {
    private static final Logger LOG = Logger.getLogger(PlainContent.class.getName());
    ProfileWebHandler myWebber = null;
    private String template = null;
    private String fhirServerNotice = null;
    private String fhirServerWarning = null;
    
    public PlainContent(ProfileWebHandler webber) {
        myWebber = webber;
        template = FileLoader.loadFileOnClasspath("/template/profiles.html");
        fhirServerNotice = PropertyReader.getProperty("fhirServerNotice");
        fhirServerWarning = PropertyReader.getProperty("fhirServerWarning");
    }
    
    
    
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) {
        PrintWriter outputStream = null;

        String mimes = theRequest.getHeader("accept");
        
        if (mimes == null) {
        	LOG.info("No accept header set, assume a non-browser client.");
            return true;
        } else {
            if (mimes.contains("html") == false) {
            	LOG.info("Accept header set, but without html, so assume a non-browser client.");
            	return true;
            }
        }
        
        if (theRequestDetails.getOperation() != null) {
	        if (theRequestDetails.getOperation().equals("metadata")) {
	        	// We don't have any rendered version of the conformance profile as yet, so just return it in raw form
	        	return true;
	        }
        }

        LOG.info("This appears to be a browser, generate some HTML to return.");
        
        StringBuffer content = new StringBuffer();
        String resourceType = theRequestDetails.getResourceName();
        
        try {
        	if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.READ){
        		renderSingleResource(theRequestDetails, content, resourceType);
            } else {
                renderListOfResources(theRequestDetails, content, resourceType);
            }
            
            // Initialise the output
            theResponse.setStatus(200);
    	    theResponse.setContentType("text/html");
    		outputStream = theResponse.getWriter();
    		
    		// Put the content into our template
    	    String outputString = template;
            outputString = outputString.replaceFirst("\\{\\{PAGE-CONTENT\\}\\}", content.toString());
            
            // Send it to the output
            outputStream.append(outputString);
            
        } catch (IOException ex) {
            LOG.severe("Error sending response: " + ex.getMessage());
        }
        return false;
    }
    
    private void renderSingleResource(RequestDetails theRequestDetails, StringBuffer content, String resourceType) {

        content.append("<div class='fhirServerGeneratedContent'>");
        content.append(fhirServerWarning);
        content.append(fhirServerNotice);

        if(resourceType.equals("StructureDefinition")) {
            content.append(DescribeStructureDefinition(theRequestDetails, resourceType));
        }
        if(resourceType.equals("ValueSet")) {
            content.append(DescribeValueSet(theRequestDetails, resourceType));
        }
        
        
        
        content.append("</div>");
    }

    private String DescribeStructureDefinition(RequestDetails theRequestDetails, String resourceType) {
        StringBuilder content = new StringBuilder();
        StructureDefinition sd;
        sd = myWebber.getSDByName(theRequestDetails.getId().getIdPart());
        content.append("<h2 class='resourceType'>" + sd.getName() + " (" + resourceType + ")</h2>");
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
        content.append("</div>");
        content.append("<div class='treeView'>");
        content.append(sd.getText().getDivAsString());
        content.append("</div>");
        return content.toString();
    }
    
    private String DescribeValueSet(RequestDetails theRequestDetails, String resourceType) {
        StringBuilder content = new StringBuilder();
        ValueSet valSet;
        valSet = myWebber.getVSByName(theRequestDetails.getId().getIdPart());
        content.append("<h2 class='resourceType'>" + valSet.getName() + " (" + resourceType + ")</h2>");
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
    
    private void renderListOfResources(RequestDetails theRequestDetails, StringBuffer content, String resourceType) {
    	Map<String, String[]> params = theRequestDetails.getParameters();
        
        content.append("<div class='fhirServerGeneratedContent'>");
        content.append(fhirServerWarning);
        content.append(fhirServerNotice);
        content.append("<h2 class='resourceType'>" + resourceType + " Resources</h2><ul>");
        
        if(params.containsKey("name") || params.containsKey("name:contains")) {
            if(params.containsKey("name")) {
                content.append(myWebber.getAllNames(resourceType, params.get("name")[0]));
            }
            if(params.containsKey("name:contains")) {
                content.append(myWebber.getAllNames(resourceType, params.get("name:contains")[0]));
            }
        } else {
            //content.append(myWebber.getAllNames(resourceType));
            content.append(myWebber.getAllGroupedNames(resourceType));
        }
        content.append("</ul>");
        content.append("</div>");
    }
    
    private static Object printIfNotNull(Object input) {
    	return (input == null)?"":input;
    }
}
