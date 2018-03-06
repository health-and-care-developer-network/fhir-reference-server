package uk.nhs.fhir.data.structdef.tree;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.nhs.fhir.data.structdef.SlicingInfo;
import uk.nhs.fhir.data.url.FhirURL;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.event.EventHandlerContext;
import uk.nhs.fhir.event.RendererEventType;
import uk.nhs.fhir.util.ListUtils;

/*
 * A node belonging to a tree created as a first pass of a differential tree. This should capture just enough data that it is possible
 * to determine the value of any discriminators of sliced nodes and therefore identify the unique snapshot element that this differential
 * element should use as a backup.
 */
public class FhirDifferentialSkeletonNode extends CloneableTreeNode<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode>{
	
	private static final Set<String> choiceSuffixes = Sets.newHashSet("Integer", "Decimal", "DateTime", "Date", "Instant", "String", "Uri", "Boolean", "Code",
		"Markdown", "Base64Binary", "Coding", "CodeableConcept", "Attachment", "Identifier", "Quantity", "Range", "Period", "Ratio", "HumanName",
		"Address", "ContactPoint", "Timing", "Signature", "Reference");
	
	private Optional<SnapshotTreeNode> backupNode = Optional.empty();
	
	public FhirDifferentialSkeletonNode(FhirDifferentialSkeletonData data) {
		super(data);
	}

	@Override
	public String getPath() {
		return getData().getPath();
	}

	// Lazily evaluate/cache the backup node
	public SnapshotTreeNode getBackupNode(FhirTreeData<SnapshotData, SnapshotTreeNode> snapshot) {
		if (!backupNode.isPresent()) {
			backupNode = Optional.of(resolveBackupNode(snapshot));
		}
		
		return backupNode.get();
	}
	
	private SnapshotTreeNode resolveBackupNode(FhirTreeData<SnapshotData, SnapshotTreeNode> snapshot) {
		SnapshotTreeNode searchRoot = getRootOrFirstSlicedParent(snapshot);
		List<SnapshotTreeNode> backupNodesWithMatchingPaths = searchRoot.descendantsWithPath(getPath());
		
		if (backupNodesWithMatchingPaths.isEmpty()) {
			backupNodesWithMatchingPaths = handleForgeRenamingDifferentialChoiceButNotSnapshot(getPath(), searchRoot);
		}
		
		if (backupNodesWithMatchingPaths.size() == 1) {
			return backupNodesWithMatchingPaths.get(0);
		} else if (backupNodesWithMatchingPaths.size() > 0 
		  && backupNodesWithMatchingPaths.get(0).getData().hasSlicingInfo()) {
			SnapshotTreeNode backup = resolveSlicedBackupNode(backupNodesWithMatchingPaths);
			return backup;
		} else if (getData().getPathName().equals("extension")) {
			SnapshotTreeNode backup = resolveExtensionBackupNode(backupNodesWithMatchingPaths);
			return backup;
		} else if (backupNodesWithMatchingPaths.size() == 0) {
			Optional<String> choiceSuffix = choiceSuffixes.stream().filter(suffix -> getPath().endsWith(suffix)).findFirst();
			// handle this if it comes up
			throw new IllegalStateException("No nodes matched for differential element path " + getPath() + " choice suffix = " + choiceSuffix.toString());
		} else {
			throw new IllegalStateException("Multiple snapshot nodes matched differential element path " + getPath() + ", but first wasn't a slicing node");
		}
	}

	private SnapshotTreeNode resolveExtensionBackupNode(List<SnapshotTreeNode> pathMatches) {
		// didn't have slicing, try matching on slice name anyway
		List<SnapshotTreeNode> nameAndPathMatches = filterOnNameIfPresent(pathMatches);
		
		if (nameAndPathMatches.size() == 1) {
			return nameAndPathMatches.get(0);
		} else if (nameAndPathMatches.size() > 1) {
			throw new IllegalStateException("Couldn't differentiate " + getPath() + " nodes on name fields (" + getData().getSliceName() + "). "
			  + nameAndPathMatches.size() + " matches.");
		} else {
			throw new IllegalStateException("No " + getPath() + " nodes matched for name "
			  + getData().getSliceName().get() + ".");
		}
	}

	private SnapshotTreeNode resolveSlicedBackupNode(List<SnapshotTreeNode> backupNodesWithMatchingPaths) {
		if (getData().hasSlicingInfo()) {
			return backupNodesWithMatchingPaths.get(0);
		} else {
			SlicingInfo slicingInfo = backupNodesWithMatchingPaths.get(0).getData().getSlicingInfo().get();
			
			removeSlicingInfoMismatches(backupNodesWithMatchingPaths);
			backupNodesWithMatchingPaths = filterOnNameIfPresent(backupNodesWithMatchingPaths);
			
			if (backupNodesWithMatchingPaths.size() == 1) {
				return backupNodesWithMatchingPaths.get(0);
			}
			
			return matchOnNameOrDiscriminatorPaths(backupNodesWithMatchingPaths, slicingInfo);
		}
	}

	private SnapshotTreeNode getRootOrFirstSlicedParent(FhirTreeData<SnapshotData, SnapshotTreeNode> snapshot) {
		Optional<FhirDifferentialSkeletonNode> searchRoot = SlicingResolver.getFirstSlicedParent(this);
		
		if (searchRoot.isPresent()) {
			return searchRoot.get().getBackupNode(snapshot);
		} else {
			return snapshot.getRoot();
		}
	}
	
	private List<SnapshotTreeNode> handleForgeRenamingDifferentialChoiceButNotSnapshot(String differentialPath, SnapshotTreeNode searchRoot) {
		
		SnapshotTreeNode currentSearchRoot = searchRoot;
		String confirmedSnapshotPath = "";
		
		for (String differentialPathElement : differentialPath.split("\\.")) {
			
			String possibleSnapshotElementPath = 
				confirmedSnapshotPath.isEmpty()	?
					differentialPathElement	:
					confirmedSnapshotPath + "." + differentialPathElement;
			
			currentSearchRoot = findMatchingSnapshotNodeReinstateChoiceSuffixIfNecessary(differentialPath, currentSearchRoot, possibleSnapshotElementPath);
			
			confirmedSnapshotPath = currentSearchRoot.getPath();
		}
		
		EventHandlerContext.forThread().event(RendererEventType.MISNAMED_SNAPSHOT_CHOICE_NODE, 
			"Differential node " + differentialPath + " matched snapshot node " + confirmedSnapshotPath);
		
		return Lists.newArrayList(currentSearchRoot);
	}

	private SnapshotTreeNode findMatchingSnapshotNodeReinstateChoiceSuffixIfNecessary(String differentialPath, SnapshotTreeNode searchRoot,
			String possibleSnapshotElementPath) {
		List<SnapshotTreeNode> possibleAncestorNodes = searchRoot.childrenWithPath(possibleSnapshotElementPath);
		if (possibleAncestorNodes.size() == 1) {
			searchRoot = possibleAncestorNodes.get(0);
		} else if (possibleAncestorNodes.isEmpty()) {
			String restoredGenericChoicePath = reinstateChoiceSuffix(possibleSnapshotElementPath)
				.orElseThrow(() -> new IllegalStateException("No matching paths found, and not a resolved choice node: " + differentialPath));
			List<SnapshotTreeNode> possibleChoiceAncestorNodes = searchRoot.descendantsWithPath(restoredGenericChoicePath);
			
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

	private Optional<String> reinstateChoiceSuffix(String possibleSnapshotElementPath) {
		return matchSuffix(possibleSnapshotElementPath)
			.map(suffix -> 
				possibleSnapshotElementPath.substring(
					0, 
					possibleSnapshotElementPath.length() - suffix.length()) 
				+ "[x]");
	}

	private Optional<String> matchSuffix(String possibleSnapshotElementPath) {
		return choiceSuffixes
			.stream()
			.filter(eachSuffix -> possibleSnapshotElementPath.endsWith(eachSuffix))
			.findFirst();
	}

	void removeSlicingInfoMismatches(List<SnapshotTreeNode> matchingNodes) {
		for (int i=matchingNodes.size()-1; i>=0; i--) {
			SnapshotTreeNode matchingNode = matchingNodes.get(i);
			if (getData().hasSlicingInfo() != matchingNode.getData().hasSlicingInfo()) {
				matchingNodes.remove(i);
			}
		}
	}

	private List<SnapshotTreeNode> filterOnNameIfPresent(List<SnapshotTreeNode> toFilter) {
		Optional<String> sliceName = getData().getSliceName();
		
		if (sliceName.isPresent()
		  && !sliceName.get().isEmpty()) {
			
			String name = sliceName.get();
			
			List<SnapshotTreeNode> nameMatches = Lists.newArrayList();
			
			for (SnapshotTreeNode node : toFilter) {
				if (node.getData().getSliceName().isPresent()
				  && node.getData().getSliceName().get().equals(name)) {
					nameMatches.add(node);
				}
			}
			
			return Lists.newArrayList(ListUtils.expectUnique(nameMatches, "snapshot nodes matching path " + getPath() + " and name " + name));
		} else if (toFilter.size() > 1) {
			// no slice name to filter on
			EventHandlerContext.forThread().event(RendererEventType.DIFFERENTIAL_MISSING_SLICE_NAME,
				"No slice name available to identify node to match to a backup: " + (getData().getId().isPresent() ? getData().getId().get() : getPath()));
			
			if (getData().getId().isPresent()) {
				String id = getData().getId().get();
				List<SnapshotTreeNode> idMatches = Lists.newArrayList();
				
				for (SnapshotTreeNode node : toFilter) {
					if (node.getData().getId().isPresent()
					  && node.getData().getId().get().equals(id)) {
						idMatches.add(node);
					}
				}
				
				return idMatches;
				
			} else {
				return toFilter;
			}
		} else {
			return toFilter;
		}
	}
	
	/**
	 * Finds a matching node for a sliced element based on name or (failing that) on slicing discriminators.
	 */
	private SnapshotTreeNode matchOnNameOrDiscriminatorPaths(List<SnapshotTreeNode> pathMatches, SlicingInfo slicingInfo) {
		Set<String> discriminatorPaths = slicingInfo.getDiscriminatorPaths();
		
		// nodes which match on discriminator (as well as path)
		List<SnapshotTreeNode> discriminatorMatches = Lists.newArrayList();
		
		for (SnapshotTreeNode pathMatch : pathMatches) {
			boolean matchesOnDiscriminators = true;
			for (String discriminatorPath : discriminatorPaths) {
				if (!matchesOnDiscriminator(discriminatorPath, pathMatch)) {
					matchesOnDiscriminators = false;
					break;
				}
			}
			
			if (matchesOnDiscriminators) {
				discriminatorMatches.add(pathMatch);
			}
		}
		
		return ListUtils.expectUnique(discriminatorMatches, "matches (on all discriminators) for backupNode for slice " + getPath());
	}

	private boolean matchesOnDiscriminator(String discriminatorPath, SnapshotTreeNode pathMatch) {
		if (getData().getPathName().equals("extension")
		  && discriminatorPath.equals("url")) {
			Set<FhirURL> elementUrlDiscriminators = getExtensionUrlDiscriminators(getData().getTypeLinks());
			Set<FhirURL> pathMatchUrlDiscriminators = getExtensionUrlDiscriminators(pathMatch.getData().getTypeLinks());
			return elementUrlDiscriminators.equals(pathMatchUrlDiscriminators);
		}
		
		// most nodes
		String fullDiscriminatorPath = getPath() + "." + discriminatorPath;
		Optional<FhirDifferentialSkeletonNode> differentialDescendant = findUniqueDescendantMatchingPath(fullDiscriminatorPath);
		Optional<SnapshotTreeNode> snapshotDescendant = pathMatch.findUniqueDescendantMatchingPath(fullDiscriminatorPath);
		return differentialDescendant.isPresent()
		  && snapshotDescendant.isPresent()
		  && differentialDescendant.get().getData().getFixedValue().equals(snapshotDescendant.get().getData().getFixedValue());
	}

	public Set<FhirURL> getExtensionUrlDiscriminators(LinkDatas typeLinks) {
		return typeLinks
			.links()
			.stream()
			.filter(typeLink -> typeLink.getKey().getText().equals("Extension"))
			.flatMap(
				typeLink -> typeLink.getValue().isEmpty() ?
					Lists.newArrayList(typeLink.getKey()).stream() :
					typeLink.getValue().stream())
			.map(link -> link.getURL()).collect(Collectors.toSet());
	}

	@Override
	public FhirDifferentialSkeletonNode cloneShallow(FhirDifferentialSkeletonNode parent) {
		return new FhirDifferentialSkeletonNode(getData());
	}
}
