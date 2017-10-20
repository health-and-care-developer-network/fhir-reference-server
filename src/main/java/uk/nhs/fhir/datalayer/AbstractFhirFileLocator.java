package uk.nhs.fhir.datalayer;

import java.nio.file.Path;

import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.util.FhirVersion;

public abstract class AbstractFhirFileLocator {
	public abstract Path getRoot(FhirVersion fhirVersion);
	
	public Path pathForResourceType(ResourceType type, FhirVersion version) {
		return getRoot(version).resolve(type.getDisplayName());
	}
	
	public Path versionedPathForResourceType(ResourceType type, FhirVersion version) {
		return pathForResourceType(type, version).resolve("versioned");
	}
}
