package uk.nhs.fhir.makehtml.data;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Takes a FHIR tree data and its differential data. Removes any children of slice nodes if they are not modified 
 * from the base resource (i.e. not in the differential) and have no descendants that are modified from the base resource. 
 */
public class UnchangedSliceInfoRemover {
	
	private final FhirTreeData differentialTree;

	public UnchangedSliceInfoRemover(FhirTreeData differentialTree) {
		this.differentialTree = differentialTree;
	}
	
	public void process(FhirTreeData treeToModify) {
		removeNonDifferentialSlicingChildren(treeToModify.getRoot());
	}
	
	public void removeNonDifferentialSlicingChildren(FhirTreeTableContent currentNode) {
		
		if (currentNode.hasSlicingInfo()) {
			removeNonDifferentialDescendants(currentNode);
		} else {
			for (FhirTreeTableContent child : currentNode.getChildren()) {
				removeNonDifferentialSlicingChildren(child);
			}
		}
	}
	
	private void removeNonDifferentialDescendants(FhirTreeTableContent node) {
		List<? extends FhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			FhirTreeTableContent child = children.get(i);
			if (!isDifferentialBackupNode(child)) {
				children.remove(i);
			} else {
				removeNonDifferentialDescendants(child);
			}
		}
	}

	private boolean isDifferentialBackupNode(FhirTreeTableContent candidateForRemoval) {
		return StreamSupport.stream(differentialTree.spliterator(), false)
			.anyMatch(differentialNode -> differentialNode.getBackupNode().get().equals(candidateForRemoval));
	}
}
