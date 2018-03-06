package uk.nhs.fhir.data.structdef.tree;

public abstract class CloneableTreeNode<T, U extends CloneableTreeNode<T, U>> extends TreeNode<T, U> {

	protected abstract U cloneShallow(U newParent);
	
	public CloneableTreeNode(T data) {
		super(data);
	}
	
	public CloneableTreeNode(T data, U parent) {
		super(data, parent);
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
