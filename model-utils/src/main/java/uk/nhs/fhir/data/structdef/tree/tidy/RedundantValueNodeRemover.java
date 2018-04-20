package uk.nhs.fhir.data.structdef.tree.tidy;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.HasNodeKey;
import uk.nhs.fhir.data.structdef.tree.MaybePrimitive;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

public class RedundantValueNodeRemover<
	T extends HasBackupNode<V, W>, U extends TreeNode<T, U>, 
	V extends HasPath & MaybePrimitive, W extends TreeNode<V, W> & HasNodeKey> extends AbstractUnchangedElementRemover<T,U,V,W> {

	public RedundantValueNodeRemover(FhirTreeData<T, U> differentialTreeData) {
		super(differentialTreeData);
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

	@Override
	protected boolean parentMatches(W parentOfRemovalCandidate) {
		return true;
	}

	@Override
	protected boolean childMatches(W child) {
		boolean matches = child.getPathName().equals("value")
		  && child.getParent() != null
		  && child.getParent().getChildren().size() > 1
		  && child.getParent().getData().isPrimitive();
		return matches;
	}
}
