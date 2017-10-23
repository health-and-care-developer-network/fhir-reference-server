package uk.nhs.fhir.data.metadata;

import java.util.Arrays;
import java.util.List;

import uk.nhs.fhir.util.FhirVersion;

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

	private static final ResourceType[] DSTU2_TYPES = new ResourceType[]{STRUCTUREDEFINITION, VALUESET, OPERATIONDEFINITION, IMPLEMENTATIONGUIDE};
	private static final ResourceType[] STU3_TYPES = new ResourceType[]{STRUCTUREDEFINITION, VALUESET, OPERATIONDEFINITION, IMPLEMENTATIONGUIDE, CONCEPTMAP, CODESYSTEM};
	
	public static List<ResourceType> typesForFhirVersion(FhirVersion fhirVersion) {
		switch (fhirVersion) {
		case DSTU2:
			return Arrays.asList(DSTU2_TYPES);
		case STU3:
			return Arrays.asList(STU3_TYPES);
		default:
			throw new IllegalStateException("Don't know which resource types apply to version " + fhirVersion.toString());
		}
	}
	
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
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getHAPIName() {
		return this.hapiName;
	}
    
    public static ResourceType getTypeFromHAPIName(String hapiName) {
    	for (ResourceType type : ResourceType.values()) {
    		if (type.hapiName.equals(hapiName)) {
    			return type;
    		}
    	}
    	
    	// unrecognised, or null
    	return OTHER;
    }
}
