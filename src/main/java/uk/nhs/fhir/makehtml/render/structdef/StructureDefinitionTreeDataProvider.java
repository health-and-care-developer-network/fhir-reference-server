package uk.nhs.fhir.makehtml.render.structdef;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.structdef.tree.FhirTreeData;
import uk.nhs.fhir.data.structdef.tree.FhirTreeNode;
import uk.nhs.fhir.data.structdef.tree.FhirTreeTableContent;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.makehtml.FhirFileRegistry;
import uk.nhs.fhir.makehtml.RendererError;

public class StructureDefinitionTreeDataProvider {
	
	private final WrappedStructureDefinition source;
	private final FhirFileRegistry registry;
	
	private Set<String> choiceSuffixes = Sets.newHashSet("Integer", "Decimal", "DateTime", "Date", "Instant", "String", "Uri", "Boolean", "Code",
			"Markdown", "Base64Binary", "Coding", "CodeableConcept", "Attachment", "Identifier", "Quantity", "Range", "Period", "Ratio", "HumanName",
			"Address", "ContactPoint", "Timing", "Signature", "Reference");
	
	public StructureDefinitionTreeDataProvider(WrappedStructureDefinition source, FhirFileRegistry registry) {
		this.source = source;
		this.registry = registry;
	}
	
	public FhirTreeData getSnapshotTreeData() {
		FhirTreeData snapshotTree = source.getSnapshotTree(registry);
		
		snapshotTree.resolveLinkedNodes();
		snapshotTree.cacheSlicingDiscriminators();
		
		return snapshotTree;
	}
	
	public FhirTreeData getDifferentialTreeData() {
		return getDifferentialTreeData(getSnapshotTreeData());
	}
	
	public FhirTreeData getDifferentialTreeData(FhirTreeData backupTreeData) {
		FhirTreeData differentialTree = source.getDifferentialTree(registry);
		
		addBackupNodes(differentialTree, backupTreeData);
		
		differentialTree.resolveLinkedNodes();
		differentialTree.cacheSlicingDiscriminators();
		
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
		
		// Workaround for a Forge bug which means the differential node for the profiled choice is correctly renamed
		// but the snapshot name is unchanged.
		if (matchingNodes.size() == 0) {
			String differentialPath = differentialNode.getPath();
			Optional<String> choiceSuffix = choiceSuffixes.stream().filter(suffix -> differentialPath.endsWith(suffix)).findFirst();
			
			List<FhirTreeNode> matchingUnchangedChoiceNodes = Lists.newArrayList();
			if (choiceSuffix.isPresent()) {
				String suffix = choiceSuffix.get();
				String choicePath = differentialPath.substring(0, differentialPath.lastIndexOf(suffix)) + "[x]";
				matchingUnchangedChoiceNodes = findMatchingSnapshotNodes(choicePath, searchRoot);
				
				// This workaround is necessary due to an error in the Forge Tool (apparently fixed for STU3), so we should potentially highlight it.
				if (matchingUnchangedChoiceNodes.size() > 0) {
					RendererError.handle(RendererError.Key.MISNAMED_SNAPSHOT_CHOICE_NODE, "Differential node " + differentialPath + " matched snapshot node " + choicePath);
					matchingNodes = matchingUnchangedChoiceNodes;
				}
			}
		}
		
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
			String differentialPath = differentialNode.getPath();
			Optional<String> choiceSuffix = choiceSuffixes.stream().filter(suffix -> differentialPath.endsWith(suffix)).findFirst();
			if (choiceSuffix.isPresent()) {
				
			}
			throw new IllegalStateException("No nodes matched for differential element path " + differentialNode.getPath());
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
		
		while (ancestor != null) {
			
			if (ancestor.hasSlicingSibling()
			  || ancestor.hasSlicingInfo()
			  || (ancestor.hasBackupNode() && ancestor.getBackupNode().get().hasSlicingSibling())
			  || (ancestor.hasBackupNode() && ancestor.getBackupNode().get().hasSlicingInfo())) {
				if (!(ancestor instanceof FhirTreeNode)) {
					throw new IllegalStateException("First sliced ancestor is a dummy node (" + ancestor.getPath() + " for " + node.getPath());
				} else {
					return (FhirTreeNode)ancestor;
				}
			}
			
			ancestor = ancestor.getParent();
		}
		
		/*FhirTreeTableContent ancestorParent = ancestor.getParent();
		
		while (ancestorParent != null) {
			String ancestorPath = ancestor.getPath();
			for (FhirTreeTableContent child : ancestorParent.getChildren()) {
				if (child.getPath().equals(ancestorPath)
				  && child.hasSlicingInfo()) {
					return (FhirTreeNode)ancestor;
				}
			}
			
			ancestor = ancestorParent;
			ancestorParent = ancestor.getParent();
		}*/
		
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
		  && ((FhirTreeNode)element).getSliceName().isPresent()
		  && !((FhirTreeNode)element).getSliceName().get().isEmpty()) {
			FhirTreeNode fhirTreeNode = (FhirTreeNode)element;
			String name = fhirTreeNode.getSliceName().get();
			
			List<FhirTreeNode> nameMatches = Lists.newArrayList();
			
			for (FhirTreeNode node : toFilter) {
				if (node.getSliceName().isPresent()
				  && node.getSliceName().get().equals(name)) {
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
			Set<FhirURL> elementUrlDiscriminators = element.getExtensionUrlDiscriminators();
			Set<FhirURL> pathMatchUrlDiscriminators = pathMatch.getExtensionUrlDiscriminators();
			return elementUrlDiscriminators.equals(pathMatchUrlDiscriminators);
		}
		
		// most nodes
		String fullDiscriminatorPath = element.getPath() + "." + discriminatorPath;
		Optional<FhirTreeTableContent> discriminatorDescendant = element.findUniqueDescendantMatchingPath(fullDiscriminatorPath);
		Optional<FhirTreeTableContent> pathMatchDiscriminatorDescendant = pathMatch.findUniqueDescendantMatchingPath(fullDiscriminatorPath);
		return discriminatorDescendant.isPresent()
		  && pathMatchDiscriminatorDescendant.isPresent()
		  && discriminatorFixedValueMatchesLink(discriminatorDescendant.get(), pathMatchDiscriminatorDescendant.get());
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
}
