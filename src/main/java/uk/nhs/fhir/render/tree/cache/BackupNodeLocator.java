package uk.nhs.fhir.render.tree.cache;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.render.tree.DifferentialData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.SnapshotData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;
import uk.nhs.fhir.util.ListUtils;

public class BackupNodeLocator {

	private static final Set<String> choiceSuffixes = Sets.newHashSet("Integer", "Decimal", "DateTime", "Date", "Instant", "String", "Uri", "Boolean", "Code",
		"Markdown", "Base64Binary", "Coding", "CodeableConcept", "Attachment", "Identifier", "Quantity", "Range", "Period", "Ratio", "HumanName",
		"Address", "ContactPoint", "Timing", "Signature", "Reference");
	
	
	private final FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData;

	public BackupNodeLocator(FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {
		this.backupTreeData = backupTreeData;
	}
	
	/*
	 * Identifies the node in the snapshot which corresponds to the supplied node from a differential.
	 * Every node in the differential should be present in the snapshot.
	 * Sliced nodes need disambiguating on their discriminators to find the correct match.
	 */
	public SnapshotTreeNode findBackupNode(DifferentialTreeNode differentialNode) {
		
		SnapshotTreeNode searchRoot = getRootOrFirstSlicedParent(differentialNode);
		
		List<SnapshotTreeNode> matchingNodes = findMatchingSnapshotNodes(differentialNode.getPath(), searchRoot);
		
		// Workaround for a Forge bug which means the differential node for the profiled choice is correctly renamed
		// but the snapshot name is unchanged. The name mismatch may occur multiple times in the node path.
		if (matchingNodes.size() == 0) {
			matchingNodes = handleForgeRenamingDifferentialChoiceButNotSnapshot(differentialNode.getPath(), searchRoot);
		}
		
		if (matchingNodes.size() == 1) {
			return matchingNodes.get(0);
		} else if (matchingNodes.size() > 0 && matchingNodes.get(0).getData().hasSlicingInfo()) {
			if (differentialNode.getData().hasSlicingInfo()) {
				return matchingNodes.get(0);
			} else {
				SlicingInfo slicingInfo = matchingNodes.get(0).getData().getSlicingInfo().get();
				
				removeSlicingInfoMatches(differentialNode, matchingNodes);
				matchingNodes = filterOnNameIfPresent(differentialNode, matchingNodes);
				
				if (matchingNodes.size() == 1) {
					return matchingNodes.get(0);
				}
				
				return matchOnNameOrDiscriminatorPaths(differentialNode, matchingNodes, slicingInfo);
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
				  + differentialNode.getData().getName().get() + ".");
			}
		} else if (matchingNodes.size() == 0) {
			String differentialPath = differentialNode.getPath();
			Optional<String> choiceSuffix = choiceSuffixes.stream().filter(suffix -> differentialPath.endsWith(suffix)).findFirst();
			// handle this if it comes up
			throw new IllegalStateException("No nodes matched for differential element path " + differentialNode.getPath() + " choice suffix = " + choiceSuffix.toString());
		} else {
			throw new IllegalStateException("Multiple snapshot nodes matched differential element path " + differentialNode.getPath() + ", but first wasn't a slicing node");
		}
	}

	private List<SnapshotTreeNode> handleForgeRenamingDifferentialChoiceButNotSnapshot(String differentialPath, SnapshotTreeNode searchRoot) {
		
		SnapshotTreeNode currentSearchRoot = searchRoot;
		String confirmedSnapshotPath = "";
		
		for (String differentialPathElement : differentialPath.split("\\.")) {
			String possibleSnapshotElementPath = confirmedSnapshotPath;
			if (!confirmedSnapshotPath.isEmpty()) {
				possibleSnapshotElementPath += ".";
			}
			possibleSnapshotElementPath += differentialPathElement;
			
			currentSearchRoot = findMatchingSnapshotNodeReinstateChoiceSuffixIfNecessary(differentialPath, currentSearchRoot, possibleSnapshotElementPath);
			
			confirmedSnapshotPath = currentSearchRoot.getPath();
		}
		
		EventHandlerContext.forThread().event(RendererEventType.MISNAMED_SNAPSHOT_CHOICE_NODE, 
			"Differential node " + differentialPath + " matched snapshot node " + confirmedSnapshotPath);
		
		return Lists.newArrayList(searchRoot);
	}

	private SnapshotTreeNode findMatchingSnapshotNodeReinstateChoiceSuffixIfNecessary(String differentialPath, SnapshotTreeNode searchRoot,
			String possibleSnapshotElementPath) {
		List<SnapshotTreeNode> possibleAncestorNodes = findMatchingSnapshotNodes(possibleSnapshotElementPath, searchRoot);
		if (possibleAncestorNodes.size() == 1) {
			searchRoot = possibleAncestorNodes.get(0);
		} else if (possibleAncestorNodes.isEmpty()) {
			
			String matchedSuffix = matchSuffixOrThrow(possibleSnapshotElementPath, differentialPath);
			String restoredGenericChoicePath = reinstateChoiceSuffix(possibleSnapshotElementPath, matchedSuffix);
			List<SnapshotTreeNode> possibleChoiceAncestorNodes = findMatchingSnapshotNodes(restoredGenericChoicePath, searchRoot);
			
			if (possibleChoiceAncestorNodes.isEmpty()) { 
				throw new IllegalStateException("Didn't find any matching paths, even after choice substitution to \"[x]\": " + differentialPath);
			}
			
			if (possibleChoiceAncestorNodes.size() > 1) {
				throw new IllegalStateException("Found multiple possible matching resolved choice nodes in snapshot - implement match on discriminator? " + differentialPath);
			}
			
			// Found a single matching node
			searchRoot = possibleChoiceAncestorNodes.get(0);
		} else {
			throw new IllegalStateException("Multiple matches on name - need finer grained handling (discriminators?) " + differentialPath);
		}
		return searchRoot;
	}

	private String reinstateChoiceSuffix(String possibleSnapshotElementPath, String resolvedSuffix) {
		int lengthWithoutSuffix = possibleSnapshotElementPath.length() - resolvedSuffix.length();
		String truncatedPath = possibleSnapshotElementPath.substring(0, lengthWithoutSuffix);
		return truncatedPath + "[x]";
	}

	private String matchSuffixOrThrow(String possibleSnapshotElementPath, String differentialPath) {
		return
			choiceSuffixes
				.stream()
				.filter(eachSuffix -> possibleSnapshotElementPath.endsWith(eachSuffix))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No matching paths found, and not a resolved choice node: " + differentialPath));
	}

	private SnapshotTreeNode getRootOrFirstSlicedParent(DifferentialTreeNode node) {
		if (hasSlicedParent(node)) {
			return getFirstSlicedParent(node).getBackupNode();
		} else {
			return backupTreeData.getRoot();
		}
	}

	void removeSlicingInfoMatches(DifferentialTreeNode differentialNode, List<SnapshotTreeNode> matchingNodes) {
		for (int i=matchingNodes.size()-1; i>=0; i--) {
			SnapshotTreeNode matchingNode = matchingNodes.get(i);
			if (differentialNode.getData().hasSlicingInfo() != matchingNode.getData().hasSlicingInfo()) {
				matchingNodes.remove(i);
			}
		}
	}
	
	private <T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T,U>> boolean hasSlicedParent(U node) {
		return getFirstSlicedParent(node) != null;
	}

	private List<SnapshotTreeNode> findMatchingSnapshotNodes(String differentialPath, SnapshotTreeNode searchRoot) {
		List<SnapshotTreeNode> matchingNodes = Lists.newArrayList();
		
		for (SnapshotTreeNode node : new FhirTreeData<>(searchRoot).nodes()) {
			if (node.getPath().equals(differentialPath)) {
				matchingNodes.add(node);
			}
		}
		
		return matchingNodes;
	}

	private List<SnapshotTreeNode> filterOnNameIfPresent(DifferentialTreeNode differentialNode, List<SnapshotTreeNode> toFilter) {
		if (differentialNode.getData().getSliceName().isPresent()
		  && !differentialNode.getData().getSliceName().get().isEmpty()) {
			String name = differentialNode.getData().getSliceName().get();
			
			List<SnapshotTreeNode> nameMatches = Lists.newArrayList();
			
			for (SnapshotTreeNode node : toFilter) {
				if (node.getData().getSliceName().isPresent()
				  && node.getData().getSliceName().get().equals(name)) {
					nameMatches.add(node);
				}
			}
			
			return Lists.newArrayList(ListUtils.expectUnique(nameMatches, "snapshot nodes matching path " + differentialNode.getPath() + " and name " + name));
		} else {
			// no name to filter on
			return toFilter;
		}
	}
	
	/**
	 * Finds a matching node for a sliced element based on name or (failing that) on slicing discriminators.
	 */
	private SnapshotTreeNode matchOnNameOrDiscriminatorPaths(DifferentialTreeNode differentialNode, List<SnapshotTreeNode> pathMatches, SlicingInfo slicingInfo) {
		Set<String> discriminatorPaths = slicingInfo.getDiscriminatorPaths();
		
		// nodes which match on discriminator (as well as path)
		List<SnapshotTreeNode> discriminatorMatches = Lists.newArrayList();
		
		for (SnapshotTreeNode pathMatch : pathMatches) {
			boolean matchesOnDiscriminators = true;
			for (String discriminatorPath : discriminatorPaths) {
				if (!matchesOnDiscriminator(discriminatorPath, differentialNode, pathMatch)) {
					matchesOnDiscriminators = false;
					break;
				}
			}
			
			if (matchesOnDiscriminators) {
				discriminatorMatches.add(pathMatch);
			}
		}
		
		return ListUtils.expectUnique(discriminatorMatches, "matches (on all discriminators) for backupNode for slice " + differentialNode.getPath());
}
	
	/*
	 * Searches up the tree for the first child affected by slicing, 
	 * i.e. an ancestor node with a sibling which 
	 * 		- has a matching path 
	 * 		- has slicing information.
	 * Will not return the node itself.
	 */
	private <T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T,U>> U getFirstSlicedParent(U node) {
		U ancestor = node.getParent();
		if (ancestor == null) {
			return null;
		}
		
		while (ancestor != null) {
			
			if (ancestor.hasSlicingSibling()
			  || ancestor.getData().hasSlicingInfo()) {
				return ancestor;
			}
			
			ancestor = ancestor.getParent();
		}
		
		return null;
	}

	private boolean matchesOnDiscriminator(String discriminatorPath, DifferentialTreeNode element, SnapshotTreeNode pathMatch) {
		if (element.getData().getPathName().equals("extension")
		  && discriminatorPath.equals("url")) {
			Set<FhirURL> elementUrlDiscriminators = element.getData().getExtensionUrlDiscriminators();
			Set<FhirURL> pathMatchUrlDiscriminators = pathMatch.getData().getExtensionUrlDiscriminators();
			return elementUrlDiscriminators.equals(pathMatchUrlDiscriminators);
		}
		
		// most nodes
		String fullDiscriminatorPath = element.getPath() + "." + discriminatorPath;
		Optional<DifferentialTreeNode> differentialDescendant = element.findUniqueDescendantMatchingPath(fullDiscriminatorPath);
		Optional<SnapshotTreeNode> snapshotDescendant = pathMatch.findUniqueDescendantMatchingPath(fullDiscriminatorPath);
		return differentialDescendant.isPresent()
		  && snapshotDescendant.isPresent()
		  && discriminatorFixedValueMatchesLink(differentialDescendant.get().getData(), snapshotDescendant.get().getData());
	}

	private boolean discriminatorFixedValueMatchesLink(DifferentialData differentialDescendant, SnapshotData snapshotDescendant) {
		if (differentialDescendant.isFixedValue()) {
			return matchesOnFixedValue(differentialDescendant, snapshotDescendant);
		} 
		
		if (!differentialDescendant.isFixedValue() 
		  && !snapshotDescendant.isFixedValue()) {
			return true;
		}
		
		return false;
	}

	private boolean matchesOnFixedValue(DifferentialData differentialDescendantData, SnapshotData snapshotDescendantData) {
		if (!snapshotDescendantData.isFixedValue()) {
			return false;
		}
		
		String fixedValue = differentialDescendantData.getFixedValue().get();
		String matchFixedValue = snapshotDescendantData.getFixedValue().get();
		return matchFixedValue.equals(fixedValue);
	}

}
