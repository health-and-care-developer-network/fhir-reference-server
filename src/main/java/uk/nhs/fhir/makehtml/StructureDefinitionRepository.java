package uk.nhs.fhir.makehtml;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.util.FhirVersion;

public interface StructureDefinitionRepository {
	public WrappedStructureDefinition getStructureDefinitionIgnoreCase(FhirVersion version, String url);
	public boolean isCachedPermittedMissingExtension(String url);
	public void addCachedPermittedMissingExtension(String url);
}
