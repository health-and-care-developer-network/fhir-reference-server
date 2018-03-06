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
	private final FhirTreeContentsLookup<SnapshotData, SnapshotTreeNode> treeLookup;
	
	public EmptyDifferentialNodeFactory(FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {
		this.backupTreeData = backupTreeData;
		this.treeLookup = new FhirTreeContentsLookup<>(backupTreeData);
	}

	public DifferentialTreeNode create(DifferentialTreeNode parentNode, NodePath nodePath) {
		
		SnapshotTreeNode backupNode = findBackup(parentNode, nodePath);
		
		DifferentialData data = new DifferentialData(
			backupNode.getData().getId(),
			Optional.empty(), 
			new ResourceFlags(), 
			Optional.empty(), 
			Optional.empty(), 
			new LinkDatas(),
			"",
			Lists.newArrayList(),
			nodePath.toPathString(),
			FhirElementDataType.DELEGATED_TYPE,
			backupTreeData.getRoot().getData().getVersion(),
			backupNode);
		
		return new DifferentialTreeNode(data, parentNode, true);
	}

	private SnapshotTreeNode findBackup(DifferentialTreeNode parentNode, NodePath nodePath) {
		if (parentNode == null) {
			return backupTreeData.getRoot();
		} else {
			String backupNodePath = nodePath.toPathString();
			
			return 
				ListUtils.expectUnique(
					parentNode
						.getBackupNode()
						.getChildren()
						.stream()
						.filter(child -> treeLookup.nodesForPath(backupNodePath).contains(child))
						.collect(Collectors.toList()), 
						"immediate descendants matching path " + backupNodePath);
		}	
	}
}
