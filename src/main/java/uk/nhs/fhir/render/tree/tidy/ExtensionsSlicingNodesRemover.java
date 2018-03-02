package uk.nhs.fhir.render.tree.tidy;

import java.util.List;

import uk.nhs.fhir.render.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.render.tree.FhirTreeData;

/**
 * Tree tidier to remove slicing nodes associated with an extension node
 */
public class ExtensionsSlicingNodesRemover<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {
	
	private final FhirTreeData<T, U> treeData;

	public ExtensionsSlicingNodesRemover(FhirTreeData<T, U> treeData) {
		this.treeData = treeData;
	}
	
	public void process() {
		removeExtensionsSlicingNodes(treeData.getRoot());
	}

	private void removeExtensionsSlicingNodes(U node) {
		List<U> children = node.getChildren();
		
		// if there is an extensions slicing node (immediately under root), remove it.
		for (int i=children.size()-1; i>=0; i--) {
			U child = children.get(i);
			if (child.getData().getPathName().equals("extension")
			  && child.getData().hasSlicingInfo()) {
				children.remove(i);
			} else {
				// call recursively over whole tree
				removeExtensionsSlicingNodes(child);
			}
		}
	}
}
