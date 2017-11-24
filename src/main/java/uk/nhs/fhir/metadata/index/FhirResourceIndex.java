package uk.nhs.fhir.metadata.index;

import uk.nhs.fhir.data.metadata.ResourceMetadata;

public interface FhirResourceIndex {

	public boolean accept(ResourceMetadata metadata);
	public void addUnique(ResourceMetadata metadata);
	public void clear();

}
