package uk.nhs.fhir.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectionInterceptor extends CORSInterceptor {
	
	private final String toReplace;
	private final String replacement;

	public RedirectionInterceptor(String toReplace, String replacement) {
		this.toReplace = "/" + toReplace + "/";
		this.replacement = "/" + replacement + "/";
	}
	
	@Override
	public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {
	    /*if (theRequest.getRequestURI().contains("3.0.1")) {
	    	String newURL = theRequest.getRequestURI().replaceAll("\\/3\\.0\\.1\\/", "\\/STU3\\/");
	    	try {
				theResponse.sendRedirect(newURL);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
	    	return false;
	    } else {
	    	return true;
	    }*/
		
		String requestURI = theRequest.getRequestURI();
		if (requestURI.startsWith(toReplace)) {
			String newURL = replacement + requestURI.substring(toReplace.length());
			
	    	try {
				theResponse.sendRedirect(newURL);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
	    	
	    	return false;
	    } else {
	    	return true;
	    }
	}
    
}
