package uk.nhs.fhir.render.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.fhir.data.structdef.ConstraintInfo;
import uk.nhs.fhir.data.structdef.ExtensionType;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.StringUtil;

public class FhirTreeData implements Iterable<AbstractFhirTreeTableContent> {
	private static Logger LOG = LoggerFactory.getLogger(FhirTreeData.class);
	
	private final AbstractFhirTreeTableContent root;
	
	public FhirTreeData(AbstractFhirTreeTableContent root) {
		Preconditions.checkNotNull(root);
		
		this.root = root;
	}

	public AbstractFhirTreeTableContent getRoot() {
		return root;
	}

	@Override
	public Iterator<AbstractFhirTreeTableContent> iterator() {
		return new FhirTreeIterator (this);
	}
	
	public void dumpTreeStructure() {
		for (AbstractFhirTreeTableContent node : this) {
			int indentSize = node.getPath().split("\\.").length - 1;
			String indent = StringUtil.nChars(indentSize, '\t');
			LOG.debug(indent + node.getDisplayName());
		}
	}

	public void tidyData() {
		removeExtensionsSlicingNodes(root);
		stripChildlessDummyNodes(root);
		removeUnwantedConstraints(root);
		stripComplexExtensionChildren(root);
	}
	
	// Remove inlined child nodes of complex extensions
	private void stripComplexExtensionChildren(AbstractFhirTreeTableContent node) {
		boolean isComplexExtension = node.getExtensionType().isPresent() 
		  && node.getExtensionType().get().equals(ExtensionType.COMPLEX)
		  // exclude root node
		  && node.getPath().contains(".");
		
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			AbstractFhirTreeTableContent child = children.get(i);
			if (isComplexExtension) {
				children.remove(i);
			} else {
				stripComplexExtensionChildren(child);
			}
		}
	}

	private static final Set<String> constraintKeysToRemove = new HashSet<>(Arrays.asList(new String[] {"ele-1"}));
	
	private void removeUnwantedConstraints(AbstractFhirTreeTableContent node) {
		for (AbstractFhirTreeTableContent child : node.getChildren()) {
			List<ConstraintInfo> constraints = child.getConstraints();
			for (int constraintIndex=constraints.size()-1; constraintIndex>=0; constraintIndex--) {
				ConstraintInfo constraint = constraints.get(constraintIndex);
				if (constraintKeysToRemove.contains(constraint.getKey())) {
					constraints.remove(constraintIndex);
				}
			}
			
			removeUnwantedConstraints(child);
		}
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

	private void removeExtensionsSlicingNodes(AbstractFhirTreeTableContent node) {
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		// if there is an extensions slicing node (immediately under root), remove it.
		for (int i=children.size()-1; i>=0; i--) {
			AbstractFhirTreeTableContent child = children.get(i);
			if (child.getPathName().equals("extension")
			  && child.hasSlicingInfo()) {
				children.remove(i);
			} else {
				// call recursively over whole tree
				removeExtensionsSlicingNodes(child);
			}
		}
	}
	
	public void stripRemovedElements() {
		stripRemovedElements(root);
	}
	
	/**
	 * Remove all nodes that have cardinality max = 0 (and their children)
	 */
	private void stripRemovedElements(AbstractFhirTreeTableContent node) {
		List<? extends AbstractFhirTreeTableContent> children = node.getChildren();
		
		for (int i=children.size()-1; i>=0; i--) {
			
			AbstractFhirTreeTableContent child = children.get(i);
			
			if (child.isRemovedByProfile()) {
				children.remove(i);
			} else {
				stripRemovedElements(child);
			}
		}
	}
	
	public void resolveLinkedNodes() {
		resolveNameLinkedNodes();
		resolveIdLinkedNodes();
	}

	private void resolveIdLinkedNodes() {
		Map<String, List<AbstractFhirTreeTableContent>> expectedIds = Maps.newHashMap();
		Map<String, AbstractFhirTreeTableContent> nodesWithId = Maps.newHashMap();
		
		for (AbstractFhirTreeTableContent node : this) {
			
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

	void setLinkedNodes(Map<String, List<AbstractFhirTreeTableContent>> expectedIds,
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

	public void resolveNameLinkedNodes() {
		Map<String, List<AbstractFhirTreeTableContent>> expectedNames = Maps.newHashMap();
		Map<String, AbstractFhirTreeTableContent> namedNodes = Maps.newHashMap();
		
		for (AbstractFhirTreeTableContent node : this) {
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

	public void cacheSlicingDiscriminators() {
		for (AbstractFhirTreeTableContent content : this) {
			if (content instanceof FhirTreeNode) {
				FhirTreeNode node = (FhirTreeNode)content;
				node.cacheSlicingDiscriminator();
			}
		}
	}
}

class FhirTreeIterator implements Iterator<AbstractFhirTreeTableContent> {

	// Each node down the tree to the current node
	Deque<FhirNodeAndChildIndex> chain = new ArrayDeque<>();
	
	private final FhirTreeData data;
	
	public FhirTreeIterator(FhirTreeData data) {
		Preconditions.checkNotNull(data);
		this.data = data;
	}

	@Override
	public boolean hasNext() {
		if (!returnedRoot()) {
			return true;
		}

		// true if any node in the chain to the current node has further children to offer
		for (FhirNodeAndChildIndex node : chain) {
			if (node.hasMoreChildren()) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean returnedRoot() {
		return !chain.isEmpty();
	}

	@Override
	public AbstractFhirTreeTableContent next() {
		if (!returnedRoot()) {
			return supplyRoot();
		}
		
		while (true) {
			if (chain.getLast().hasMoreChildren()) {
				return nextChildOfCurrent();
			} else if (chain.size() > 1) {
				chain.removeLast();
			} else {
				throw new NoSuchElementException();
			}
		}
		
		/*int parentOfCurrentIndex = chain.size()-2;
		for (int nodeIndex=parentOfCurrentIndex; nodeIndex>=0; nodeIndex--) {
			FhirNodeAndChildIndex lastNode = chain.get(nodeIndex);
			if (nodeHasNextChild(nodeIndex)) {
				// update chain
				int targetSize = nodeIndex + 1;
				while (chain.size() > targetSize) {
					int currentNodeIndex = maxChainIndex();
					chain.remove(currentNodeIndex);
					if (childIndexes.size() > currentNodeIndex) {
						childIndexes.remove(childIndexes.size()-1);
					}
				}
				
				// increment index entry
				int nextChildIndex = childIndexes.get(nodeIndex)+1;
				childIndexes.set(nodeIndex, nextChildIndex);

				// add new node to chain and return
				FhirTreeTableContent nextChildNode = fhirNodeAndChildIndex.getChildren().get(childIndexes.get(nodeIndex));
				chain.add(nextChildNode);
				return nextChildNode;
			}
		}
		
		throw new NoSuchElementException();*/
	}

	private AbstractFhirTreeTableContent supplyRoot() {
		AbstractFhirTreeTableContent root = data.getRoot();
		chain.add(new FhirNodeAndChildIndex(root));
		return root;
	}
	
	private AbstractFhirTreeTableContent nextChildOfCurrent() {
		AbstractFhirTreeTableContent child = chain.getLast().nextChild();
		chain.add(new FhirNodeAndChildIndex(child));
		return child;
	}
	
	private class FhirNodeAndChildIndex {
		private final AbstractFhirTreeTableContent node;
		private Integer currentChildIndex;
		
		FhirNodeAndChildIndex(AbstractFhirTreeTableContent node) {
			this.node = node;
			this.currentChildIndex = null;
		}

		public boolean hasMoreChildren() {
			if (node.getChildren().isEmpty()) {
				return false;
			} else if (currentChildIndex == null) {
				return true;
			} else {
				int maxChildIndex = node.getChildren().size()-1;
				return maxChildIndex > currentChildIndex;
			}
		}
		
		public AbstractFhirTreeTableContent nextChild() {
			if (currentChildIndex == null) {
				currentChildIndex = 0;
			} else {
				currentChildIndex++;
			}
			
			return node.getChildren().get(currentChildIndex);
		}
	}
}