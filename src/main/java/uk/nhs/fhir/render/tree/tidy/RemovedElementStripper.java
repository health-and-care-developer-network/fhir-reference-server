package uk.nhs.fhir.render.tree.tidy;

import java.util.List;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

/**
 * In some cases (e.g. details view) we don't want to show elements which have a maximum cardinality of 0, since they can
 * no longer appear in the resource.
 */
public class RemovedElementStripper {
	private final FhirTreeData<AbstractFhirTreeTableContent> treeData;

	public RemovedElementStripper(FhirTreeData<AbstractFhirTreeTableContent> treeData) {
		this.treeData = treeData;
	}

	public void process() {
		stripRemovedElements(treeData.getRoot());
	}
	
	/**
	 * Remove all nodes that have cardinality max = 0 (and their children)
	 */
	private void stripRemovedElements(AbstractFhirTreeTableContent node) {
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			AbstractFhirTreeTableContent child = children.get(i);
			
			if (child.isRemovedByProfile()) {
				children.remove(i);
			} else {
				stripRemovedElements(child);
			}
		}
	}
}
