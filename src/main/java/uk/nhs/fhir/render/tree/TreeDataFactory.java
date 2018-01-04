package uk.nhs.fhir.render.tree;

public interface TreeDataFactory<T> {

	public FhirTreeData create(T rootNode);

}
