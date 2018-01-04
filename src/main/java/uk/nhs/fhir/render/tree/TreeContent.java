package uk.nhs.fhir.render.tree;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * An interface for types which can be consumed and turned into a tree by a FhirTreeDataBuilder.
 * Primarily to allow mocking of nodes.
 */
public abstract class TreeContent<T extends TreeContent<T>> implements TreeContentI<T> {

	private final List<T> children = Lists.newArrayList();
	private T parent;

	public TreeContent() {
		this.parent = null;
	}
	
	public TreeContent(T parent) {
		this.parent = parent;
	}

	public List<? extends T> getChildren() {
		return children;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}
	
	public T getParent() {
		return parent;
	}
	
	@SuppressWarnings("unchecked")
	public void addChild(T child) {
		children.add(child);
		child.setParent((T) this);
	}

}
