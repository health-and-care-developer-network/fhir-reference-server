package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractFhirTreeNode<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> extends CloneableTreeNode<T, U> {

	public abstract String getNodeKey();
	
	public AbstractFhirTreeNode(T data) {
		super(data, data.getPath());
	}
	
	public AbstractFhirTreeNode(T data, U parent) {
		super(data, data.getPath(), parent);
	}

	public U getSlicingSibling() {
		List<U> slicingSiblings = getParent().getChildren().stream()
				.filter(child -> child.getPath().equals(getPath()) && child.getData().hasSlicingInfo())
				.collect(Collectors.toList());

		if (slicingSiblings.size() > 1) {
			throw new IllegalStateException("More than 1 sibling with slicing present for " + getPath());
		}

		return slicingSiblings.get(0);
	}

	private boolean isDirectlyRemovedByProfile() {
		return getData().getMax().equals("0");
	}

	public boolean isRemovedByProfile() {
		if (isDirectlyRemovedByProfile()) {
			return true;
		} else if (getParent() != null) {
			return getParent().isRemovedByProfile();
		} else {
			return false;
		}
	}
}
