package uk.nhs.fhir.render.tree.tidy;

import java.util.List;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.TreeNode;

/**
 * In some cases (e.g. details view) we don't want to show elements which have a maximum cardinality of 0, since they can
 * no longer appear in the resource.
 */
public class RemovedElementStripper<T extends AbstractFhirTreeTableContent, U extends TreeNode<T, U>> {
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
			
			if (child.getData().isRemovedByProfile()) {
				children.remove(i);
			} else {
				stripRemovedElements(child);
			}
		}
	}
}
