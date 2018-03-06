package uk.nhs.fhir.util;

import java.util.Optional;

import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;

public interface StructureDefinitionRepository {
	public WrappedStructureDefinition getStructureDefinitionIgnoreCase(FhirVersion version, String url);
	public boolean isCachedPermittedMissingExtension(String url);
	public void addCachedPermittedMissingExtension(String url);
	public Optional<WrappedStructureDefinition> getUserDefinedType(String constrainedType, String url, FhirVersion version);
}
