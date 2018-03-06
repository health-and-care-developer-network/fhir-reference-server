package uk.nhs.fhir.data.structdef.tree;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionSnapshotComponent;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.util.FhirVersion;
import uk.nhs.fhir.util.StructureDefinitionRepository;

public class FhirTreeDatas {
	
	public static CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> getSnapshotTree(
			WrappedStructureDefinition structureDefinition,
			Optional<StructureDefinitionRepository> structureDefinitions) {
		FhirVersion fhirVersion = structureDefinition.getImplicitFhirVersion();
		switch (fhirVersion) {
			case DSTU2:
				return dstu2Snapshot(structureDefinition, structureDefinitions);
			case STU3:
				return stu3Snapshot(structureDefinition, structureDefinitions);
			default:
				throw new IllegalStateException("Unexpected FHIR verison " + fhirVersion.toString());
		}
	}

	private static CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> dstu2Snapshot(
			WrappedStructureDefinition structureDefinition,
			Optional<StructureDefinitionRepository> structureDefinitions) {
		FhirTreeDataBuilder<SnapshotData, SnapshotTreeNode> fhirTreeDataBuilder = new FhirTreeDataBuilder<>();
		
		Snapshot snapshot = ((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		for (ElementDefinitionDt element : snapshot.getElement()) {
			WrappedElementDefinition wrappedElement = WrappedElementDefinition.fromDefinition(element);
			SnapshotData snapshotData = FhirTreeNodeDataBuilder.buildSnapshotNode(wrappedElement, structureDefinitions);
			SnapshotTreeNode snapshotNode = new SnapshotTreeNode(snapshotData);
			
			fhirTreeDataBuilder.addFhirTreeNode(snapshotNode);
		}
		
		return  new CloneableFhirTreeData<>(fhirTreeDataBuilder.getTree());
	}
	
	private static CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> stu3Snapshot(
			WrappedStructureDefinition structureDefinition,
			Optional<StructureDefinitionRepository> structureDefinitions) {
		FhirTreeDataBuilder<SnapshotData, SnapshotTreeNode> fhirTreeDataBuilder = new FhirTreeDataBuilder<>();
		
		StructureDefinitionSnapshotComponent snapshot = ((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		
		for (ElementDefinition element : snapshot.getElement()) {
			WrappedElementDefinition wrappedElement = WrappedElementDefinition.fromDefinition(element);
			SnapshotData snapshotData = FhirTreeNodeDataBuilder.buildSnapshotNode(wrappedElement, structureDefinitions);
			SnapshotTreeNode node = new SnapshotTreeNode(snapshotData);
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return new CloneableFhirTreeData<>(fhirTreeDataBuilder.getTree());
	}
	
	public static CloneableFhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTree(
			WrappedStructureDefinition structureDefinition, 
			CloneableFhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData,
			Optional<StructureDefinitionRepository> structureDefinitions) {
		
		FhirTreeData<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> backupNodeResolvingTree;
		FhirVersion fhirVersion = structureDefinition.getImplicitFhirVersion();
		switch (fhirVersion) {
			case DSTU2:
				backupNodeResolvingTree = dstu2DifferentialSkeleton((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource(), backupTreeData, structureDefinitions);
				break;
			case STU3:
				backupNodeResolvingTree = stu3DifferentialSkeleton((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource(), backupTreeData, structureDefinitions);
				break;
			default:
				throw new IllegalStateException("Unexpected FHIR version " + fhirVersion.toString());
		}
		
		FhirTreeDataBuilder<DifferentialData, DifferentialTreeNode> fhirDifferentialTreeDataBuilder = new FhirTreeDataBuilder<>(new EmptyDifferentialNodeFactory(backupTreeData));
		
		for (FhirDifferentialSkeletonNode skeletonNode : backupNodeResolvingTree.nodes()) {
			
			SnapshotTreeNode backupNode = skeletonNode.getBackupNode(backupTreeData);
			WrappedElementDefinition elementDefinition = skeletonNode.getData().getElement();

			if (elementDefinition != null) {
				// i.e. not a dummy node - dummy nodes will be inserted automatically by the EmptyDifferentialNodeFactory
				DifferentialData differentialNodeData = FhirTreeNodeDataBuilder.buildDifferentialNode(elementDefinition, backupNode, structureDefinitions);
				DifferentialTreeNode differentialNode = new DifferentialTreeNode(differentialNodeData);
				fhirDifferentialTreeDataBuilder.addFhirTreeNode(differentialNode);
			}
		}

		return  new CloneableFhirTreeData<>(fhirDifferentialTreeDataBuilder.getTree());
	}

	private static FhirTreeData<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> dstu2DifferentialSkeleton(
			ca.uhn.fhir.model.dstu2.resource.StructureDefinition structureDefinition, 
			FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData,
			Optional<StructureDefinitionRepository> structureDefinitions) {
		
		FhirTreeDataBuilder<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> fhirSkeletonTreeDataBuilder = new FhirTreeDataBuilder<>();
		for (ElementDefinitionDt element : structureDefinition.getDifferential().getElement()) {
			FhirDifferentialSkeletonData data = FhirTreeNodeDataBuilder.buildDifferentialSkeletonNode(WrappedElementDefinition.fromDefinition(element));
			FhirDifferentialSkeletonNode node = new FhirDifferentialSkeletonNode(data);
			fhirSkeletonTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirSkeletonTreeDataBuilder.getTree();
	}

	private static FhirTreeData<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> stu3DifferentialSkeleton(
			org.hl7.fhir.dstu3.model.StructureDefinition structureDefinition,
			FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData,
			Optional<StructureDefinitionRepository> structureDefinitions) {

		FhirTreeDataBuilder<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> fhirSkeletonTreeDataBuilder = 
				new FhirTreeDataBuilder<>(new FhirSkeletonEmptyNodeFactory());
		for (ElementDefinition element : structureDefinition.getDifferential().getElement()) {
			FhirDifferentialSkeletonData data = FhirTreeNodeDataBuilder.buildDifferentialSkeletonNode(WrappedElementDefinition.fromDefinition(element));
			FhirDifferentialSkeletonNode node = new FhirDifferentialSkeletonNode(data);
			fhirSkeletonTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirSkeletonTreeDataBuilder.getTree();
	}
}

class FhirSkeletonEmptyNodeFactory implements EmptyNodeFactory<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> {

	@Override
	public FhirDifferentialSkeletonNode create(FhirDifferentialSkeletonNode currentNode, NodePath path) {
		FhirDifferentialSkeletonData data = new FhirDifferentialSkeletonData(Optional.empty(), path.toPathString(), Optional.empty(), Optional.empty(), new LinkDatas(), Optional.empty(), null);

		return new FhirDifferentialSkeletonNode(data);
	}
	
}
