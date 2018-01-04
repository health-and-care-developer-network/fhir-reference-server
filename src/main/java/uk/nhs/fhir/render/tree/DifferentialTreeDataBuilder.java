package uk.nhs.fhir.render.tree;

public class DifferentialTreeDataBuilder<T extends TreeContent<T>> extends FhirTreeDataBuilder<T> {

	private final DummyNodeFactory<T> dummyNodeFactory;
	
	public DifferentialTreeDataBuilder(DummyNodeFactory<T> dummyNodeFactory) {
		this.dummyNodeFactory = dummyNodeFactory;
	}
	
	@Override
	protected void stepToNonAncestorPath(NodePath targetPath) {
		// trim path back until it only has nodes in common with the target path
		while (!targetPath.isSubpath(path)) {
			stepOut();
		}
		
		// add dummy nodes until we reach the target path
		for (int i=path.size(); i<targetPath.size(); i++) {
			path.stepInto(targetPath.getPart(i));

			appendNode(dummyNodeFactory.create(currentNode, path));
		}
	}
}
