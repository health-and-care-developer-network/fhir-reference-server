package uk.nhs.fhir.util;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;

public interface StructureDefinitionRepository {
	public WrappedStructureDefinition getStructureDefinitionIgnoreCase(FhirVersion version, String url);
	public boolean isCachedPermittedMissingExtension(String url);
	public void addCachedPermittedMissingExtension(String url);
}
