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

/**
 *
 * @author Tim Coates
 */
public class PlainContent extends InterceptorAdapter {
    private static final Logger LOG = Logger.getLogger(PlainContent.class.getName());
    ProfileWebHandler myWebber = null;
    
    public PlainContent(ProfileWebHandler webber) {
        myWebber = webber;
    }
    
    
    
    @Override
    public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest, HttpServletResponse theResponse) {
        PrintWriter pw = null;

        String mimes = theRequest.getHeader("accept");
        
        if (mimes == null) {
            return true;
        } else {
            if (mimes.contains("html") == false) {
                return true;
            }
        }

        try {
            theResponse.setStatus(200);
            theResponse.setContentType("text/html");
            pw = theResponse.getWriter();
            pw.append("<html><body>");
            if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.READ){
                pw.append("Hello browser, clearly you were looking for a: <b>" + theRequestDetails.getResourceName() + "</b><br /><ul>");
                StructureDefinition sd = myWebber.getSDByName(theRequestDetails.getId().getIdPart());
                pw.append("<ul>");
                pw.append("<li>url: " + sd.getUrl() + "</li>");
                pw.append("<li>version: " + sd.getVersion() + "</li>");
                pw.append("<li>name: " + sd.getName() + "</li>");
                pw.append("<li>publisher: " + sd.getPublisher() + "</li>");
                pw.append("<li>description: " + sd.getDescription() + "</li>");
                pw.append("<li>requirements: " + sd.getRequirements() + "</li>");
                pw.append("<li>status: " + sd.getStatus() + "</li>");
                pw.append("<li>experimental: " + sd.getExperimental() + "</li>");
                pw.append("<li>date: " + sd.getDate() + "</li>");
                pw.append("<li>fhirVersion: " + sd.getFhirVersion() + "</li>");
                pw.append(sd.getText().getDivAsString());
                
                pw.append("");
            } else {
                Map<String, String[]> params = theRequestDetails.getParameters();
                
                pw.append("Hello browser, clearly you were looking for resources of type: <b>" + theRequestDetails.getResourceName() + "</b><br /><ul>");

                if(params.containsKey("name") || params.containsKey("name:contains")) {
                    if(params.containsKey("name")) {
                        pw.append(myWebber.getAllNames(params.get("name")[0]));
                    }
                    if(params.containsKey("name:contains")) {
                        pw.append(myWebber.getAllNames(params.get("name:contains")[0]));
                    }
                } else {
                    pw.append(myWebber.getAllNames());
                }
                pw.append("</ul>");
            }
            pw.append("</body></html>");
        } catch (IOException ex) {
            LOG.info("" + ex.getMessage());
        }
        return false;
    }
}
