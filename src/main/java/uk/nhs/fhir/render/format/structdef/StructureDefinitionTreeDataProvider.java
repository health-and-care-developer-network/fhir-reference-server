package uk.nhs.fhir.render.format.structdef;

import uk.nhs.fhir.FhirTreeDatas;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.cache.BackupNodeCacher;
import uk.nhs.fhir.render.tree.cache.IdLinkedNodeResolver;
import uk.nhs.fhir.render.tree.cache.NameLinkedNodeResolver;
import uk.nhs.fhir.render.tree.cache.SlicingDiscriminatorCacher;

public class StructureDefinitionTreeDataProvider {
	
	private final WrappedStructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(WrappedStructureDefinition source) {
		this.source = source;
	}
	
	public FhirTreeData<AbstractFhirTreeTableContent> getSnapshotTreeData() {
		FhirTreeData<AbstractFhirTreeTableContent> snapshotTree = FhirTreeDatas.getSnapshotTree(source);
		
		cacheTreeData(snapshotTree);
		
		return snapshotTree;
	}
	
	public FhirTreeData<AbstractFhirTreeTableContent> getDifferentialTreeData() {
		return getDifferentialTreeData(getSnapshotTreeData());
	}
	
	public FhirTreeData<AbstractFhirTreeTableContent> getDifferentialTreeData(FhirTreeData<AbstractFhirTreeTableContent> backupTreeData) {
		FhirTreeData<AbstractFhirTreeTableContent> differentialTree = FhirTreeDatas.getDifferentialTree(source);
		
		new BackupNodeCacher(differentialTree, backupTreeData).resolve();
		
		cacheTreeData(differentialTree);
		
		return differentialTree;
	}
	
	/**
	 * Cache internal references between nodes. This can only be done once the entire tree is available.
	 */
	private void cacheTreeData(FhirTreeData<AbstractFhirTreeTableContent> treeData) {
		new NameLinkedNodeResolver(treeData).resolve();
		new IdLinkedNodeResolver(treeData).resolve();
		new SlicingDiscriminatorCacher(treeData).resolve();
	}
}
