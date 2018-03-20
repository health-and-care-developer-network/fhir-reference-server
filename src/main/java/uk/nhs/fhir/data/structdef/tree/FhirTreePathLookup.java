package uk.nhs.fhir.data.structdef.tree;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FhirTreePathLookup<T, U extends TreeNode<T, U>> {
	
	private final Map<ImmutableNodePath, List<U>> nodesByPath = Maps.newHashMap();
	
	public FhirTreePathLookup(FhirTreeData<T, U> tree) {
		for (U node : tree.nodes()) {
			List<U> nodesForPath = nodesByPath.computeIfAbsent(node.getPath(), nodePath -> Lists.newArrayList());
			nodesForPath.add(node);
		}
	}
	
	public List<U> nodesForPath(ImmutableNodePath path) {
		return nodesByPath.getOrDefault(path, Collections.emptyList());
	}
}
