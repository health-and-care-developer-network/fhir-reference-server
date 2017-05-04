package uk.nhs.fhir.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IterableNodeList implements Iterable<Node> {

	private final NodeList nodes;
	
	public IterableNodeList(NodeList nodes) {
		this.nodes = nodes;
	}
	
	@Override
	public Iterator<Node> iterator() {
		return new NodeListIterator();
	}
	
	private class NodeListIterator implements Iterator<Node> {

		private int nextIndex=0;
		
		@Override
		public boolean hasNext() {
			return nodes.getLength() > nextIndex;
		}

		@Override
		public Node next() {
			Node nextNode = null;
			if (hasNext()) {
				nextNode = nodes.item(nextIndex);
				nextIndex++;
				
				return nextNode;
			} else {
				throw new NoSuchElementException();
			}
		}
	}
}

