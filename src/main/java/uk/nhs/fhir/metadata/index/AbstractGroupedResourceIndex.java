package uk.nhs.fhir.metadata.index;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Maps;

import uk.nhs.fhir.data.metadata.ResourceMetadata;

public abstract class AbstractGroupedResourceIndex<K> implements FhirResourceIndex {
	private final ConcurrentMap<K, List<ResourceMetadata>> map = Maps.newConcurrentMap();

	protected abstract K getKey(ResourceMetadata metadata);

	@Override
	public void addUnique(ResourceMetadata metadata) {
		K key = getKey(metadata);
		synchronized(this) {
			map.putIfAbsent(key, new CopyOnWriteArrayList<>());
			List<ResourceMetadata> list = map.get(key);
			if (!list.contains(metadata)) {
				list.add(metadata);
			}
		}
	}

	@Override
	public void clear() {
		map.clear();
	}
}
