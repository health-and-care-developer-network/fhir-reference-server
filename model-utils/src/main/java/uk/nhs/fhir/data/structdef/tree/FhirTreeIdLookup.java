package uk.nhs.fhir.data.structdef.tree;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FhirTreeIdLookup<T extends HasId, U extends TreeNode<T, U>> {
		
	private final Map<String, List<U>> nodesById = Maps.newHashMap();
	
	public FhirTreeIdLookup(FhirTreeData<T, U> tree) {
		for (U node : tree.nodes()) {
			Optional<String> id = node.getData().getId();
			if (id.isPresent()) {
				List<U> nodes = nodesById.computeIfAbsent(id.get(), nodeId -> Lists.newArrayList());
				nodes.add(node);
			}
		}
	}
	
	public List<U> nodesForPath(String id) {
		return nodesById.getOrDefault(id, Collections.emptyList());
	}
}
