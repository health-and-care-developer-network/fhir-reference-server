package uk.nhs.fhir.metadata.index;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

import uk.nhs.fhir.data.metadata.ResourceMetadata;

public abstract class HashedResourceIndex<K> implements FhirResourceIndex {

	private ConcurrentMap<K, ResourceMetadata> map = Maps.newConcurrentMap();
	
	protected abstract K getKey(ResourceMetadata metadata);

	@Override
	public void addUnique(ResourceMetadata metadata) {
		K key = getKey(metadata);
		map.putIfAbsent(key, metadata);
	}

	@Override
	public void clear() {
		map.clear();
	}

}
