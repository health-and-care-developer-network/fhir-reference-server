package uk.nhs.fhir.render.format.structdef;

import java.util.List;
import java.util.stream.StreamSupport;

import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

/**
 * Takes a FHIR tree data and its differential data. Removes any children of slice nodes if they are not modified 
 * from the base resource (i.e. not in the differential) and have no descendants that are modified from the base resource. 
 */
public class UnchangedSliceInfoRemover {
	
	private final FhirTreeData<AbstractFhirTreeTableContent> differentialTree;

	public UnchangedSliceInfoRemover(FhirTreeData<AbstractFhirTreeTableContent> differentialTree) {
		this.differentialTree = differentialTree;
	}
	
	public void process(FhirTreeData<AbstractFhirTreeTableContent> treeToModify) {
		removeNonDifferentialSlicingChildren(treeToModify.getRoot());
	}
	
	public void removeNonDifferentialSlicingChildren(AbstractFhirTreeTableContent currentNode) {
		
		if (currentNode.hasSlicingInfo()) {
			removeNonDifferentialDescendants(currentNode);
		} else {
			for (AbstractFhirTreeTableContent child : currentNode.getChildren()) {
				removeNonDifferentialSlicingChildren(child);
			}
		}
	}
	
	private void removeNonDifferentialDescendants(AbstractFhirTreeTableContent node) {
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			AbstractFhirTreeTableContent child = children.get(i);
			if (!isDifferentialBackupNode(child)) {
				children.remove(i);
			} else {
				removeNonDifferentialDescendants(child);
			}
		}
	}

	private boolean isDifferentialBackupNode(AbstractFhirTreeTableContent candidateForRemoval) {
		return StreamSupport.stream(differentialTree.spliterator(), false)
			.anyMatch(differentialNode -> differentialNode.getBackupNode().get().equals(candidateForRemoval));
	}
}
