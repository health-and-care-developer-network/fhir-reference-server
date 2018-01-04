package uk.nhs.fhir.render.format.structdef;

import java.util.stream.StreamSupport;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

public class RedundantValueNodeRemover {

	private final FhirTreeData<AbstractFhirTreeTableContent> differentialTreeData;

	public RedundantValueNodeRemover(FhirTreeData<AbstractFhirTreeTableContent> differentialTreeData) {
		this.differentialTreeData = differentialTreeData;
	}

	public void process(FhirTreeData<AbstractFhirTreeTableContent> snapshotTreeData) {
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
	private void stripExpandedPrimitiveValueNodes(AbstractFhirTreeTableContent node) {
		for (int i=node.getChildren().size()-1; i>=0; i--) {
			AbstractFhirTreeTableContent child = node.getChildren().get(i);

			if (child.getPathName().equals("value")
			  && child.getParent() != null
			  && child.getParent().getChildren().size() > 1
			  && child.getParent().isPrimitive()
			  && !isDifferentialBackupNode(child)) {
				node.getChildren().remove(i);
			} else {
				stripExpandedPrimitiveValueNodes(child);
			}
		}
	}

	private boolean isDifferentialBackupNode(AbstractFhirTreeTableContent candidateForRemoval) {
		return StreamSupport.stream(differentialTreeData.spliterator(), false)
			.anyMatch(differentialNode -> differentialNode.getBackupNode().get().equals(candidateForRemoval));
	}
}
