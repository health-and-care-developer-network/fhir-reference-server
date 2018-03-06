package uk.nhs.fhir.data.structdef.tree.tidy;

import java.util.stream.StreamSupport;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.MaybePrimitive;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

public class RedundantValueNodeRemover<
	T extends HasPath & MaybePrimitive, U extends TreeNode<T, U>, 
	V extends HasBackupNode<T, U>, W extends TreeNode<V, W>> {
	
	private final FhirTreeData<V, W> differentialTreeData;

	public RedundantValueNodeRemover(FhirTreeData<V, W> differentialTreeData) {
		this.differentialTreeData = differentialTreeData;
	}

	public void process(FhirTreeData<T, U> snapshotTreeData) {
		stripExpandedPrimitiveValueNodes(snapshotTreeData.getRoot());
	}

	/**
	 * When the Forge tool steps into a primitive node (e.g. to add an extension to it) it explicitly expands the constraints
	 * on the primitive type. This doesn't contain extra information and the primitive type doesn't get removed from the 
	 * original element.
	 * 
	 * We hit this in our Patient profile where the birthTime extension is added to birthDate and the 'date' constraints get 
	 * added as a birthDate.value node.
	 * 
	 * We should remove these nodes since the information is redundant (should probably appear as a constraint on the element if anything?)
	 * and there isn't a sensible 'type' to assign to them, so they look peculiar in the tree. Simplifier seems not to display them.
	 */
	private void stripExpandedPrimitiveValueNodes(U node) {
		for (int i=node.getChildren().size()-1; i>=0; i--) {
			U child = node.getChildren().get(i);

			if (child.getData().getPathName().equals("value")
			  && child.getParent() != null
			  && child.getParent().getChildren().size() > 1
			  && child.getParent().getData().isPrimitive()
			  && !isDifferentialBackupNode(child)) {
				node.getChildren().remove(i);
			} else {
				stripExpandedPrimitiveValueNodes(child);
			}
		}
	}

	private boolean isDifferentialBackupNode(U candidateForRemoval) {
		return StreamSupport.stream(differentialTreeData.spliterator(), false)
			.anyMatch(differentialNode -> differentialNode.getBackupNode().equals(candidateForRemoval));
	}
}
