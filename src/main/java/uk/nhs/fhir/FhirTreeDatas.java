package uk.nhs.fhir;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionDifferentialComponent;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionSnapshotComponent;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Differential;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.tree.AbstractFhirTreeTableContent;
import uk.nhs.fhir.render.tree.DifferentialData;
import uk.nhs.fhir.render.tree.DifferentialTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeDataBuilder;
import uk.nhs.fhir.render.tree.FhirTreeNodeBuilder;
import uk.nhs.fhir.render.tree.FhirTreeNodeDataBuilder;
import uk.nhs.fhir.render.tree.SnapshotData;
import uk.nhs.fhir.render.tree.SnapshotTreeNode;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeDatas {

	private static final FhirTreeNodeBuilder<SnapshotData, SnapshotTreeNode> snapshotTreeNodeBuilder = new FhirTreeNodeBuilder<>();
	private static final FhirTreeNodeBuilder<DifferentialData, DifferentialTreeNode> differentialTreeNodeBuilder = new FhirTreeNodeBuilder<>();
	private static final EmptyDifferentialNodeFactory emptyDifferentialNodeFactory = new EmptyDifferentialNodeFactory();
	
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
			SnapshotData snapshotData = snapshotTreeNodeBuilder.buildSnapshotNode(wrappedElement);
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
			SnapshotData snapshotData = snapshotTreeNodeBuilder.fromElementDefinition(wrappedElement).toSnapshotData();
			SnapshotTreeNode node = new SnapshotTreeNode(snapshotData);
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	public static FhirTreeData<DifferentialData, DifferentialTreeNode> getDifferentialTree(WrappedStructureDefinition structureDefinition) {

		FhirVersion fhirVersion = structureDefinition.getImplicitFhirVersion();
		switch (fhirVersion) {
			case DSTU2:
				return dstu2Differential((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource());
			case STU3:
				return stu3Differential((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource());
			default:
				throw new IllegalStateException("Unexpected FHIR verison " + fhirVersion.toString());
		}
	}

	private static FhirTreeData<DifferentialData, DifferentialTreeNode> dstu2Differential(ca.uhn.fhir.model.dstu2.resource.StructureDefinition structureDefinition) {
		FhirTreeDataBuilder<DifferentialData, DifferentialTreeNode> fhirTreeDataBuilder = new FhirTreeDataBuilder<>(emptyDifferentialNodeFactory);
		
		Differential differential = structureDefinition.getDifferential();
		for (ElementDefinitionDt element : differential.getElement()) {
			
			DifferentialTreeNode node = differentialTreeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element)).toDifferentialData(backupNode);
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
	}

	private static FhirTreeData<DifferentialData, DifferentialTreeNode> stu3Differential(org.hl7.fhir.dstu3.model.StructureDefinition structureDefinition) {

		FhirTreeDataBuilder<DifferentialData, DifferentialTreeNode> fhirTreeDataBuilder = new FhirTreeDataBuilder<>(emptyDifferentialNodeFactory);

		StructureDefinitionDifferentialComponent differential = structureDefinition.getDifferential();
		
		for (ElementDefinition element : differential.getElement()) {
			DifferentialTreeNode node = differentialTreeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
	}
}
