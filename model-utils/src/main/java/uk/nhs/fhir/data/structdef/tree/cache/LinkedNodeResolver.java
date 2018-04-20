package uk.nhs.fhir.data.structdef.tree.cache;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.SnapshotTreeNode;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;

public abstract class LinkedNodeResolver<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> {

	protected final FhirTreeData<T, U> treeData;
	
	public LinkedNodeResolver(FhirTreeData<T, U> treeData) {
		this.treeData = treeData;
	}
	
	protected void setLinkedNodes(Map<String, List<U>> expectedIds, Map<String, SnapshotTreeNode> nodesWithId) {
		for (Map.Entry<String, List<U>> expectedIdEntry : expectedIds.entrySet()) {
			String expectedId = expectedIdEntry.getKey();
			List<U> nodesWithLink = expectedIdEntry.getValue();
			
			if (nodesWithId.containsKey(expectedId)) {
				for (U nodeWithLink : nodesWithLink) {
					nodeWithLink.getData().setLinkedNode(nodesWithId.get(expectedId));
				}
			} else {
				String nodesWithMissingLinkTarget = String.join(", ", 
					nodesWithLink
						.stream()
						.map(node -> node.getPathString())
						.collect(Collectors.toList()));
				
				EventHandlerContext.forThread().event(RendererEventType.MISSING_REFERENCED_NODE, 
					"Linked node(s) at " + nodesWithMissingLinkTarget + " missing target (" + expectedId + ")");
			}
		}
	}

	
}
