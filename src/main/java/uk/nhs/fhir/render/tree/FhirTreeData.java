package uk.nhs.fhir.render.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.nhs.fhir.util.StringUtil;

public class FhirTreeData implements Iterable<AbstractFhirTreeTableContent> {
	private static Logger LOG = LoggerFactory.getLogger(FhirTreeData.class);
	
	private final AbstractFhirTreeTableContent root;
	
	public FhirTreeData(AbstractFhirTreeTableContent root) {
		Preconditions.checkNotNull(root);
		
		this.root = root;
	}

	public AbstractFhirTreeTableContent getRoot() {
		return root;
	}

	@Override
	public Iterator<AbstractFhirTreeTableContent> iterator() {
		return new FhirTreeIterator (this);
	}
	
	public void dumpTreeStructure() {
		for (AbstractFhirTreeTableContent node : this) {
			int indentSize = node.getPath().split("\\.").length - 1;
			String indent = StringUtil.nChars(indentSize, '\t');
			LOG.debug(indent + node.getDisplayName());
		}
	}

	

	public void cacheSlicingDiscriminators() {
		for (AbstractFhirTreeTableContent content : this) {
			if (content instanceof FhirTreeNode) {
				FhirTreeNode node = (FhirTreeNode)content;
				node.cacheSlicingDiscriminator();
			}
		}
	}
}

class FhirTreeIterator implements Iterator<AbstractFhirTreeTableContent> {

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
	public AbstractFhirTreeTableContent next() {
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

	private AbstractFhirTreeTableContent supplyRoot() {
		AbstractFhirTreeTableContent root = data.getRoot();
		chain.add(new FhirNodeAndChildIndex(root));
		return root;
	}
	
	private AbstractFhirTreeTableContent nextChildOfCurrent() {
		AbstractFhirTreeTableContent child = chain.getLast().nextChild();
		chain.add(new FhirNodeAndChildIndex(child));
		return child;
	}
	
	private class FhirNodeAndChildIndex {
		private final AbstractFhirTreeTableContent node;
		private Integer currentChildIndex;
		
		FhirNodeAndChildIndex(AbstractFhirTreeTableContent node) {
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
		
		public AbstractFhirTreeTableContent nextChild() {
			if (currentChildIndex == null) {
				currentChildIndex = 0;
			} else {
				currentChildIndex++;
			}
			
			return node.getChildren().get(currentChildIndex);
		}
	}
}