package uk.nhs.fhir.render.tree;

public class SnapshotTreeDataBuilder<T extends TreeContent<T>> extends FhirTreeDataBuilder<T> {
	
	private final TreeDataFactory<T> treeDataFactory;

	public SnapshotTreeDataBuilder(TreeDataFactory<T> treeDataFactory) {
		this.treeDataFactory = treeDataFactory;
	}
	
	protected void stepToNonAncestorPath(NodePath targetPath) {
		throw new IllegalArgumentException("Cannot step out from " + path.toPathString() + " to " + targetPath.toPathString());
	}
	
	public FhirTreeData getTree() {
		return treeDataFactory.create(rootNode);
	}
}
