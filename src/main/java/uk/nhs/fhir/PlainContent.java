/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.fhir;

import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IIdType;
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
        
        Enumeration headerNames = theRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = theRequest.getHeader(key);
            LOG.info(key + " : " + value);
        }
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
            theResponse.addHeader("Content-Type", "text/html");
                    
            pw = theResponse.getWriter();
            pw.append("<html><body>Hello browser, clearly you were looking for a: " + theRequestDetails.getResourceName());
            
            //String id = theRequestDetails.getId().getIdPart();
            pw.append(myWebber.getAllNames());
            //if(id == null) {
            //    pw.append("id Was null");
            //} else {
                //pw.append(id);
            //}
            pw.append("</body></html>");
        } catch (IOException ex) {
            LOG.info("" + ex.getMessage());
        }
        return false;
    }
}
