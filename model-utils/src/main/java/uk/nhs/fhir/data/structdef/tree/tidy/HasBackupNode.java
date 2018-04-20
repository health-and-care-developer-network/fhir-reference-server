package uk.nhs.fhir.data.structdef.tree.tidy;

import uk.nhs.fhir.data.structdef.tree.TreeNode;

public interface HasBackupNode<T, U extends TreeNode<T, U>> {
	public U getBackupNode();
}
