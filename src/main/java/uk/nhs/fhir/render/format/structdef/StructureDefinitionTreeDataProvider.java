package uk.nhs.fhir.render.format.structdef;

import uk.nhs.fhir.FhirTreeDatas;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNode;
import uk.nhs.fhir.render.tree.AbstractFhirTreeNodeData;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.DifferentialData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.SnapshotData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;
import uk.nhs.fhir.render.tree.cache.BackupNodeLocator;
import uk.nhs.fhir.render.tree.cache.IdLinkedNodeResolver;
import uk.nhs.fhir.render.tree.cache.NameLinkedNodeResolver;
import uk.nhs.fhir.render.tree.cache.SlicingDiscriminatorCacher;

public class StructureDefinitionTreeDataProvider {
	
	private final WrappedStructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(WrappedStructureDefinition source) {
		this.source = source;
	}
	
	public FhirTreeData<SnapshotData, SnapshotTreeNode> getSnapshotTreeData() {
		FhirTreeData<SnapshotData, SnapshotTreeNode> snapshotTree = FhirTreeDatas.getSnapshotTree(source);
		
		cacheTreeData(snapshotTree);
		
		return snapshotTree;
	}
	
	public FhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTreeData() {
		return getDifferentialTreeData(getSnapshotTreeData());
	}
	
	public FhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTreeData(FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {
		FhirTreeData<DifferentialData, DifferentialTreeNode> differentialTree = FhirTreeDatas.getDifferentialTree(source);
		
		new BackupNodeLocator(differentialTree, backupTreeData).resolve();
		
		cacheTreeData(differentialTree);
		
		return differentialTree;
	}
	
	/**
	 * Cache internal references between nodes. This can only be done once the entire tree is available.
	 */
	private <T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> void cacheTreeData(FhirTreeData<T, U> treeData) {
		new NameLinkedNodeResolver<>(treeData).resolve();
		new IdLinkedNodeResolver<>(treeData).resolve();
		new SlicingDiscriminatorCacher<U>(treeData).resolve();
	}
}
