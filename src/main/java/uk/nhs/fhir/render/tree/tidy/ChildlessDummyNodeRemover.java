package uk.nhs.fhir.render.tree.tidy;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.DummyFhirTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeData;

/**
 * Dummy nodes may only include nodes which are subsequently removed.
 * Likewise, dummy nodes may be further up a chain of dummy nodes which eventually has any non-dummy nodes removed.
 * These should generally not be displayed.
 */
public class ChildlessDummyNodeRemover {
	private final FhirTreeData<AbstractFhirTreeTableContent> treeData;

	public ChildlessDummyNodeRemover(FhirTreeData<AbstractFhirTreeTableContent> treeData) {
		this.treeData = treeData;
	}

	public void process() {
		stripChildlessDummyNodes(treeData.getRoot());
	}

	private void stripChildlessDummyNodes(AbstractFhirTreeTableContent node) {
		for (int i=node.getChildren().size()-1; i>=0; i--) {
			AbstractFhirTreeTableContent child = node.getChildren().get(i);
			
			stripChildlessDummyNodes(child);
			
			if (child instanceof DummyFhirTreeNode
			  && child.getChildren().isEmpty()) {
				node.getChildren().remove(i);
			}
		}
	}
}
