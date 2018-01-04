package uk.nhs.fhir.render.tree.tidy;

import java.util.List;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

/**
 * Tree tidier to remove slicing nodes associated with an extension node
 */
public class ExtensionsSlicingNodesRemover {
	private final FhirTreeData treeData;

	public ExtensionsSlicingNodesRemover(FhirTreeData treeData) {
		this.treeData = treeData;
	}
	
	public void process() {
		removeExtensionsSlicingNodes(treeData.getRoot());
	}

	private void removeExtensionsSlicingNodes(AbstractFhirTreeTableContent node) {
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		// if there is an extensions slicing node (immediately under root), remove it.
		for (int i=children.size()-1; i>=0; i--) {
			AbstractFhirTreeTableContent child = children.get(i);
			if (child.getPathName().equals("extension")
			  && child.hasSlicingInfo()) {
				children.remove(i);
			} else {
				// call recursively over whole tree
				removeExtensionsSlicingNodes(child);
			}
		}
	}
}
