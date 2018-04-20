package uk.nhs.fhir.data.structdef.tree;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public abstract class AbstractNodePath implements Iterable<String> {
	protected final List<String> pathParts;
	
	public AbstractNodePath(List<String> pathParts) {
		this.pathParts = Preconditions.checkNotNull(pathParts);
	}
	
	public AbstractNodePath(String nodePath) {
		String[] partsArray = nodePath.split("\\.");
		this.pathParts = Lists.newArrayList(partsArray);
	}

	public MutableNodePath mutableCopy() {
		return new MutableNodePath(Lists.newArrayList(pathParts));
	}
	
	public ImmutableNodePath immutableCopy() {
		return new ImmutableNodePath(Lists.newArrayList(pathParts));
	}
	
	public int size() {
		return pathParts.size();
	}
	
	public String getPart(int i) {
		return pathParts.get(i);
	}
	
	// Returns true if this path begin with the entirety of ancestorPath
	public boolean isSubpath(MutableNodePath ancestorPath) {
		if (ancestorPath.size() > size()) {
			return false;
		}
		
		for (int i=0; i<ancestorPath.size(); i++) {
			if (!pathParts.get(i).equals(ancestorPath.getPart(i))) {
				return false;
			}
		}
		return true;
	}
	
	public String toString() {
		return String.join(".", pathParts);
	}
	
	public String getPathName() {
		if (!pathParts.isEmpty()) {
			return pathParts.get(pathParts.size()-1);
		} else {
			return "";
		}
	}
	
	public boolean isRoot() {
		return pathParts.size() == 1;
	}
	
	public boolean equals(Object other) {
		if (other == null
		  || !(other instanceof AbstractNodePath)) {
			return false;
		}
		
		AbstractNodePath otherNodePath = (AbstractNodePath)other;
		if (otherNodePath.pathParts.size() != pathParts.size()) {
			return false;
		}
		
		for (int i=0; i<pathParts.size(); i++) {
			if (!otherNodePath.getPart(i).equals(getPart(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return pathParts.hashCode();
	}

	public boolean isEmpty() {
		return pathParts.isEmpty();
	}

	@Override
	public Iterator<String> iterator() {
		return new NodePathIterator(pathParts);
	}
}

class NodePathIterator implements Iterator<String> {

	private final List<String> pathParts;
	private int next = 0;
	
	public NodePathIterator(List<String> pathParts) {
		this.pathParts = Lists.newArrayList(pathParts);
	}
	
	@Override
	public boolean hasNext() {
		return pathParts.size() > next;
	}

	@Override
	public String next() {
		if (hasNext()) {
			String nextPart = pathParts.get(next);
			next++;
			return nextPart;
		} else {
			throw new NoSuchElementException();
		}
	}
	
}
