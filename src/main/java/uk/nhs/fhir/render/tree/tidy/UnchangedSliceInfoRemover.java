package uk.nhs.fhir.render.tree.tidy;

import java.util.List;
import java.util.stream.StreamSupport;

import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.TreeNode;

/**
 * Takes a FHIR tree data and its differential data. Removes any children of slice nodes if they are not modified 
 * from the base resource (i.e. not in the differential) and have no descendants that are modified from the base resource.
 * 
 * Since it relies on differential nodes being aware of their backup nodes, to keep interesting nodes in the tree,
 * must happen after backup nodes have been assigned.
 */
public class UnchangedSliceInfoRemover<
		T extends HasBackupNode<V, W>, U extends TreeNode<T, U>, 
		V extends HasSlicingInfo, W extends TreeNode<V, W>> {
	
	private final FhirTreeData<T, U> differentialTree;

	public UnchangedSliceInfoRemover(FhirTreeData<T, U> differentialTree) {
		this.differentialTree = differentialTree;
	}
	
	public void process(FhirTreeData<V, W> treeToModify) {
		removeNonDifferentialSlicingChildren(treeToModify.getRoot());
	}
	
	private void removeNonDifferentialSlicingChildren(W currentNode) {
		if (currentNode.getData().hasSlicingInfo()) {
			removeNonDifferentialDescendants(currentNode);
		} else {
			for (W child : currentNode.getChildren()) {
				removeNonDifferentialSlicingChildren(child);
			}
		}
	}
	
	private void removeNonDifferentialDescendants(W node) {
		List<W> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			W child = children.get(i);
			if (!isDifferentialBackupNode(child)) {
				children.remove(i);
			} else {
				removeNonDifferentialDescendants(child);
			}
		}
	}

	private boolean isDifferentialBackupNode(W candidateForRemoval) {
		return StreamSupport.stream(differentialTree.spliterator(), false)
			.anyMatch(differentialNode -> differentialNode.getBackupNode().equals(candidateForRemoval));
	}
}
