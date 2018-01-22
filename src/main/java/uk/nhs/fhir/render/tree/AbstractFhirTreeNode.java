package uk.nhs.fhir.render.tree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import uk.nhs.fhir.util.ListUtils;

public abstract class AbstractFhirTreeNode<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> extends TreeNode<T, U> {

	public abstract String getNodeKey();
	
	public AbstractFhirTreeNode() {
	}
	
	public AbstractFhirTreeNode(U parent) {
		super(parent);
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
	
	public Optional<U> findUniqueDescendantMatchingPath(String relativePath) {
		String fullPath = getPath() + "." + relativePath;
		
		List<U> descendantsMatchingDiscriminatorPath = 
			StreamSupport.stream(descendants().spliterator(), false)
				.filter(descendant -> descendant.getPath().equals(fullPath))
				.collect(Collectors.toList());
		
		return ListUtils.uniqueIfPresent(descendantsMatchingDiscriminatorPath, 
			"descendants matching discriminator " + fullPath + " for element at " + getPath());
	}

	private boolean isDirectlyRemovedByProfile() {
		return getData().getMax().equals(Optional.of("0"));
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

	public boolean hasSlicingSibling() {
		return getParent() != null 
		  && getParent()
		  		.getChildren()
		  		.stream()
				.anyMatch(child -> child != this && child.getPath().equals(getPath()) && child.getData().hasSlicingInfo());
	}

}
