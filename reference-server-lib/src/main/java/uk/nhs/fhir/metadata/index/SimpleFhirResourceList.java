package uk.nhs.fhir.metadata.index;

import java.util.List;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.metadata.ResourceMetadata;

public class SimpleFhirResourceList implements FhirResourceIndex {

	private final List<ResourceMetadata> list = Lists.newArrayList();
	
	@Override
	public void addUnique(ResourceMetadata metadata) {
		if (!list.contains(metadata)) {
			list.add(metadata);
		}
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean accept(ResourceMetadata metadata) {
		return true;
	}

}
