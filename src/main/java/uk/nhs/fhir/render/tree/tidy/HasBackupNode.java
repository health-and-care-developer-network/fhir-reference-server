package uk.nhs.fhir.render.tree.tidy;

import uk.nhs.fhir.render.tree.TreeNode;

public interface HasBackupNode<T, U extends TreeNode<T, U>> {
	public U getBackupNode();
}
