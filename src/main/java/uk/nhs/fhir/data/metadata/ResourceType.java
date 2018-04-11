package uk.nhs.fhir.data.metadata;

import java.util.Arrays;
import java.util.List;

import uk.nhs.fhir.util.FhirVersion;

public enum ResourceType {
	
	STRUCTUREDEFINITION("StructureDefinition", "StructureDefinition", "StructureDefinition", "StructureDefinitions", "StructureDefinition"),
	VALUESET("ValueSet", "ValueSet", "ValueSet", "ValueSets", "ValueSet"),
	OPERATIONDEFINITION("OperationDefinition", "OperationDefinition", "OperationDefinition", "OperationDefinitions", "OperationDefinition"),
	IMPLEMENTATIONGUIDE("ImplementationGuide", "ImplementationGuide", "ImplementationGuide", "ImplementationGuides", "ImplementationGuide"),
	CONFORMANCE("Conformance", "Conformance", "Conformance", "Conformance Statements", "Conformance"),
	
	// Added for STU3
	CONCEPTMAP("ConceptMap", "ConceptMap", "ConceptMap", "ConceptMaps", "ConceptMap"),
	CODESYSTEM("CodeSystem", "CodeSystem", "CodeSystem", "CodeSystems", "CodeSystem"),
	MESSAGEDEFINITION("MessageDefinition", "MessageDefinition", "MessageDefinition", "MessageDefinitions", "MessageDefinition"),
	
	EXTENSION("Extension", null, "Extensions", "Extension Registry", "Extensions"),
	EXAMPLES("Example", "Examples", "Examples", "Examples", "Examples"),
	OTHER("Other", "Other", "Other", null, null);

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
	
	private ResourceType(String displayName, String hapiName, String folderName, String breadcrumbName, String requestPath) {
		this.displayName = displayName;
		this.hapiName = hapiName;
		this.folderName = folderName;
		this.breadcrumbName = breadcrumbName;
		this.requestPath = requestPath;
	}
	
	private final String displayName;
	private final String hapiName;
	private final String folderName;
	private final String breadcrumbName;
	private final String requestPath;
	
	@Override
	public String toString() {
		return displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getHAPIName() {
		return hapiName;
	}
	
	public String getBreadcrumbName() {
		return breadcrumbName;
	}
	
	public String getFolderName() {
		return folderName;
	}
	
	public String getRequestPath() {
		return requestPath;
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
