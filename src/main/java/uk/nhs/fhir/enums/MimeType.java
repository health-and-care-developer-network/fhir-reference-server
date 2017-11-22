package uk.nhs.fhir.enums;

import java.util.Arrays;
import java.util.List;

/**
 * This is an enum to hold whether the request was for XML, JSON, or something else. It can be
 * initialised using getTypeFromHeader to identify the type requested from the value of a
 * HTTP header or request parameter
 * @author adam
 *
 */
public enum MimeType {
	
	XML, JSON, UNKNOWN_MIME;
	
    private static final List<String> xmlTypes  = Arrays.asList("xml", "text/xml", "application/xml", "application/xml+fhir");
    private static final List<String> jsonTypes = Arrays.asList("json", "application/json", "application/json+fhir");

    public static MimeType getTypeFromHeader(String mimeTypeFromHeader) {
    	if (xmlTypes.contains(mimeTypeFromHeader)) {
    		return XML;
    	} else if (jsonTypes.contains(mimeTypeFromHeader)) {
    		return JSON;
    	} else {
    		return UNKNOWN_MIME;
    	}
    }
}
