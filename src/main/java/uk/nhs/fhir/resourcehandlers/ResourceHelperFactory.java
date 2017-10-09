package uk.nhs.fhir.resourcehandlers;

import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.util.FHIRVersion;

public class ResourceHelperFactory {

	public static IResourceHelper getResourceHelper(FHIRVersion fhirVersion, ResourceType resourceType) {
		if (fhirVersion.equals(FHIRVersion.DSTU2)) {
			if (resourceType.equals(ResourceType.STRUCTUREDEFINITION)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.StrutureDefinitionProvider(null);
			} else if (resourceType.equals(ResourceType.VALUESET)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.ValueSetProvider(null);
			} else if (resourceType.equals(ResourceType.OPERATIONDEFINITION)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.OperationDefinitionProvider(null);
			} else if (resourceType.equals(ResourceType.CONFORMANCE)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.ConformanceProvider(null);
			}
		}
		
		if (fhirVersion.equals(FHIRVersion.STU3)) {
			if (resourceType.equals(ResourceType.STRUCTUREDEFINITION)) {
				return new uk.nhs.fhir.resourcehandlers.stu3.StructureDefinitionProvider(null);
			} else if (resourceType.equals(ResourceType.VALUESET)) {
				return new uk.nhs.fhir.resourcehandlers.stu3.ValueSetProvider(null);
			} else if (resourceType.equals(ResourceType.OPERATIONDEFINITION)) {
				return new uk.nhs.fhir.resourcehandlers.stu3.OperationDefinitionProvider(null);
			} else if (resourceType.equals(ResourceType.CODESYSTEM)) {
				return new uk.nhs.fhir.resourcehandlers.stu3.CodeSystemProvider(null);
			} else if (resourceType.equals(ResourceType.CONCEPTMAP)) {
				return new uk.nhs.fhir.resourcehandlers.stu3.ConceptMapProvider(null);
			}
		}
		
		return null;
	}
}
