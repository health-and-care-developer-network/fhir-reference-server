package uk.nhs.fhir.data.structdef.tree;

import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.util.ListUtils;

public class EmptyDifferentialNodeFactory implements EmptyNodeFactory<DifferentialData, DifferentialTreeNode> {
	private final FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData;
	private final FhirTreePathLookup<SnapshotData, SnapshotTreeNode> treeLookup;
	
	public EmptyDifferentialNodeFactory(FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {
		this.backupTreeData = backupTreeData;
		this.treeLookup = new FhirTreePathLookup<>(backupTreeData);
	}

	public DifferentialTreeNode create(DifferentialTreeNode parentNode, ImmutableNodePath mutablePath) {
		
		ImmutableNodePath path = mutablePath.immutableCopy();
		
		SnapshotTreeNode backupNode = findBackup(parentNode, path);
		
		DifferentialData data = new DifferentialData(
			backupNode.getData().getId(),
			Optional.empty(), 
			new ResourceFlags(), 
			Optional.empty(), 
			Optional.empty(), 
			new LinkDatas(),
			"",
			Lists.newArrayList(),
			path,
			FhirElementDataType.DELEGATED_TYPE,
			backupTreeData.getRoot().getData().getVersion(),
			backupNode);
		
		return new DifferentialTreeNode(data, parentNode, true);
	}

	private SnapshotTreeNode findBackup(DifferentialTreeNode parentNode, ImmutableNodePath nodePath) {
		if (parentNode == null) {
			return backupTreeData.getRoot();
		} else {
			return 
				ListUtils.expectUnique(
					parentNode
						.getBackupNode()
						.getChildren()
						.stream()
						.filter(child -> treeLookup.nodesForPath(nodePath).contains(child))
						.collect(Collectors.toList()), 
						"immediate descendants matching path " + nodePath);
		}	
	}
}
