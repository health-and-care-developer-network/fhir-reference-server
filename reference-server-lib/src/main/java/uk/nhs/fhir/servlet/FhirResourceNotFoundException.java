package uk.nhs.fhir.servlet;

import java.util.Optional;

import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public class FhirResourceNotFoundException extends RuntimeException {
	
	public FhirResourceNotFoundException(FhirVersion version, String resourceType, String resourceName, Optional<String> resourceVersion) {
		super("Failed to find " + version.toString() + " " + resourceType + " resource " + resourceName + (resourceVersion.isPresent() ? " (v" + resourceVersion.get() + ")" : ""));
	}
}
