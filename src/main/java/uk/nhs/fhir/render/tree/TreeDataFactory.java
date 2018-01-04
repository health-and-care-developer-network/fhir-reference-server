package uk.nhs.fhir.render.tree;

public interface TreeDataFactory<T extends TreeContent<T>> {

	public FhirTreeData<T> create(T rootNode);

}
