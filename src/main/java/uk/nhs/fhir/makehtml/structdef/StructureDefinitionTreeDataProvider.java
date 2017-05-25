package uk.nhs.fhir.makehtml.structdef;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.makehtml.SkipRenderGenerationException;
import uk.nhs.fhir.makehtml.data.FhirTreeData;
import uk.nhs.fhir.makehtml.data.FhirTreeDataBuilder;
import uk.nhs.fhir.makehtml.data.FhirTreeNode;
import uk.nhs.fhir.makehtml.data.FhirTreeNodeBuilder;
import uk.nhs.fhir.makehtml.data.FhirTreeTableContent;
import uk.nhs.fhir.makehtml.data.LinkData;
import uk.nhs.fhir.makehtml.data.NestedLinkData;
import uk.nhs.fhir.makehtml.data.SimpleLinkData;
import uk.nhs.fhir.makehtml.data.SlicingInfo;

public class StructureDefinitionTreeDataProvider {
	
	private final StructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(StructureDefinition source) {
		this.source = source;
	}
	
	public FhirTreeData getSnapshotTreeData() {

		FhirTreeDataBuilder fhirTreeBuilder = new FhirTreeDataBuilder();
		
		Snapshot snapshot = source.getSnapshot();
		List<ElementDefinitionDt> snapshotElements = snapshot.getElement();
		for (ElementDefinitionDt elementDefinition : snapshotElements) {
			fhirTreeBuilder.addElementDefinition(elementDefinition, false);
		}
		
		FhirTreeData tree = fhirTreeBuilder.getTree();
		
		return tree;
	}
	
	public FhirTreeData getDifferentialTreeData() {
		return getDifferentialTreeData(getSnapshotTreeData());
	}
	
	public FhirTreeData getDifferentialTreeData(FhirTreeData backupTreeData) {
		FhirTreeDataBuilder fhirTreeBuilder = new FhirTreeDataBuilder(new FhirTreeNodeBuilder());
		
		List<ElementDefinitionDt> differentialElements = source.getDifferential().getElement();
		for (ElementDefinitionDt differentialElement : differentialElements) {
			fhirTreeBuilder.addElementDefinition(differentialElement, true);
		}
		
		FhirTreeData differentialTree = fhirTreeBuilder.getTree();
		//differentialTree.dumpTreeStructure();
		
		addBackupNodes(differentialTree, backupTreeData);
		
		return differentialTree;
	}

	private void addBackupNodes(FhirTreeData differentialTree, FhirTreeData snapshotTreeData) {
		for (FhirTreeTableContent differentialNode : differentialTree) {
			FhirTreeNode backupNode = findBackupNode(differentialNode, snapshotTreeData);
			differentialNode.setBackupNode(backupNode);
		}
	}

	/*
	 * Identifies the node in the snapshot which corresponds to the supplied node from a differential.
	 * Every node in the differential should be present in the snapshot.
	 * Sliced nodes need disambiguating on their discriminators to find the correct match.
	 */
	private FhirTreeNode findBackupNode(FhirTreeTableContent differentialNode, FhirTreeData snapshotTreeData) {
		
		FhirTreeNode searchRoot = (FhirTreeNode) snapshotTreeData.getRoot();
		if (hasSlicedParent(differentialNode)) {
			searchRoot = getFirstSlicedParent(differentialNode).getBackupNode().get();
		}
		
		List<FhirTreeNode> matchingNodes = findMatchingSnapshotNodes(differentialNode.getPath(), searchRoot);
		
		if (matchingNodes.size() == 1) {
			return matchingNodes.get(0);
		} else if (matchingNodes.size() > 0 && matchingNodes.get(0).hasSlicingInfo()) {
			if (differentialNode.hasSlicingInfo()) {
				return matchingNodes.get(0);
			} else {
				SlicingInfo slicingInfo = matchingNodes.get(0).getSlicingInfo().get();
				
				removeSlicingInfoMatches(differentialNode, matchingNodes);
				matchingNodes = filterOnNameIfPresent(differentialNode, matchingNodes);
				
				if (matchingNodes.size() == 1) {
					return matchingNodes.get(0);
				}
				
				if (differentialNode instanceof FhirTreeNode) {
					return matchOnNameOrDiscriminatorPaths((FhirTreeNode)differentialNode, matchingNodes, slicingInfo);
				} else {
					throw new IllegalStateException("Multiple matches for dummy node " + differentialNode.getPath());
				}
			}
		} else if (differentialNode.getPath().equals("Extension.extension")) {
			// didn't have slicing, try matching on name anyway
			matchingNodes = filterOnNameIfPresent(differentialNode, matchingNodes);
			if (matchingNodes.size() == 1) {
				return matchingNodes.get(0);
			} else if (matchingNodes.size() > 1) {
				throw new IllegalStateException("Couldn't differentiate Extension.extension nodes on name fields. "
				  + matchingNodes.size() + " matches.");
			} else {
				throw new IllegalStateException("No Extension.extension nodes matched for name "
				  + ((FhirTreeNode)differentialNode).getName().get() + ".");
			}
		} else if (matchingNodes.size() == 0) {
			throw new SkipRenderGenerationException("No nodes matched for differential element path " + differentialNode.getPath());
		} else {
			throw new IllegalStateException("Multiple snapshot nodes matched differential element path " + differentialNode.getPath() + ", but first wasn't a slicing node");
		}
	}

	void removeSlicingInfoMatches(FhirTreeTableContent differentialNode, List<FhirTreeNode> matchingNodes) {
		for (int i=matchingNodes.size()-1; i>=0; i--) {
			FhirTreeNode matchingNode = matchingNodes.get(i);
			if (differentialNode.hasSlicingInfo() != matchingNode.hasSlicingInfo()) {
				matchingNodes.remove(i);
			}
		}
	}

	/*
	 * Searches up the tree for the first child affected by slicing, 
	 * i.e. an ancestor node with a sibling which 
	 * 		- has a matching path 
	 * 		- has slicing information.
	 * Will not return the node itself.
	 */
	private FhirTreeNode getFirstSlicedParent(FhirTreeTableContent node) {
		FhirTreeTableContent ancestor = node.getParent();
		if (ancestor == null) {
			return null;
		}
		
		FhirTreeTableContent ancestorParent = ancestor.getParent();
		
		while (ancestorParent != null) {
			if (ancestor instanceof FhirTreeNode) {
				String nodePath = ancestor.getPath();
				for (FhirTreeTableContent child : ancestorParent.getChildren()) {
					if (child.getPath().equals(nodePath)
					  && child.hasSlicingInfo()) {
						return (FhirTreeNode)ancestor;
					}
				}
			}
			
			ancestor = ancestorParent;
			ancestorParent = ancestor.getParent();
		}
		
		return null;
	}

	private boolean hasSlicedParent(FhirTreeTableContent node) {
		return getFirstSlicedParent(node) != null;
	}

	private List<FhirTreeNode> findMatchingSnapshotNodes(String differentialPath, FhirTreeNode searchRoot) {
		List<FhirTreeNode> matchingNodes = Lists.newArrayList();
		
		for (FhirTreeTableContent node : new FhirTreeData(searchRoot)) {
			//System.out.println(node.getPath());
			if (node.getPath().equals(differentialPath)) {
				if (node instanceof FhirTreeNode) {
					FhirTreeNode matchedFhirTreeNode = (FhirTreeNode)node;
					matchingNodes.add(matchedFhirTreeNode);
				} else {
					throw new IllegalStateException("Snapshot tree contains a Dummy node");
				}
			}
		}
		
		return matchingNodes;
	}

	private List<FhirTreeNode> filterOnNameIfPresent(FhirTreeTableContent element, List<FhirTreeNode> toFilter) {
		if (element instanceof FhirTreeNode 
		  && ((FhirTreeNode)element).getName().isPresent()
		  && !((FhirTreeNode)element).getName().get().isEmpty()) {
			FhirTreeNode fhirTreeNode = (FhirTreeNode)element;
			String name = fhirTreeNode.getName().get();
			
			List<FhirTreeNode> nameMatches = Lists.newArrayList();
			
			for (FhirTreeNode node : toFilter) {
				if (node.getName().isPresent()
				  && node.getName().get().equals(name)) {
					nameMatches.add(node);
				}
			}
			
			if (nameMatches.size() == 1) {
				return nameMatches;
			} else if (nameMatches.size() == 0) {
				throw new IllegalStateException("No snapshot nodes matched path " + fhirTreeNode.getPath() + " and name " + name);
			} else {
				throw new IllegalStateException("Multiple (" + nameMatches.size() + ") snapshot nodes matched"
					+ " path " + fhirTreeNode.getPath() + " and name " + name);
			}
		} else {
			// no name to filter on
			return toFilter;
		}
	}
	
	/**
	 * Finds a matching node for a sliced element based on name or (failing that) on slicing discriminators.
	 */
	private FhirTreeNode matchOnNameOrDiscriminatorPaths(FhirTreeNode element, List<FhirTreeNode> pathMatches, SlicingInfo slicingInfo) {
		Set<String> discriminatorPaths = slicingInfo.getDiscriminatorPaths();
		
		// nodes which match on discriminator (as well as path)
		List<FhirTreeNode> discriminatorMatches = Lists.newArrayList();
		
		for (FhirTreeNode pathMatch : pathMatches) {
			boolean matchesOnDiscriminators = true;
			for (String discriminatorPath : discriminatorPaths) {
				if (!matchesOnDiscriminator(discriminatorPath, element, pathMatch)) {
					matchesOnDiscriminators = false;
					break;
				}
			}
			
			if (matchesOnDiscriminators) {
				discriminatorMatches.add(pathMatch);
			}
		}
		
		if (discriminatorMatches.size() == 1) {
			return discriminatorMatches.get(0);
		} else if (discriminatorMatches.size() == 0) {
			throw new IllegalStateException("No matches found for backupNode for slice " + element.getPath());
		} else {
			throw new IllegalStateException("Multiple matches (on all discriminators) found for backupNode for slice " + element.getPath());
		}
	}

	private boolean matchesOnDiscriminator(String discriminatorPath, FhirTreeNode element, FhirTreeNode pathMatch) {
		if (element.getPathName().equals("extension")
		  && discriminatorPath.equals("url")) {
			for (LinkData typeLinkToMatch : element.getTypeLinks()) {
				if (typeLinkToMatch instanceof NestedLinkData
				  && typeLinkToMatch.getPrimaryLinkData().getText().equals("Extension")
				  && ((NestedLinkData) typeLinkToMatch).getNestedLinks().size() == 1) {
					SimpleLinkData nestedLinkToMatch = ((NestedLinkData)typeLinkToMatch).getNestedLinks().get(0);
					String urlToMatch = nestedLinkToMatch.getURL();
					
					for (LinkData typeLink : pathMatch.getTypeLinks()) {
						if (typeLink instanceof NestedLinkData
						  && typeLink.getPrimaryLinkData().getText().equals("Extension")) {
							for (LinkData nestedLink : ((NestedLinkData)typeLink).getNestedLinks()) {
								if (nestedLink.getURL().equals(urlToMatch)) {
									return true;
								}
							}
						}
					}
				}
			}
			
		}
		
		// most nodes
		String fullDiscriminatorPath = element.getPath() + "." + discriminatorPath;
		Optional<FhirTreeTableContent> discriminatorDescendant = findUniqueDescendantMatchingPath(element, fullDiscriminatorPath);
		Optional<FhirTreeTableContent> pathMatchDiscriminatorDescendant = findUniqueDescendantMatchingPath(pathMatch, fullDiscriminatorPath);
		if (discriminatorDescendant.isPresent()
		  && pathMatchDiscriminatorDescendant.isPresent()
		  && discriminatorFixedValueMatchesLink(discriminatorDescendant.get(), pathMatchDiscriminatorDescendant.get())) {
			return true;
		}
		
		return false;
	}

	private boolean discriminatorFixedValueMatchesLink(FhirTreeTableContent discriminatorDescendant, FhirTreeTableContent pathMatchDiscriminatorDescendant) {
		if (discriminatorDescendant.isFixedValue()) {
			return matchesOnFixedValue(discriminatorDescendant, pathMatchDiscriminatorDescendant);
		} 
		
		if (!discriminatorDescendant.isFixedValue() 
		  && !pathMatchDiscriminatorDescendant.isFixedValue()) {
			return true;
		}
		
		return false;
	}

	private boolean matchesOnFixedValue(FhirTreeTableContent discriminatorDescendant, FhirTreeTableContent pathMatchDiscriminatorDescendant) {
		if (!pathMatchDiscriminatorDescendant.isFixedValue()) {
			return false;
		}
		
		String fixedValue = discriminatorDescendant.getFixedValue().get();
		String matchFixedValue = pathMatchDiscriminatorDescendant.getFixedValue().get();
		return matchFixedValue.equals(fixedValue);
	}

	Optional<FhirTreeTableContent> findUniqueDescendantMatchingPath(FhirTreeNode searchRoot, String fullDiscriminatorPath) {
		List<FhirTreeTableContent> childNodesMatchingDiscriminatorPath = Lists.newArrayList();
		for (FhirTreeTableContent descendantNode : new FhirTreeData(searchRoot)) {
			if (descendantNode.getPath().equals(fullDiscriminatorPath)) {
				if (descendantNode instanceof FhirTreeNode) {
					FhirTreeNode matchedFhirTreeNode = (FhirTreeNode)descendantNode;
					childNodesMatchingDiscriminatorPath.add(matchedFhirTreeNode);
				} else {
					throw new IllegalStateException("Snapshot tree contains a Dummy node");
				}
			}
		}
		
		FhirTreeTableContent discriminatorDescendant;
		if (childNodesMatchingDiscriminatorPath.size() == 1) {
			discriminatorDescendant = childNodesMatchingDiscriminatorPath.get(0);
		} else if (childNodesMatchingDiscriminatorPath.size() == 0) {
			discriminatorDescendant = null;
		} else {
			throw new IllegalStateException("Multiple descendants matching discriminator " + fullDiscriminatorPath 
					+ " found for element at " + searchRoot.getPath());
		}
		
		return Optional.ofNullable(discriminatorDescendant);
	}
}
