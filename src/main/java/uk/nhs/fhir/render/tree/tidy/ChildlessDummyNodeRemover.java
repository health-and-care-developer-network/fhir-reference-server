package uk.nhs.fhir.render.tree.tidy;

import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.MaybeDummy;
import uk.nhs.fhir.render.tree.TreeNode;

/**
 * Dummy nodes may only include nodes which are subsequently removed.
 * Likewise, dummy nodes may be further up a chain of dummy nodes which eventually has any non-dummy nodes removed.
 * These should generally not be displayed.
 */
public class ChildlessDummyNodeRemover<T, U extends TreeNode<T, U> & MaybeDummy> {
	private final FhirTreeData<T, U> treeData;

	public ChildlessDummyNodeRemover(FhirTreeData<T, U> treeData) {
		this.treeData = treeData;
	}

	public void process() {
		stripChildlessDummyNodes(treeData.getRoot());
	}

	private void stripChildlessDummyNodes(U node) {
		for (int i=node.getChildren().size()-1; i>=0; i--) {
			U child = node.getChildren().get(i);
			
			stripChildlessDummyNodes(child);
			
			if (child.isDummy()
			  && child.getChildren().isEmpty()) {
				node.getChildren().remove(i);
			}
		}
	}
}
