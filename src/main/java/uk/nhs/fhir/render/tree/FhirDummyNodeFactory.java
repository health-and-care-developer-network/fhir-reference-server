package uk.nhs.fhir.render.tree;

public class FhirDummyNodeFactory implements DummyNodeFactory<AbstractFhirTreeTableContent> {

	@Override
	public AbstractFhirTreeTableContent create(AbstractFhirTreeTableContent parent, NodePath path) {
		return new DummyFhirTreeNode(parent, path.toPathString());
	}

}
