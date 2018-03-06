package uk.nhs.fhir.data.structdef.tree;

import java.util.List;

import uk.nhs.fhir.data.structdef.FhirElementMapping;

public interface HasMappings {

	public List<FhirElementMapping> getMappings();
	public String getPath();

}
