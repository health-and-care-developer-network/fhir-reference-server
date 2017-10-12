package uk.nhs.fhir.util;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

public class PageTemplateHelper {
	
	private static final Logger LOG = Logger.getLogger(PageTemplateHelper.class.getName());
    
    public void setResponseSuccess(HttpServletResponse theResponse, String contentType, String wrappedContent) {
    	try {
			theResponse.getWriter().append(wrappedContent);

	        theResponse.setStatus(200);
	        theResponse.setContentType(contentType);
    	} catch (IOException e) {
    		LOG.severe(e.getMessage());
		}
    }
}
