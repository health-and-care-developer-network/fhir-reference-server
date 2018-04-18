package uk.nhs.fhir.util;

import java.nio.file.Path;

import uk.nhs.fhir.data.metadata.ResourceType;

public abstract class AbstractFhirFileLocator {
	public abstract Path getSourceRoot(FhirVersion fhirVersion);
	public abstract Path getDestinationPathForResourceType(ResourceType type, FhirVersion version);

	public Path getSourcePathForResourceType(ResourceType type, FhirVersion version) {
		return getSourceRoot(version).resolve(type.getFolderName());
	}
}
