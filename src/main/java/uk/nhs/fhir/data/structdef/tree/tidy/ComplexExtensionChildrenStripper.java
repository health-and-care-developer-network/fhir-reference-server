package uk.nhs.fhir.data.structdef.tree.tidy;

import java.util.List;

import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;

public class ComplexExtensionChildrenStripper<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {
	
	private final FhirTreeData<T, U> treeData;

	public ComplexExtensionChildrenStripper(FhirTreeData<T, U> treeData) {
		this.treeData = treeData;
	}

	public void process() {
		stripComplexExtensionChildren(treeData.getRoot());
	}
	
	// Remove inlined child nodes of complex extensions
	private void stripComplexExtensionChildren(U node) {
		boolean isComplexExtension = node.getData().getExtensionType().isPresent() 
		  && node.getData().getExtensionType().get().equals(ExtensionType.COMPLEX)
		  // exclude root node
		  && node.getPath().contains(".");
		
		List<U> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			U child = children.get(i);
			if (isComplexExtension) {
				children.remove(i);
			} else {
				stripComplexExtensionChildren(child);
			}
		}
	}
}
