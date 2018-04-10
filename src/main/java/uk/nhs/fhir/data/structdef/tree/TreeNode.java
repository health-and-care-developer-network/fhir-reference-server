package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;

import uk.nhs.fhir.util.ListUtils;

/**
 * A base class for types which can be consumed and turned into a tree by a FhirTreeDataBuilder.
 * Primarily to allow mocking of nodes.
 */
public abstract class TreeNode<T, U extends TreeNode<T, U>> {
	
	private final T data;

	private final List<U> children = Lists.newArrayList();
	private final ImmutableNodePath path;
	private U parent;

	public TreeNode(T data, ImmutableNodePath path) {
		this(data, path, null);
	}
	
	public TreeNode(T data, ImmutableNodePath path, U parent) {
		this.data = data;
		this.path = path;
		this.parent = parent;
	}
	
	public T getData() {
		return data;
	}

	public List<U> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public ImmutableNodePath getPath() {
		return path;
	}
	
	public String getPathString() {
		return path.toString();
	}

	public String getPathName() {
		return path.getPathName();
	}

	public boolean isRoot() {
		return path.isRoot();
	}

	void setParent(U parent) {
		this.parent = parent;
	}
	
	public U getParent() {
		return parent;
	}
	
	@SuppressWarnings("unchecked")
	public U getRoot() {
		U ancestor = (U)this;
		while (!ancestor.isRoot()) {
			ancestor = ancestor.getParent();
		}
		return ancestor;
	}
	
	@SuppressWarnings("unchecked")
	public void addChild(U child) {
		children.add(child);
		child.setParent((U) this);
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<U> descendants() {
		return new FhirTreeData<T, U>((U)this).nodes();
	}

	public List<U> descendantsWithPath(AbstractNodePath absolutePath) {
		return nodesWithPath(descendants(), absolutePath);
	}
	
	@SuppressWarnings("unchecked")
	public List<U> selfOrChildrenWithPath(AbstractNodePath absolutePath) {
		if (getPath().equals(absolutePath)) {
			return Lists.newArrayList((U)this);
		}
		return nodesWithPath(getChildren(), absolutePath);
	}
	
	private List<U> nodesWithPath(Iterable<U> nodeIterable, AbstractNodePath absolutePath) {
		List<U> withPath = Lists.newArrayList();
		
		for (U child : nodeIterable) {
			if (child.getPath().equals(absolutePath)) {
				withPath.add(child);
			}
		}
		
		return withPath;
	}
	
	public Optional<U> findUniqueDescendantMatchingPath(String relativePath) {
		String fullPath = getPath() + "." + relativePath;
		
		List<U> descendantsMatchingDiscriminatorPath = 
			StreamSupport.stream(descendants().spliterator(), false)
				.filter(descendant -> descendant.getPathString().equals(fullPath))
				.collect(Collectors.toList());
		
		return ListUtils.uniqueIfPresent(descendantsMatchingDiscriminatorPath, 
			"descendants matching discriminator " + fullPath + " for element at " + getPath());
	}
	
	public List<U> getSiblings() {
		if (getParent() == null) {
			// tree root
			return Lists.newArrayList();
		}
		
		return 
			getParent()
				.getChildren()
				.stream()
				.filter(child -> child != this)
				.collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return getPath().toString();
	}
	
	public boolean isDescendantOf(U possibleAncestor) {
		U ancestor = parent;
		while (ancestor != null) {
			if (ancestor.equals(possibleAncestor)) {
				return true;
			}
			ancestor = ancestor.getParent();
		}
		
		return false;
	}
}
