package uk.nhs.fhir.enums;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an enum to hold whether the request is from a browser or not. It can be initialised using
 * getTypeFromHeaders to detect and return whether it is a browser or not.
 * @author Adam Hatherly
 */
public enum ClientType {
	
	BROWSER, NON_BROWSER;
	
	private static final Logger LOG = LoggerFactory.getLogger(ClientType.class.getName());
	
    public static ClientType getTypeFromHeaders(HttpServletRequest theRequest) {
    	String mimes = theRequest.getHeader("accept");
    	// Check if this has come from a browser
        if (mimes == null) {
            LOG.debug("No accept header set, assume a non-browser client.");
            return NON_BROWSER;
        } else {
            if (mimes.contains("html") == false) {
                LOG.debug("Accept header set, but without html, so assume a non-browser client.");
                return NON_BROWSER;
            }
        }
        return BROWSER;
    }
}
