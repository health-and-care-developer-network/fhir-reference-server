package uk.nhs.fhir.render.tree;

public class FhirTreeDataFactory implements TreeDataFactory<AbstractFhirTreeTableContent> {

	@Override
	public FhirTreeData<AbstractFhirTreeTableContent> create(AbstractFhirTreeTableContent rootNode) {
		return new FhirTreeData<>(rootNode);
	}

}
