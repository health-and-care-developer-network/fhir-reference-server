package uk.nhs.fhir.enums;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.rest.method.RequestDetails;

/**
 * This is an enum to hold whether the request is from a browser or not. It can be initialised using
 * getTypeFromHeaders to detect and return whether it is a browser or not.
 * @author Adam Hatherly
 */
public enum ResourceType {
	
	STRUCTUREDEFINITION("StructureDefinition", "StructureDefinition"),
	VALUESET("ValueSet", "ValueSet"),
	OPERATIONDEFINITION("OperationDefinition", "OperationDefinition"),
	CONFORMANCE("Conformance", "Conformance"), 
	OTHER("Other", "Other");
	
	private static final Logger LOG = Logger.getLogger(ResourceType.class.getName());
	
	private ResourceType(String displayName, String hapiName) {
		this.displayName = displayName;
		this.hapiName = hapiName;
	}
	
	private String displayName = null;
	private String hapiName = null;
	
	@Override
	public String toString() {
		return this.displayName;
	}
	
	public String getHAPIName() {
		return this.hapiName;
	}
	
    public static ResourceType getTypeFromRequest(RequestDetails theRequestDetails) {
    	
    	String typeInRequest = theRequestDetails.getResourceName();
    	LOG.info("Detecting type of resource: " + typeInRequest);
    	
    	if (typeInRequest == null) { 
    		return OTHER;
    	} else if (typeInRequest.equals(STRUCTUREDEFINITION.hapiName)) {
    		return STRUCTUREDEFINITION;
    	} else if (typeInRequest.equals(VALUESET.hapiName)) {
    		return VALUESET;
    	} else if (typeInRequest.equals(OPERATIONDEFINITION.hapiName)) {
    		return OPERATIONDEFINITION;
    	} else if (typeInRequest.equals(CONFORMANCE.hapiName)) {
    		return CONFORMANCE;
    	}
    	return OTHER;
    }
}
