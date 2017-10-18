package uk.nhs.fhir.metadata;

import java.util.ArrayList;
import java.util.List;

public class FhirResourceMetadataStore implements Store<FhirResourceMetadata> {

	private final List<FhirResourceIndex> resourceIndices = new ArrayList<>();
	
	@Override
	public void populate(Iterable<FhirResourceMetadata> supplier) {
		for (FhirResourceMetadata metadata : supplier) {
			for (FhirResourceIndex index : resourceIndices) {
				index.add(metadata);
			}
		}
	}

	@Override
	public void clear() {
		for (FhirResourceIndex index : resourceIndices) {
			index.clear();
		}
	}
	
	public void addIndex(FhirResourceIndex index) {
		resourceIndices.add(index);
	}
}
