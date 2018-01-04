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
import uk.nhs.fhir.render.tree.DifferentialTreeDataBuilder;
import uk.nhs.fhir.render.tree.FhirDummyNodeFactory;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeNodeBuilder;
import uk.nhs.fhir.render.tree.SnapshotTreeDataBuilder;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeDatas {

	private static final FhirTreeNodeBuilder treeNodeBuilder = new FhirTreeNodeBuilder();
	
	public static FhirTreeData<AbstractFhirTreeTableContent> getSnapshotTree(WrappedStructureDefinition structureDefinition) {
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

	private static FhirTreeData<AbstractFhirTreeTableContent> dstu2Snapshot(WrappedStructureDefinition structureDefinition) {
		SnapshotTreeDataBuilder<AbstractFhirTreeTableContent> fhirTreeDataBuilder = new SnapshotTreeDataBuilder<>();
		
		Snapshot snapshot = ((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		for (ElementDefinitionDt element : snapshot.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	private static FhirTreeData<AbstractFhirTreeTableContent> stu3Snapshot(WrappedStructureDefinition structureDefinition) {
		SnapshotTreeDataBuilder<AbstractFhirTreeTableContent> fhirTreeDataBuilder = new SnapshotTreeDataBuilder<>();
		
		StructureDefinitionSnapshotComponent snapshot = ((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		
		for (ElementDefinition element : snapshot.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	public static FhirTreeData<AbstractFhirTreeTableContent> getDifferentialTree(WrappedStructureDefinition structureDefinition) {

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

	private static FhirTreeData<AbstractFhirTreeTableContent> dstu2Differential(ca.uhn.fhir.model.dstu2.resource.StructureDefinition structureDefinition) {
		DifferentialTreeDataBuilder<AbstractFhirTreeTableContent> fhirTreeDataBuilder = new DifferentialTreeDataBuilder<>(new FhirDummyNodeFactory());
		
		Differential differential = structureDefinition.getDifferential();
		for (ElementDefinitionDt element : differential.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
	}

	private static FhirTreeData<AbstractFhirTreeTableContent> stu3Differential(org.hl7.fhir.dstu3.model.StructureDefinition structureDefinition) {

		DifferentialTreeDataBuilder<AbstractFhirTreeTableContent> fhirTreeDataBuilder = new DifferentialTreeDataBuilder<>(new FhirDummyNodeFactory());

		StructureDefinitionDifferentialComponent differential = structureDefinition.getDifferential();
		
		for (ElementDefinition element : differential.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
	}
}
