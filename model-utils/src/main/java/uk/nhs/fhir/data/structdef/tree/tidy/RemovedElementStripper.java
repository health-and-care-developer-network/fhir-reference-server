package uk.nhs.fhir.data.structdef.tree.tidy;

import java.util.List;

import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;

/**
 * In some cases (e.g. details view) we don't want to show elements which have a maximum cardinality of 0, since they can
 * no longer appear in the resource.
 */
public class RemovedElementStripper<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {
	
	private final FhirTreeData<T, U> treeData;

	public RemovedElementStripper(FhirTreeData<T, U> treeData) {
		this.treeData = treeData;
	}

	public void process() {
		stripRemovedElements(treeData.getRoot());
	}
	
	/**
	 * Remove all nodes that have cardinality max = 0 (and their children)
	 */
	private void stripRemovedElements(U node) {
		List<U> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			U child = children.get(i);
			
			if (child.isRemovedByProfile()) {
				children.remove(i);
			} else {
				stripRemovedElements(child);
			}
		}
	}
}
