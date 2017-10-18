package uk.nhs.fhir.metadata;

import java.io.File;

import uk.nhs.fhir.data.metadata.ResourceType;

public interface FhirResourceMetadata {
	public String getName();
	public boolean isExtension();

	public String getBaseType();
	
	public File getFile();
	public ResourceType getType();
}
