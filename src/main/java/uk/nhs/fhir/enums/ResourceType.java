package uk.nhs.fhir.enums;

import java.util.HashMap;
import java.util.logging.Logger;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.util.PropertyReader;

/**
 * This is an enum to hold whether the request is from a browser or not. It can be initialised using
 * getTypeFromHeaders to detect and return whether it is a browser or not.
 * @author Adam Hatherly
 */
public enum ResourceType {
	
	STRUCTUREDEFINITION("StructureDefinition", "StructureDefinition"),
	VALUESET("ValueSet", "ValueSet"),
	OPERATIONDEFINITION("OperationDefinition", "OperationDefinition"),
	IMPLEMENTATIONGUIDE("ImplementationGuide", "ImplementationGuide"),
	CONFORMANCE("Conformance", "Conformance"),
	
	// Added for STU3
	CONCEPTMAP("ConceptMap", "ConceptMap"),
	CODESYSTEM("CodeSystem", "CodeSystem"),
	
	EXAMPLES("Examples", "Examples"),
	OTHER("Other", "Other");
	
	private static final Logger LOG = Logger.getLogger(ResourceType.class.getName());
	
	private ResourceType(String displayName, String hapiName) {
		this.displayName = displayName;
		this.hapiName = hapiName;
		
		// Lookup the relevant path for the FHIR version and resource type
		for (FHIRVersion fhirVersion : FHIRVersion.values()) {
			if (this.filesystemPath == null) {
				this.filesystemPath = new HashMap<FHIRVersion, String>();
			}
			this.filesystemPath.put(fhirVersion, PropertyReader.getProperty("Path-"+displayName+"-"+fhirVersion));
		}
	}
	
	private String displayName = null;
	private String hapiName = null;
	private HashMap<FHIRVersion, String> filesystemPath = null;
	
	@Override
	public String toString() {
		return this.displayName;
	}
	
	public String getHAPIName() {
		return this.hapiName;
	}
	
	public String getFilesystemPath(FHIRVersion fhirVersion) {
		return this.filesystemPath.get(fhirVersion);
	}
	
	public String getVersionedFilesystemPath(FHIRVersion fhirVersion) {
		return getFilesystemPath(fhirVersion) + "/versioned";
	}
	
    public static ResourceType getTypeFromRequest(RequestDetails theRequestDetails) {
    	String typeInRequest = theRequestDetails.getResourceName();
    	LOG.fine("Detecting type of resource: " + typeInRequest);
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
    	} else if (hapiName.equals(CODESYSTEM.hapiName)) {
    		return CODESYSTEM;
    	} else if (hapiName.equals(CONCEPTMAP.hapiName)) {
    		return CONCEPTMAP;
    	}
    	return OTHER;
    }
}
