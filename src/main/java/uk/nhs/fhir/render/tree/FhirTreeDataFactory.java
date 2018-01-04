package uk.nhs.fhir.render.tree;

public class FhirTreeDataFactory implements TreeDataFactory<AbstractFhirTreeTableContent> {

	@Override
	public FhirTreeData create(AbstractFhirTreeTableContent rootNode) {
		return new FhirTreeData(rootNode);
	}

}
