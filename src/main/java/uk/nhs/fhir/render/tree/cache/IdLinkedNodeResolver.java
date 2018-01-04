package uk.nhs.fhir.render.tree.cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

public class IdLinkedNodeResolver extends LinkedNodeResolver {
	
	public IdLinkedNodeResolver(FhirTreeData<AbstractFhirTreeTableContent> treeData) {
		super(treeData);
	}
	
	public void resolve() {
		Map<String, List<AbstractFhirTreeTableContent>> expectedIds = Maps.newHashMap();
		Map<String, AbstractFhirTreeTableContent> nodesWithId = Maps.newHashMap();
		
		for (AbstractFhirTreeTableContent node : treeData) {
			
			Optional<String> id = node.getId();
			boolean hasId = id.isPresent();
			if (hasId) {
				nodesWithId.put(id.get(), node);
			}
			
			Optional<String> linkedNodeId = node.getLinkedNodeId();
			boolean hasIdLinkedNode = linkedNodeId.isPresent();
			if (hasIdLinkedNode) {
				List<AbstractFhirTreeTableContent> nodesLinkingToThisId;
				if (expectedIds.containsKey(linkedNodeId.get())) {
					nodesLinkingToThisId = expectedIds.get(linkedNodeId.get());
				} else {
					nodesLinkingToThisId = Lists.newArrayList();
					expectedIds.put(linkedNodeId.get(), nodesLinkingToThisId);
				}
				
				nodesLinkingToThisId.add(node);
			}
			
			if (hasId && hasIdLinkedNode) {
				if (node.getId().get().equals(node.getLinkedNodeId().get())) {
					EventHandlerContext.forThread().event(RendererEventType.LINK_REFERENCES_ITSELF, "Link " + node.getPath() + " references itself (" + node.getId().get() + ")");
				}
			}
			
			if (hasIdLinkedNode && node.getFixedValue().isPresent()) {
				EventHandlerContext.forThread().event(RendererEventType.FIXEDVALUE_WITH_LINKED_NODE, 
				  "Node " + node.getPath() + " has a fixed value (" + node.getFixedValue().get() + ") and a linked node"
				  + " (" + node.getLinkedNodeId().get() + ")");
			}
		}
		
		setLinkedNodes(expectedIds, nodesWithId);
	}

	
}
