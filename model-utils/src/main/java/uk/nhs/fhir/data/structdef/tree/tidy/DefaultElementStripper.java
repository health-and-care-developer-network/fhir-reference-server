package uk.nhs.fhir.data.structdef.tree.tidy;

import java.util.Set;

import com.google.common.collect.Sets;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.HasNodeKey;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

public class DefaultElementStripper<
	T extends HasBackupNode<V, W>, U extends TreeNode<T, U>, 
	V extends HasInformation & HasFhirVersion, W extends TreeNode<V, W> & HasNodeKey>  extends AbstractUnchangedElementRemover<T,U,V,W> {

	private static final Set<String> UNCHANGED_NONROOT_TAGS_TO_DROP = Sets.newHashSet("id");
	
	public DefaultElementStripper(FhirTreeData<T, U> treeData) {
		super(treeData);
	}

	@Override
	protected boolean parentMatches(W parentOfRemovalCandidate) {
		return !parentOfRemovalCandidate.isRoot();
	}

	@Override
	protected boolean childMatches(W child) {
		return UNCHANGED_NONROOT_TAGS_TO_DROP.contains(child.getPathName());
	}
}