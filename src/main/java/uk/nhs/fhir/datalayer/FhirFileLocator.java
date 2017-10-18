package uk.nhs.fhir.datalayer;

import java.nio.file.Path;

import uk.nhs.fhir.util.FhirVersion;

public interface FhirFileLocator {
	public Path getRoot(FhirVersion fhirVersion);
}
