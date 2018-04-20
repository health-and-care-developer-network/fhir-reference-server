package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import uk.nhs.fhir.data.structdef.tree.tidy.HasBackupNode;
import uk.nhs.fhir.data.structdef.tree.tidy.HasSlicingInfo;
import uk.nhs.fhir.util.ListUtils;

public class SlicingResolver {
	
	/*
	 * Searches up the tree for the first child affected by slicing, 
	 * i.e. an ancestor node with a sibling which 
	 * 		- has a matching path 
	 * 		- has slicing information.
	 * Will not return the node itself.
	 */
	public static <T extends HasSlicingInfo & HasId, U extends TreeNode<T, U>> Optional<U> getFirstSlicedParent(U node) {
		U ancestor = node.getParent();
		if (ancestor == null) {
			return Optional.empty();
		}
		
		while (ancestor != null) {
			
			if (SlicingResolver.getSlicingSibling(ancestor).isPresent()
			  || ancestor.getData().hasSlicingInfo()) {
				return Optional.of(ancestor);
			}
			
			ancestor = ancestor.getParent();
		}
		
		return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	public static < 
		V extends HasSlicingInfo & HasId, W extends TreeNode<V, W>, 
		T extends HasSlicingInfo & HasBackupNode<V, W> & HasId, U extends TreeNode<T, U>>
			Optional<W> getSlicingSibling(W node) {
		
		if (node.getData() instanceof HasBackupNode) {
			return (Optional<W>)getSlicingSiblingWithBackup((U)node);
		} else {
			return getDirectSlicingSibling(node);
		}
	}
	
	private static <T extends HasSlicingInfo, U extends TreeNode<T, U>> 
			Optional<U> getDirectSlicingSibling(U node) {

		if (node.getParent() == null) {
			return Optional.empty();
		}
		
		List<U> slicingSiblings = 
			node
				.getSiblings()
				.stream()
				.filter(child -> 
		  			child.getPath().equals(node.getPath()) 
		  			&& child.getData().hasSlicingInfo())
				.collect(Collectors.toList());
		
		return ListUtils.uniqueIfPresent(slicingSiblings, "slicing siblings");
	}
	
	private static <
		V extends HasSlicingInfo & HasId, W extends TreeNode<V, W>, 
		T extends HasSlicingInfo & HasBackupNode<V, W> & HasId, U extends TreeNode<T, U>> 
			Optional<U> getSlicingSiblingWithBackup(U node) {
		
		Optional<U> direct = SlicingResolver.getDirectSlicingSibling(node);
		Optional<W> backup = SlicingResolver.getDirectSlicingSibling(node.getData().getBackupNode());
		
		// Differentials should always include slicing information, including any info required to resolve which node
		// a differential update needs to be applied to in the base profile.
		
		// It seems that sometimes the differential doesn't include the sliced sibling, in which case the best we can
		// do is use ID to sanity check that they are correctly matched. This is done on Differential node creation.
		if (direct.isPresent() && backup.isPresent()) {
			if (!direct.get().getData().getBackupNode().equals(backup.get())) {
				throw new IllegalStateException("Direct slicing sibling's backup node doesn't match backup node's slicing sibling");
			}
		}
		
		if (direct.isPresent() && !backup.isPresent()) {
			throw new IllegalStateException("Differential slicing sibling was present, but backup node is not sliced for " + node.getPath());
		}
		
		return direct;
	}
}
