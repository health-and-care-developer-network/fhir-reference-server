package uk.nhs.fhir.makehtml.data;

public class FhirTreeData {
	private final FhirTreeNode root;
	
	public FhirTreeData(FhirTreeNode root) {
		this.root = root;
	}
	
	public FhirTreeNode getRoot() {
		return root;
	}
}