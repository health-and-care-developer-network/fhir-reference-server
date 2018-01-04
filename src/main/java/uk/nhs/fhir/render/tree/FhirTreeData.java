package uk.nhs.fhir.render.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

public class FhirTreeData<T extends TreeContent<T>> implements Iterable<T> {
	
	private final T root;
	
	public FhirTreeData(T root) {
		Preconditions.checkNotNull(root);
		
		this.root = root;
	}

	public T getRoot() {
		return root;
	}

	@Override
	public Iterator<T> iterator() {
		return new TreeIterator<T> (this);
	}
}

class TreeIterator<T extends TreeContent<T>> implements Iterator<T> {

	// Each node down the tree to the current node
	Deque<NodeAndChildIndex> chain = new ArrayDeque<>();
	
	private final FhirTreeData<T> data;
	
	public TreeIterator(FhirTreeData<T> data) {
		Preconditions.checkNotNull(data);
		this.data = data;
	}

	@Override
	public boolean hasNext() {
		if (!returnedRoot()) {
			return true;
		}

		// true if any node in the chain to the current node has further children to offer
		for (NodeAndChildIndex node : chain) {
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
	public T next() {
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
	}

	private T supplyRoot() {
		T root = data.getRoot();
		chain.add(new NodeAndChildIndex(root));
		return root;
	}
	
	private T nextChildOfCurrent() {
		T child = chain.getLast().nextChild();
		chain.add(new NodeAndChildIndex(child));
		return child;
	}
	
	private class NodeAndChildIndex {
		private final T node;
		private Integer currentChildIndex;
		
		NodeAndChildIndex(T node) {
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
		
		public T nextChild() {
			if (currentChildIndex == null) {
				currentChildIndex = 0;
			} else {
				currentChildIndex++;
			}
			
			return node.getChildren().get(currentChildIndex);
		}
	}
}