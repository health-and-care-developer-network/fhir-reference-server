package uk.nhs.fhir.data.structdef.tree.tidy;

import java.util.List;
import java.util.stream.StreamSupport;

import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.HasNodeKey;
import uk.nhs.fhir.data.structdef.tree.TreeNode;

public abstract class AbstractUnchangedElementRemover<
	T extends HasBackupNode<V, W>, U extends TreeNode<T, U>, 
	V, W extends TreeNode<V, W> & HasNodeKey> {

	protected abstract boolean parentMatches(W parentOfRemovalCandidate);
	protected abstract boolean childMatches(W child);
	
	protected final FhirTreeData<T, U> differentialTree;
	
	public AbstractUnchangedElementRemover(FhirTreeData<T, U> differentialTree) {
		this.differentialTree = differentialTree;
	}
	
	public void process(FhirTreeData<V, W> snapshotTreeData) {
		processDescendants(snapshotTreeData.getRoot());
	}
	
	private void processDescendants(W parentOfRemovalCandidate) {
		if (parentMatches(parentOfRemovalCandidate)) {
			removeMatchingChildren(parentOfRemovalCandidate);
		}
		
		for (W child : parentOfRemovalCandidate.getChildren()) {
			processDescendants(child);
		}
	}
	
	private void removeMatchingChildren(W parentOfRemovalCandidate) {
		List<W> children = parentOfRemovalCandidate.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			W child = children.get(i);
			if (childMatches(child)
			  && !isDifferentialBackupNode(child)) {
				children.remove(i);
			}
		}
	}

	private boolean isDifferentialBackupNode(W candidateForRemoval) {
		String candidateKey = candidateForRemoval.getNodeKey();
		return StreamSupport.stream(differentialTree.spliterator(), false)
			.anyMatch(differentialNode -> differentialNode.getBackupNode().getNodeKey().equals(candidateKey));
	}
	
}
