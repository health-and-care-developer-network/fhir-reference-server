package uk.nhs.fhir.render.tree.cache;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;

public class NameLinkedNodeResolver extends LinkedNodeResolver {
	
	public NameLinkedNodeResolver(FhirTreeData treeData) {
		super(treeData);
	}
	
	public void resolve() {
		Map<String, List<AbstractFhirTreeTableContent>> expectedNames = Maps.newHashMap();
		Map<String, AbstractFhirTreeTableContent> namedNodes = Maps.newHashMap();
		
		for (AbstractFhirTreeTableContent node : treeData) {
			boolean hasName = node.getName().isPresent();
			if (hasName) {
				namedNodes.put(node.getName().get(), node);
			}
			
			boolean hasLinkedNode = node.getLinkedNodeName().isPresent();
			if (hasLinkedNode) {
				List<AbstractFhirTreeTableContent> nodesLinkingToThisName;
				if (expectedNames.containsKey(node.getLinkedNodeName().get())) {
					nodesLinkingToThisName = expectedNames.get(node.getLinkedNodeName().get());
				} else {
					nodesLinkingToThisName = Lists.newArrayList();
					expectedNames.put(node.getLinkedNodeName().get(), nodesLinkingToThisName);
				}
				
				nodesLinkingToThisName.add(node);
			}
			
			if (hasName && hasLinkedNode) {
				if (node.getName().get().equals(node.getLinkedNodeName().get())) {
					EventHandlerContext.forThread().event(RendererEventType.LINK_REFERENCES_ITSELF, 
						"Link " + node.getPath() + " references itself (" + node.getName().get() + ")");
				}
			}
			
			if (hasLinkedNode && node.getFixedValue().isPresent()) {
				EventHandlerContext.forThread().event(RendererEventType.FIXEDVALUE_WITH_LINKED_NODE, 
				  "Node " + node.getPath() + " has a fixed value (" + node.getFixedValue().get() + ") and a linked node"
				  + " (" + node.getLinkedNodeName().get() + ")");
			}
		}
		
		setLinkedNodes(expectedNames, namedNodes);
	}


}
