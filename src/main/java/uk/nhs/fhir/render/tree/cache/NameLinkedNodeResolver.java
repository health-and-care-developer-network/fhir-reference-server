package uk.nhs.fhir.render.tree.cache;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;

public class NameLinkedNodeResolver<T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> extends LinkedNodeResolver<T, U> {
	
	public NameLinkedNodeResolver(FhirTreeData<T, U> treeData) {
		super(treeData);
	}
	
	public void resolve() {
		Map<String, List<U>> expectedNames = Maps.newHashMap();
		Map<String, SnapshotTreeNode> namedNodes = Maps.newHashMap();
		
		for (U node : treeData.nodes()) {
			T nodeData = node.getData();
			
			boolean hasName = nodeData.getName().isPresent();
			if (hasName) {
				if (node instanceof SnapshotTreeNode) {
					namedNodes.put(nodeData.getName().get(), (SnapshotTreeNode)node);
				} else if (node instanceof DifferentialTreeNode) {
					// resolve backup node
					namedNodes.put(nodeData.getName().get(), ((DifferentialTreeNode) node).getBackupNode());
				} else {
					throw new IllegalStateException("Unexpected node type: " + node.getClass().getSimpleName());
				}
			}
			
			boolean hasLinkedNode = nodeData.getLinkedNodeName().isPresent();
			if (hasLinkedNode) {
				List<U> nodesLinkingToThisName;
				if (expectedNames.containsKey(nodeData.getLinkedNodeName().get())) {
					nodesLinkingToThisName = expectedNames.get(nodeData.getLinkedNodeName().get());
				} else {
					nodesLinkingToThisName = Lists.newArrayList();
					expectedNames.put(nodeData.getLinkedNodeName().get(), nodesLinkingToThisName);
				}
				
				nodesLinkingToThisName.add(node);
			}
			
			if (hasName && hasLinkedNode) {
				if (nodeData.getName().get().equals(nodeData.getLinkedNodeName().get())) {
					EventHandlerContext.forThread().event(RendererEventType.LINK_REFERENCES_ITSELF, 
						"Link " + node.getPath() + " references itself (" + nodeData.getName().get() + ")");
				}
			}
			
			if (hasLinkedNode && nodeData.getFixedValue().isPresent()) {
				EventHandlerContext.forThread().event(RendererEventType.FIXEDVALUE_WITH_LINKED_NODE, 
				  "Node " + node.getPath() + " has a fixed value (" + nodeData.getFixedValue().get() + ") and a linked node"
				  + " (" + nodeData.getLinkedNodeName().get() + ")");
			}
		}
		
		setLinkedNodes(expectedNames, namedNodes);
	}


}
