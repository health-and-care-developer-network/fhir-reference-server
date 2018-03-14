package uk.nhs.fhir.metadata;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.metadata.index.FhirResourceIndex;

public class FhirResourceMetadataStore implements Store<ResourceMetadata> {

	private final List<FhirResourceIndex> resourceIndices = Lists.newArrayList();
	
	@Override
	public void populate(Iterable<ResourceMetadata> supplier) {
		for (ResourceMetadata metadata : supplier) {
			for (FhirResourceIndex index : resourceIndices) {
				if (index.accept(metadata)) {
					index.addUnique(metadata);
				}
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
