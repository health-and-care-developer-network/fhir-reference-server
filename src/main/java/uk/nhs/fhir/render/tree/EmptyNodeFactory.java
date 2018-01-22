package uk.nhs.fhir.render.tree;

public interface EmptyNodeFactory<T, U extends TreeNode<T, U>> {

	U create(U currentNode, NodePath path);

}
