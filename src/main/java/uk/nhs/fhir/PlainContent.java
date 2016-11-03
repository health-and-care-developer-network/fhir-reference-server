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

import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
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
import uk.nhs.fhir.resourcehandlers.ProfileWebHandler;
import uk.nhs.fhir.util.FileLoader;

/**
 *
 * @author Tim Coates
 */
public class PlainContent extends InterceptorAdapter {
    private static final Logger LOG = Logger.getLogger(PlainContent.class.getName());
    ProfileWebHandler myWebber = null;
    private String template = null;
    
    public PlainContent(ProfileWebHandler webber) {
        myWebber = webber;
        template = FileLoader.loadFileOnClasspath("/template/devnet.html");
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

        LOG.info("This appears to be a browser, generate some HTML to return.");
        
        StringBuffer content = new StringBuffer();
	    
        try {
            //content = theResponse.getWriter();
            //content.append("<html><body>");
            if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.READ){
                content.append("Hello browser, clearly you were looking for a: <b>" + theRequestDetails.getResourceName() + "</b><br /><ul>");
                StructureDefinition sd = myWebber.getSDByName(theRequestDetails.getId().getIdPart());
                content.append("<ul>");
                content.append("<li>url: " + sd.getUrl() + "</li>");
                content.append("<li>version: " + sd.getVersion() + "</li>");
                content.append("<li>name: " + sd.getName() + "</li>");
                content.append("<li>publisher: " + sd.getPublisher() + "</li>");
                content.append("<li>description: " + sd.getDescription() + "</li>");
                content.append("<li>requirements: " + sd.getRequirements() + "</li>");
                content.append("<li>status: " + sd.getStatus() + "</li>");
                content.append("<li>experimental: " + sd.getExperimental() + "</li>");
                content.append("<li>date: " + sd.getDate() + "</li>");
                content.append("<li>fhirVersion: " + sd.getFhirVersion() + "</li>");
                content.append(sd.getText().getDivAsString());
                
                content.append("");
            } else {
                Map<String, String[]> params = theRequestDetails.getParameters();
                
                content.append("Hello browser, clearly you were looking for resources of type: <b>" + theRequestDetails.getResourceName() + "</b><br /><ul>");

                if(params.containsKey("name") || params.containsKey("name:contains")) {
                    if(params.containsKey("name")) {
                        content.append(myWebber.getAllNames(params.get("name")[0]));
                    }
                    if(params.containsKey("name:contains")) {
                        content.append(myWebber.getAllNames(params.get("name:contains")[0]));
                    }
                } else {
                    content.append(myWebber.getAllNames());
                }
                content.append("</ul>");
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
}
