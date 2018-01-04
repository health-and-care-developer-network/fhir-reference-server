package uk.nhs.fhir.render.tree.cache;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeNode;

public class SlicingDiscriminatorCacher {
	
	private final FhirTreeData<AbstractFhirTreeTableContent> treeData;
	
	public SlicingDiscriminatorCacher(FhirTreeData<AbstractFhirTreeTableContent> treeData) {
		this.treeData = treeData;
	}
	
	public void resolve() {
		for (AbstractFhirTreeTableContent content : treeData) {
			if (content instanceof FhirTreeNode) {
				FhirTreeNode node = (FhirTreeNode)content;
				node.cacheSlicingDiscriminator();
			}
		}
	}
}
