package uk.nhs.fhir.render.tree;

public class SnapshotTreeDataBuilder<T extends TreeContent<T>> extends FhirTreeDataBuilder<T> {
	
	protected void stepToNonAncestorPath(NodePath targetPath) {
		throw new IllegalArgumentException("Cannot step out from " + path.toPathString() + " to " + targetPath.toPathString());
	}
}
