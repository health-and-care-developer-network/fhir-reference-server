package uk.nhs.fhir.render.tree.tidy;

import java.util.List;

import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

public class ComplexExtensionChildrenStripper {
	
	private final FhirTreeData treeData;

	public ComplexExtensionChildrenStripper(FhirTreeData treeData) {
		this.treeData = treeData;
	}

	public void process() {
		stripComplexExtensionChildren(treeData.getRoot());
	}
	
	// Remove inlined child nodes of complex extensions
	private void stripComplexExtensionChildren(AbstractFhirTreeTableContent node) {
		boolean isComplexExtension = node.getExtensionType().isPresent() 
		  && node.getExtensionType().get().equals(ExtensionType.COMPLEX)
		  // exclude root node
		  && node.getPath().contains(".");
		
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			AbstractFhirTreeTableContent child = children.get(i);
			if (isComplexExtension) {
				children.remove(i);
			} else {
				stripComplexExtensionChildren(child);
			}
		}
	}
}
