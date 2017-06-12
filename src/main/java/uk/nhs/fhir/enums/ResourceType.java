package uk.nhs.fhir.enums;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import ca.uhn.fhir.rest.method.RequestDetails;
import uk.nhs.fhir.util.PropertyReader;

/**
 * This is an enum to hold whether the request is from a browser or not. It can be initialised using
 * getTypeFromHeaders to detect and return whether it is a browser or not.
 * @author Adam Hatherly
 */
public enum ResourceType {
	
	STRUCTUREDEFINITION("StructureDefinition", "StructureDefinition", PropertyReader.getProperty("profilePath")),
	VALUESET("ValueSet", "ValueSet", PropertyReader.getProperty("valusetPath")),
	OPERATIONDEFINITION("OperationDefinition", "OperationDefinition", PropertyReader.getProperty("operationsPath")),
	IMPLEMENTATIONGUIDE("ImplementationGuide", "ImplementationGuide", PropertyReader.getProperty("guidesPath")),
	CONFORMANCE("Conformance", "Conformance", PropertyReader.getProperty("conformancePath")),
	EXAMPLES("Examples", "Examples", PropertyReader.getProperty("examplesPath")),
	OTHER("Other", "Other", null);
	
	private static final Logger LOG = Logger.getLogger(ResourceType.class.getName());
	
	private ResourceType(String displayName, String hapiName, String filesystemPath) {
		this.displayName = displayName;
		this.hapiName = hapiName;
		this.filesystemPath = filesystemPath;
	}
	
	private String displayName = null;
	private String hapiName = null;
	private String filesystemPath = null;
	
	@Override
	public String toString() {
		return this.displayName;
	}
	
	public String getHAPIName() {
		return this.hapiName;
	}
	
	public String getFilesystemPath() {
		return this.filesystemPath;
	}
	
	public String getVersionedFilesystemPath() {
		return this.filesystemPath + "/versioned";
	}
	
    public static ResourceType getTypeFromRequest(RequestDetails theRequestDetails) {
    	String typeInRequest = theRequestDetails.getResourceName();
    	LOG.info("Detecting type of resource: " + typeInRequest);
    	return getTypeFromHAPIName(typeInRequest);
    }
    
    public static ResourceType getTypeFromHAPIName(String hapiName) {
    	if (hapiName == null) { 
    		return OTHER;
    	} else if (hapiName.equals(STRUCTUREDEFINITION.hapiName)) {
    		return STRUCTUREDEFINITION;
    	} else if (hapiName.equals(VALUESET.hapiName)) {
    		return VALUESET;
    	} else if (hapiName.equals(OPERATIONDEFINITION.hapiName)) {
    		return OPERATIONDEFINITION;
    	} else if (hapiName.equals(IMPLEMENTATIONGUIDE.hapiName)) {
    		return IMPLEMENTATIONGUIDE;
    	} else if (hapiName.equals(CONFORMANCE.hapiName)) {
    		return CONFORMANCE;
    	}
    	return OTHER;
    }
}
