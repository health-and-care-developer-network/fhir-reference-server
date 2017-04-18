package uk.nhs.fhir.makehtml.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

public class FhirTreeData implements Iterable<FhirTreeNode> {
	private final FhirTreeNode root;
	
	public FhirTreeData(FhirTreeNode root) {
		Preconditions.checkNotNull(root);
		
		this.root = root;
	}
	
	public FhirTreeNode getRoot() {
		return root;
	}

	@Override
	public Iterator<FhirTreeNode> iterator() {
		return new FhirTreeIterator (this);
	}
}

class FhirTreeIterator implements Iterator<FhirTreeNode> {

	// Each node down the tree to the current node
	List<FhirTreeNode> chain = new ArrayList<>();
	// Index of the current child for the node with the matching index
	List<Integer> childIndexes = new ArrayList<>();
	
	private final FhirTreeData data;
	
	public FhirTreeIterator(FhirTreeData data) {
		Preconditions.checkNotNull(data);
		this.data = data;
	}

	@Override
	public boolean hasNext() {
		if (!returnedRoot()) {
			return true;
		}
		
		if (current().hasChildren()) {
			return true;
		}

		// true if any node in the chain to the current node has further children to offer
		for (int i=0; i<=chain.size()-2; i++) {
			if (nodeHasNextChild(i)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean returnedRoot() {
		return !chain.isEmpty();
	}

	@Override
	public FhirTreeNode next() {
		if (!returnedRoot()) {
			return supplyRoot();
		}
		
		if (current().hasChildren()) {
			return firstChildOfCurrent();
		}
		
		// Iterate back from end, find first with more children to offer 
		int parentOfCurrentIndex = chain.size()-2; 
		for (int nodeIndex=parentOfCurrentIndex; nodeIndex>=0; nodeIndex--) {
			if (nodeHasNextChild(nodeIndex)) {
				// update chain
				int targetSize = nodeIndex + 1;
				while (chain.size() > targetSize) {
					chain.remove(maxChainIndex());
				}
				
				// increment index entry
				int nextChildIndex = childIndexes.get(nodeIndex)+1;
				childIndexes.set(nodeIndex, nextChildIndex);

				// add new node to chain and return
				FhirTreeNode nextChildNode = chain.get(nodeIndex).getChildren().get(childIndexes.get(nodeIndex));
				chain.add(nextChildNode);
				return nextChildNode;
			}
		}
		
		throw new NoSuchElementException();
	}
	
	private int maxChainIndex() {
		return chain.size() - 1;
	}

	private FhirTreeNode supplyRoot() {
		FhirTreeNode root = data.getRoot();
		chain.add(root);
		return root;
	}
	
	private FhirTreeNode firstChildOfCurrent() {
		childIndexes.add(new Integer(0));
		FhirTreeNode child = current().getChildren().get(0);
		chain.add(child);
		return child;
	}

	private FhirTreeNode current() {
		int lastNodeIndex = chain.size()-1;
		return chain.get(lastNodeIndex);
	}
	
	private boolean nodeHasNextChild(int nodeIndex) {
		int currentChildIndex = childIndexes.get(nodeIndex);
		int maxChildIndex = getNodeChildCount(nodeIndex) - 1; 
		return maxChildIndex > currentChildIndex;
	}
	
	private int getNodeChildCount(int nodeIndex) {
		return chain.get(nodeIndex).getChildren().size();
	}
}