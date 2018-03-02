package uk.nhs.fhir.render.tree;

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
	
	public abstract String getPath();

	private final List<U> children = Lists.newArrayList();
	private U parent;

	public TreeNode(T data) {
		this(data, null);
	}
	
	public TreeNode(T data, U parent) {
		this.data = data;
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

	public void setParent(U parent) {
		this.parent = parent;
	}
	
	public U getParent() {
		return parent;
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

	public List<U> descendantsWithPath(String absolutePath) {
		return StreamSupport
			.stream(descendants().spliterator(), false)
			.filter(node -> node.getPath().equals(absolutePath))
			.collect(Collectors.toList());
	}
	
	public Optional<U> findUniqueDescendantMatchingPath(String relativePath) {
		String fullPath = getPath() + "." + relativePath;
		
		List<U> descendantsMatchingDiscriminatorPath = 
			StreamSupport.stream(descendants().spliterator(), false)
				.filter(descendant -> descendant.getPath().equals(fullPath))
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
		return getPath();
	}
}
