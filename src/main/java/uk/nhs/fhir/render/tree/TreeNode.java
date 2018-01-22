package uk.nhs.fhir.render.tree;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A base class for types which can be consumed and turned into a tree by a FhirTreeDataBuilder.
 * Primarily to allow mocking of nodes.
 */
public abstract class TreeNode<T, U extends TreeNode<T, U>> {
	
	public abstract T getData();
	public abstract String getPath();

	private final List<U> children = Lists.newArrayList();
	private U parent;

	public TreeNode() {
		this(null);
	}
	
	public TreeNode(U parent) {
		this.parent = parent;
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
}
