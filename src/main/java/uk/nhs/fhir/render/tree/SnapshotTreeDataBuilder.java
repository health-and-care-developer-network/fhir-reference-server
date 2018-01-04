package uk.nhs.fhir.render.tree;

public class SnapshotTreeDataBuilder extends FhirTreeDataBuilder<AbstractFhirTreeTableContent> {
	
	protected void stepToNonAncestorPath(NodePath targetPath) {
		throw new IllegalArgumentException("Cannot step out from " + path.toPathString() + " to " + targetPath.toPathString());
	}
	
	public FhirTreeData getTree() {
		return new FhirTreeData(rootNode);
	}
}
