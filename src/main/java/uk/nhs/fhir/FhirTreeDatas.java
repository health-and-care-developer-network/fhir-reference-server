package uk.nhs.fhir;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionSnapshotComponent;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.data.url.LinkDatas;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.tree.DifferentialData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.EmptyNodeFactory;
import uk.nhs.fhir.render.tree.FhirDifferentialSkeletonData;
import uk.nhs.fhir.render.tree.FhirDifferentialSkeletonNode;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeDataBuilder;
import uk.nhs.fhir.render.tree.FhirTreeNodeDataBuilder;
import uk.nhs.fhir.render.tree.NodePath;
import uk.nhs.fhir.render.tree.SnapshotData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeDatas {
	
	public static FhirTreeData<SnapshotData, SnapshotTreeNode> getSnapshotTree(WrappedStructureDefinition structureDefinition) {
		FhirVersion fhirVersion = structureDefinition.getImplicitFhirVersion();
		switch (fhirVersion) {
			case DSTU2:
				return dstu2Snapshot(structureDefinition);
			case STU3:
				return stu3Snapshot(structureDefinition);
			default:
				throw new IllegalStateException("Unexpected FHIR verison " + fhirVersion.toString());
		}
	}

	private static FhirTreeData<SnapshotData, SnapshotTreeNode> dstu2Snapshot(WrappedStructureDefinition structureDefinition) {
		FhirTreeDataBuilder<SnapshotData, SnapshotTreeNode> fhirTreeDataBuilder = new FhirTreeDataBuilder<>();
		
		Snapshot snapshot = ((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		for (ElementDefinitionDt element : snapshot.getElement()) {
			WrappedElementDefinition wrappedElement = WrappedElementDefinition.fromDefinition(element);
			SnapshotData snapshotData = FhirTreeNodeDataBuilder.buildSnapshotNode(wrappedElement);
			SnapshotTreeNode snapshotNode = new SnapshotTreeNode(snapshotData);
			
			fhirTreeDataBuilder.addFhirTreeNode(snapshotNode);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	private static FhirTreeData<SnapshotData, SnapshotTreeNode> stu3Snapshot(WrappedStructureDefinition structureDefinition) {
		FhirTreeDataBuilder<SnapshotData, SnapshotTreeNode> fhirTreeDataBuilder = new FhirTreeDataBuilder<>();
		
		StructureDefinitionSnapshotComponent snapshot = ((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		
		for (ElementDefinition element : snapshot.getElement()) {
			WrappedElementDefinition wrappedElement = WrappedElementDefinition.fromDefinition(element);
			SnapshotData snapshotData = FhirTreeNodeDataBuilder.buildSnapshotNode(wrappedElement);
			SnapshotTreeNode node = new SnapshotTreeNode(snapshotData);
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	public static FhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTree(
			WrappedStructureDefinition structureDefinition, FhirTreeData<SnapshotData, 
			SnapshotTreeNode> backupTreeData) {

		FhirTreeData<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> backupNodeResolvingTree;
		FhirVersion fhirVersion = structureDefinition.getImplicitFhirVersion();
		switch (fhirVersion) {
			case DSTU2:
				backupNodeResolvingTree = dstu2Differential((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource(), backupTreeData);
				break;
			case STU3:
				backupNodeResolvingTree = stu3Differential((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource(), backupTreeData);
				break;
			default:
				throw new IllegalStateException("Unexpected FHIR version " + fhirVersion.toString());
		}
		
		FhirTreeDataBuilder<DifferentialData, DifferentialTreeNode> fhirDifferentialTreeDataBuilder = new FhirTreeDataBuilder<>(new EmptyDifferentialNodeFactory(backupTreeData));
		
		for (FhirDifferentialSkeletonNode skeletonNode : backupNodeResolvingTree.nodes()) {
			
			SnapshotTreeNode backupNode = skeletonNode.getBackupNode(backupTreeData);
			WrappedElementDefinition elementDefinition = skeletonNode.getData().getElement();

			if (elementDefinition != null) {
				// i.e. not a dummy node
				DifferentialData differentialNodeData = FhirTreeNodeDataBuilder.buildDifferentialNode(elementDefinition, backupNode);
				DifferentialTreeNode differentialNode = new DifferentialTreeNode(differentialNodeData);
				fhirDifferentialTreeDataBuilder.addFhirTreeNode(differentialNode);
			}
		}

		return fhirDifferentialTreeDataBuilder.getTree();
	}

	private static FhirTreeData<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> dstu2Differential(
			ca.uhn.fhir.model.dstu2.resource.StructureDefinition structureDefinition, 
			FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {
		
		FhirTreeDataBuilder<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> fhirSkeletonTreeDataBuilder = new FhirTreeDataBuilder<>();
		for (ElementDefinitionDt element : structureDefinition.getDifferential().getElement()) {
			FhirDifferentialSkeletonData data = FhirTreeNodeDataBuilder.buildDifferentialSkeletonNode(WrappedElementDefinition.fromDefinition(element));
			FhirDifferentialSkeletonNode node = new FhirDifferentialSkeletonNode(data);
			fhirSkeletonTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirSkeletonTreeDataBuilder.getTree();
	}

	private static FhirTreeData<FhirDifferentialSkeletonData, FhirDifferentialSkeletonNode> stu3Differential(
			org.hl7.fhir.dstu3.model.StructureDefinition structureDefinition,
			FhirTreeData<SnapshotData, SnapshotTreeNode> backupTreeData) {

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
		FhirDifferentialSkeletonData data = new FhirDifferentialSkeletonData(path.toPathString(), Optional.empty(), Optional.empty(), new LinkDatas(), Optional.empty(), null);

		return new FhirDifferentialSkeletonNode(data);
	}
	
}
