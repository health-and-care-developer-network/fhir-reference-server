package uk.nhs.fhir.resourcehandlers;

import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.util.FhirVersion;

public class ResourceHelperFactory {

	public static IResourceHelper getResourceHelper(FhirVersion fhirVersion, ResourceType resourceType) {
		if (fhirVersion.equals(FhirVersion.DSTU2)) {
			if (resourceType.equals(ResourceType.STRUCTUREDEFINITION)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.StructureDefinitionProvider(null);
			} else if (resourceType.equals(ResourceType.VALUESET)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.ValueSetProvider(null);
			} else if (resourceType.equals(ResourceType.OPERATIONDEFINITION)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.OperationDefinitionProvider(null);
			} else if (resourceType.equals(ResourceType.CONFORMANCE)) {
				return new uk.nhs.fhir.resourcehandlers.dstu2.ConformanceProvider(null);
			}
		}
		
		if (fhirVersion.equals(FhirVersion.STU3)) {
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
