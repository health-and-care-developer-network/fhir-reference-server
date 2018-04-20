package uk.nhs.fhir.data.structdef.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

/**
 * T is the type of the data held by the TreeNode
 * U is the class extending TreeNode that the tree consists of
 */
public class FhirTreeData<T, U extends TreeNode<T, U>> implements Iterable<T> {
	
	private final U root;
	
	public FhirTreeData(U root) {
		this.root = Preconditions.checkNotNull(root);
	}

	public U getRoot() {
		return root;
	}

	@Override
	public Iterator<T> iterator() {
		return new TreeIterator<T, U> (this);
	}
	
	public Iterable<U> nodes() {
		return new TreeNodeIterable<T, U> (this);
	}
}

class TreeNodeIterable<T, U extends TreeNode<T, U>> implements Iterable<U> {

	private final FhirTreeData<T, U> data;
	
	public TreeNodeIterable(FhirTreeData<T, U> data) {
		this.data = data;
	}
	
	@Override
	public Iterator<U> iterator() {
		return new TreeNodeIterator<T, U>(data);
	}
	
}

/**
 * A depth-first iterator over every node in a tree, returning each node's data object.
 * Maintains a list of nodes ('chain') which may still have children to offer.
 * 
 * T and U are as above
 * S is the type of data supplied by the iterator
 */
abstract class AbstractTreeIterator<T, S, U extends TreeNode<T, U>> implements Iterator<S> {

	protected abstract S supply(U node);
	
	// Each node down the tree to the current node
	Deque<NodeAndChildIndex<T, U>> chain = new ArrayDeque<>();
	
	private final FhirTreeData<T, U> data;
	
	public AbstractTreeIterator(FhirTreeData<T, U> data) {
		this.data = Preconditions.checkNotNull(data);
	}

	@Override
	public boolean hasNext() {
		if (!returnedRoot()) {
			return true;
		}

		// true if any node in the chain to the current node has further children to offer
		for (NodeAndChildIndex<T, U> node : chain) {
			if (node.hasMoreChildren()) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean returnedRoot() {
		return !chain.isEmpty();
	}
	
	/**
	 * returns the next child of the last node in that chain that still has children to offer, stripping off any that are fully consumed.
	 */
	@Override
	public S next() {
		if (!returnedRoot()) {
			return supplyRoot();
		}
		
		while (true) {
			if (chain.getLast().hasMoreChildren()) {
				return nextChildOfCurrent();
			} else if (chain.size() > 1) {
				chain.removeLast();
			} else {
				// Only root node remains, but it has no more children. Fully consumed.
				throw new NoSuchElementException();
			}
		}
	}

	private S supplyRoot() {
		U root = data.getRoot();
		chain.add(new NodeAndChildIndex<>(root));
		return supply(root);
	}
	
	private S nextChildOfCurrent() {
		U child = chain.getLast().nextChild();
		chain.add(new NodeAndChildIndex<>(child));
		return supply(child);
	}

	/**
	 * Wrapper around a node, holding a reference to the index, allowing iteration over an individual
	 * node's children.
	 */
	private static class NodeAndChildIndex<T, U extends TreeNode<T, U>> {
		private final TreeNode<T, U> node;
		private Integer currentChildIndex;
		
		public NodeAndChildIndex(TreeNode<T, U> node) {
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
		
		public U nextChild() {
			if (currentChildIndex == null) {
				currentChildIndex = 0;
			} else {
				currentChildIndex++;
			}
			
			return (U)node.getChildren().get(currentChildIndex);
		}
	}
}

class TreeIterator<T, U extends TreeNode<T, U>> extends AbstractTreeIterator<T, T, U> {

	public TreeIterator(FhirTreeData<T, U> data) {
		super(data);
	}

	protected T supply(U node) {
		return node.getData();
	}
}

class TreeNodeIterator<T, U extends TreeNode<T, U>> extends AbstractTreeIterator<T, U, U> {

	public TreeNodeIterator(FhirTreeData<T, U> data) {
		super(data);
	}
	
	@Override
	protected U supply(U node) {
		return node;
	}
}