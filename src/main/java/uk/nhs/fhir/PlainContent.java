/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author tim
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
