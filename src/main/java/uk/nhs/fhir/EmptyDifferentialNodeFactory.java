package uk.nhs.fhir;

import java.util.Optional;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.structdef.FhirElementDataType;
import uk.nhs.fhir.data.structdef.ResourceFlags;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.render.tree.DifferentialData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.EmptyNodeFactory;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.NodePath;
import uk.nhs.fhir.render.tree.SnapshotData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;
import uk.nhs.fhir.util.ListUtils;

public class EmptyDifferentialNodeFactory implements EmptyNodeFactory<DifferentialData, DifferentialTreeNode> {
	private final FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData;

	public EmptyDifferentialNodeFactory(FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {
		this.backupTreeData = backupTreeData;
	}

	public DifferentialTreeNode create(DifferentialTreeNode parentNode, NodePath nodePath) {
		
		SnapshotTreeNode backupNode = findBackup(parentNode, nodePath);
		
		DifferentialData data = new DifferentialData(
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
						.descendantsWithPath(backupNodePath), 
						"immediate descendants matching path " + backupNodePath);
		}	
	}
}
