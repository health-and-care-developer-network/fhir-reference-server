package uk.nhs.fhir.data.structdef.tree;

public abstract class CloneableTreeNode<T, U extends CloneableTreeNode<T, U>> extends TreeNode<T, U> {

	public abstract String getNodeKey();
	protected abstract U cloneShallow(U newParent);
	
	public CloneableTreeNode(T data, ImmutableNodePath path) {
		super(data, path);
	}
	
	public CloneableTreeNode(T data, ImmutableNodePath path, U parent) {
		super(data, path, parent);
	}
	
	public U cloneSubtreeShallow(U newParent) {
		U newNode = cloneShallow(newParent);
		
		for (U child : getChildren()) {
			U newChildNode = child.cloneSubtreeShallow(newNode);
			newNode.addChild(newChildNode);
		}
		
		return newNode;
	}
}
