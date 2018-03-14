package uk.nhs.fhir.servlet;

import uk.nhs.fhir.util.FhirVersion;

@SuppressWarnings("serial")
public class FhirResourceNotFoundException extends RuntimeException {
	
	public FhirResourceNotFoundException(FhirVersion version, String resourceType, String resourceName) {
		super("Failed to find " + version.toString() + " " + resourceType + " resource " + resourceName);
	}
}
