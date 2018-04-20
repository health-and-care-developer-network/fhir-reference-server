package uk.nhs.fhir.data.structdef.tree;

import java.util.Map;

import com.google.common.collect.Maps;

public class FhirTreeKeyLookup<T, U extends TreeNode<T, U> & HasNodeKey> {
	
	private final Map<String, U> nodesByKey = Maps.newHashMap();
	
	public FhirTreeKeyLookup(FhirTreeData<T, U> tree) {
		for (U node : tree.nodes()) {
			String displayName = node.getNodeKey();
			if (!nodesByKey.containsKey(displayName)) {
				nodesByKey.put(displayName, node);
			} else {
				throw new IllegalStateException("Non-unique display name: " + displayName);
			}
		}
	}
	
	public U nodeForKey(String key) {
		if (nodesByKey.containsKey(key)) {
			return nodesByKey.get(key);
		} else {
			throw new IllegalStateException("Haven't recorded a node with key " + key);
		}
	}
}