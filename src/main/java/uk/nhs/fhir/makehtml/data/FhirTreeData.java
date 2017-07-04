package uk.nhs.fhir.makehtml.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

public class FhirTreeData implements Iterable<FhirTreeTableContent> {
	private final FhirTreeTableContent root;
	
	public FhirTreeData(FhirTreeTableContent root) {
		Preconditions.checkNotNull(root);
		
		this.root = root;
	}
	
	public FhirTreeTableContent getRoot() {
		return root;
	}

	@Override
	public Iterator<FhirTreeTableContent> iterator() {
		return new FhirTreeIterator (this);
	}
	
	public void dumpTreeStructure() {
		for (FhirTreeTableContent node : this) {
			for (int i=0; i < (node.getPath().split("\\.").length - 1); i++) {
				System.out.write('\t');
			}
			System.out.println(node.getDisplayName());
		}
	}

	public void stripRemovedElements() {
		stripRemovedElements(root);
	}
	
	/**
	 * Remove all nodes that have cardinality max = 0 (and their children)
	 */
	private void stripRemovedElements(FhirTreeTableContent node) {
		List<? extends FhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			FhirTreeTableContent child = children.get(i);
			
			if (child.isRemovedByProfile()) {
				children.remove(i);
			} else {
				stripRemovedElements(child);
			}
		}
	}
}

class FhirTreeIterator implements Iterator<FhirTreeTableContent> {

	// Each node down the tree to the current node
	Deque<FhirNodeAndChildIndex> chain = new ArrayDeque<>();
	
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

		// true if any node in the chain to the current node has further children to offer
		for (FhirNodeAndChildIndex node : chain) {
			if (node.hasMoreChildren()) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean returnedRoot() {
		return !chain.isEmpty();
	}

	@Override
	public FhirTreeTableContent next() {
		if (!returnedRoot()) {
			return supplyRoot();
		}
		
		while (true) {
			if (chain.getLast().hasMoreChildren()) {
				return nextChildOfCurrent();
			} else if (chain.size() > 1) {
				chain.removeLast();
			} else {
				throw new NoSuchElementException();
			}
		}
		
		/*int parentOfCurrentIndex = chain.size()-2;
		for (int nodeIndex=parentOfCurrentIndex; nodeIndex>=0; nodeIndex--) {
			FhirNodeAndChildIndex lastNode = chain.get(nodeIndex);
			if (nodeHasNextChild(nodeIndex)) {
				// update chain
				int targetSize = nodeIndex + 1;
				while (chain.size() > targetSize) {
					int currentNodeIndex = maxChainIndex();
					chain.remove(currentNodeIndex);
					if (childIndexes.size() > currentNodeIndex) {
						childIndexes.remove(childIndexes.size()-1);
					}
				}
				
				// increment index entry
				int nextChildIndex = childIndexes.get(nodeIndex)+1;
				childIndexes.set(nodeIndex, nextChildIndex);

				// add new node to chain and return
				FhirTreeTableContent nextChildNode = fhirNodeAndChildIndex.getChildren().get(childIndexes.get(nodeIndex));
				chain.add(nextChildNode);
				return nextChildNode;
			}
		}
		
		throw new NoSuchElementException();*/
	}

	private FhirTreeTableContent supplyRoot() {
		FhirTreeTableContent root = data.getRoot();
		chain.add(new FhirNodeAndChildIndex(root));
		return root;
	}
	
	private FhirTreeTableContent nextChildOfCurrent() {
		FhirTreeTableContent child = chain.getLast().nextChild();
		chain.add(new FhirNodeAndChildIndex(child));
		return child;
	}
	
	private class FhirNodeAndChildIndex {
		private final FhirTreeTableContent node;
		private Integer currentChildIndex;
		
		FhirNodeAndChildIndex(FhirTreeTableContent node) {
			this.node = node;
			this.currentChildIndex = null;
		}

		public boolean hasMoreChildren() {
			if (node.getChildren().isEmpty()) {
				return false;
			} else if (currentChildIndex == null) {
				return true;
			} else {
				int maxChildIndex = node.getChildren().size()-1;
				return maxChildIndex > currentChildIndex;
			}
		}
		
		public FhirTreeTableContent nextChild() {
			if (currentChildIndex == null) {
				currentChildIndex = 0;
			} else {
				currentChildIndex++;
			}
			
			return node.getChildren().get(currentChildIndex);
		}
	}
}