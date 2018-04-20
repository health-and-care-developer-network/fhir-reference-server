package uk.nhs.fhir.data.structdef.tree.tidy;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.HasNodeKey;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

/**
 * Takes a FHIR tree data and its differential data. Removes any children of slice nodes if they are not modified 
 * from the base resource (i.e. not in the differential) and have no descendants that are modified from the base resource.
 * 
 * Since it relies on differential nodes being aware of their backup nodes, to keep interesting nodes in the tree,
 * must happen after backup nodes have been assigned.
 */
public class UnchangedSliceInfoRemover<
		T extends HasBackupNode<V, W>, U extends TreeNode<T, U>, 
		V extends HasSlicingInfo, W extends TreeNode<V, W> & HasNodeKey>  extends AbstractUnchangedElementRemover<T,U,V,W>{

	public UnchangedSliceInfoRemover(FhirTreeData<T, U> differentialTree) {
		super(differentialTree);
	}

	@Override
	protected boolean parentMatches(W parentOfRemovalCandidate) {
		return parentOfRemovalCandidate.getData().hasSlicingInfo();
	}

	@Override
	protected boolean childMatches(W child) {
		return true;
	}
}
