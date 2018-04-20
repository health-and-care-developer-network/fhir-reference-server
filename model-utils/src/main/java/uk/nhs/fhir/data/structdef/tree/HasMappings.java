package uk.nhs.fhir.data.structdef.tree;

import java.util.List;

import uk.nhs.fhir.data.structdef.FhirElementMapping;
import uk.nhs.fhir.data.structdef.tree.tidy.HasPath;

public interface HasMappings extends HasPath {

	public List<FhirElementMapping> getMappings();

}
