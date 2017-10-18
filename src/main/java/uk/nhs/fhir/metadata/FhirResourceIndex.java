package uk.nhs.fhir.metadata;

public interface FhirResourceIndex {

	public void add(FhirResourceMetadata metadata);
	public void clear();

}
