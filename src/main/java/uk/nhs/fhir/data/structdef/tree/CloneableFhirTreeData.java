package uk.nhs.fhir.data.structdef.tree;

public class CloneableFhirTreeData<T, U extends CloneableTreeNode<T, U>> extends FhirTreeData<T, U> {

	public CloneableFhirTreeData(U root) {
		super(root);
	}

	public CloneableFhirTreeData(FhirTreeData<T, U> tree) {
		this(tree.getRoot());
	}

	public CloneableFhirTreeData<T, U> shallowCopy() {
		return new CloneableFhirTreeData<>(getRoot().cloneSubtreeShallow(null));
	}

	
}
