package uk.nhs.fhir.render.tree.cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;

public class IdLinkedNodeResolver<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> extends LinkedNodeResolver<T, U> {
	
	public IdLinkedNodeResolver(FhirTreeData<T, U> treeData) {
		super(treeData);
	}
	
	public void resolve() {
		Map<String, List<U>> expectedIds = Maps.newHashMap();
		Map<String, SnapshotTreeNode> nodesWithId = Maps.newHashMap();
		
		for (U node : treeData.nodes()) {
			T nodeData = node.getData();
			
			Optional<String> id = nodeData.getId();
			boolean hasId = id.isPresent();
			if (hasId) {
				if (node instanceof SnapshotTreeNode) {
					nodesWithId.put(id.get(), (SnapshotTreeNode)node);
				} else if (node instanceof DifferentialTreeNode) {
					// resolve backup node
					nodesWithId.put(id.get(), ((DifferentialTreeNode) node).getBackupNode());
				} else {
					throw new IllegalStateException("Unexpected node type: " + node.getClass().getSimpleName());
				}
			}
			
			Optional<String> linkedNodeId = nodeData.getLinkedNodeId();
			boolean hasIdLinkedNode = linkedNodeId.isPresent();
			if (hasIdLinkedNode) {
				List<U> nodesLinkingToThisId;
				if (expectedIds.containsKey(linkedNodeId.get())) {
					nodesLinkingToThisId = expectedIds.get(linkedNodeId.get());
				} else {
					nodesLinkingToThisId = Lists.newArrayList();
					expectedIds.put(linkedNodeId.get(), nodesLinkingToThisId);
				}
				
				nodesLinkingToThisId.add(node);
			}
			
			if (hasId && hasIdLinkedNode) {
				if (nodeData.getId().get().equals(nodeData.getLinkedNodeId().get())) {
					EventHandlerContext.forThread().event(RendererEventType.LINK_REFERENCES_ITSELF, "Link " + node.getPath() + " references itself (" + nodeData.getId().get() + ")");
				}
			}
			
			if (hasIdLinkedNode && nodeData.getFixedValue().isPresent()) {
				EventHandlerContext.forThread().event(RendererEventType.FIXEDVALUE_WITH_LINKED_NODE, 
				  "Node " + node.getPath() + " has a fixed value (" + nodeData.getFixedValue().get() + ") and a linked node"
				  + " (" + nodeData.getLinkedNodeId().get() + ")");
			}
		}
		
		setLinkedNodes(expectedIds, nodesWithId);
	}

	
}
