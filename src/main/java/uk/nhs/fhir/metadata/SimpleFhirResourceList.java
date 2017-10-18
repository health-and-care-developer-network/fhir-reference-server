package uk.nhs.fhir.metadata;

import java.util.List;

import com.google.common.collect.Lists;

public class SimpleFhirResourceList implements FhirResourceIndex {

	private final List<FhirResourceMetadata> list = Lists.newArrayList();
	
	@Override
	public void add(FhirResourceMetadata metadata) {
		list.add(metadata);
	}

	@Override
	public void clear() {
		list.clear();
	}

}
