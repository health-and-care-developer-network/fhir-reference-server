package uk.nhs.fhir.render.tree;

public class DifferentialTreeDataBuilder extends FhirTreeDataBuilder<AbstractFhirTreeTableContent> {

	@Override
	protected void stepToNonAncestorPath(NodePath targetPath) {
		// trim path back until it only has nodes in common with the target path
		while (!targetPath.isSubpath(path)) {
			stepOut();
		}
		
		// add dummy nodes until we reach the target path
		for (int i=path.size(); i<targetPath.size(); i++) {
			path.stepInto(targetPath.getPart(i));

			appendNode(new DummyFhirTreeNode(currentNode, path.toPathString()));
		}
	}
	
	public FhirTreeData getTree() {
		return new FhirTreeData(rootNode);
	}
}
