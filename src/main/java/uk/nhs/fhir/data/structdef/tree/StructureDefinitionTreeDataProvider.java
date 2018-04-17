package uk.nhs.fhir.data.structdef.tree;

import java.util.Optional;
import java.util.Set;

import uk.nhs.fhir.data.structdef.tree.cache.IdLinkedNodeResolver;
import uk.nhs.fhir.data.structdef.tree.cache.NameLinkedNodeResolver;
import uk.nhs.fhir.data.structdef.tree.cache.SlicingDiscriminatorCacher;
import uk.nhs.fhir.data.structdef.tree.validate.NodeMappingValidator;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public class StructureDefinitionTreeDataProvider {
	
	private final WrappedStructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(WrappedStructureDefinition source) {
		this.source = source;
	}
	
	public CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> getSnapshotTreeData(Optional<StructureDefinitionRepository> structureDefinitions,
			Set<String> permittedMissingExtensionPrefixes) {
		CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> snapshotTree = 
			FhirTreeDatas.getSnapshotTree(source, structureDefinitions, permittedMissingExtensionPrefixes);
		
		cacheTreeData(snapshotTree);
		
		validateTreeData(snapshotTree);
		
		return snapshotTree;
	}
	
	private <T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> void validateTreeData(FhirTreeData<T, U> tree) {
		for (AbstractFhirTreeNodeData data : tree) {
			new NodeMappingValidator(data.getMappings(), data.getPath()).validate();
		}
	}
	
	public CloneableFhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTreeData(
			CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData, 
			Optional<StructureDefinitionRepository> structureDefinitions,
			Set<String> permittedMissingExtensionPrefixes) {
		CloneableFhirTreeData<DifferentialData, DifferentialTreeNode> differentialTree = 
			FhirTreeDatas.getDifferentialTree(source, backupTreeData, structureDefinitions, permittedMissingExtensionPrefixes);
		
		cacheTreeData(differentialTree);
		
		validateTreeData(differentialTree);
		
		return differentialTree;
	}
	
	/**
	 * Cache internal references between nodes. This can only be done once the entire tree is available.
	 */
	private <T extends AbstractFhirTreeNodeData, U extends AbstractFhirTreeNode<T, U>> void cacheTreeData(
			FhirTreeData<T, U> treeData) {
		new NameLinkedNodeResolver<>(treeData).resolve();
		new IdLinkedNodeResolver<>(treeData).resolve();
		new SlicingDiscriminatorCacher<>(treeData).resolve();
	}
}
