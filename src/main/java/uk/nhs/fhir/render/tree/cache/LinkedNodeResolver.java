package uk.nhs.fhir.render.tree.cache;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeNode;

public abstract class LinkedNodeResolver {

	protected final FhirTreeData treeData;
	
	public LinkedNodeResolver(FhirTreeData treeData) {
		this.treeData = treeData;
	}
	
	protected void setLinkedNodes(Map<String, List<AbstractFhirTreeTableContent>> expectedIds,
			Map<String, AbstractFhirTreeTableContent> nodesWithId) {
		for (Map.Entry<String, List<AbstractFhirTreeTableContent>> expectedIdEntry : expectedIds.entrySet()) {
			String expectedId = expectedIdEntry.getKey();
			List<AbstractFhirTreeTableContent> nodesWithLink = expectedIdEntry.getValue();
			
			if (nodesWithId.containsKey(expectedId)) {
				for (AbstractFhirTreeTableContent nodeWithLink : nodesWithLink) {
					if (nodeWithLink instanceof FhirTreeNode) {
						((FhirTreeNode)nodeWithLink).setLinkedNode(nodesWithId.get(expectedId));
					}
					// If we are in a dummy node, we don't need to do anything since the backup node
					// should contain this information
				}
			} else {
				String nodesWithMissingLinkTarget = String.join(", ", 
					nodesWithLink
						.stream()
						.map(node -> node.getPath())
						.collect(Collectors.toList()));
				
				EventHandlerContext.forThread().event(RendererEventType.MISSING_REFERENCED_NODE, 
					"Linked node(s) at " + nodesWithMissingLinkTarget + " missing target (" + expectedId + ")");
			}
		}
	}

	
}
