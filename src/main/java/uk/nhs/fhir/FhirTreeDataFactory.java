package uk.nhs.fhir;

import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionDifferentialComponent;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionSnapshotComponent;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Differential;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.data.wrap.WrappedElementDefinition;
import uk.nhs.fhir.data.wrap.WrappedStructureDefinition;
import uk.nhs.fhir.render.tree.FhirTreeData;
import uk.nhs.fhir.render.tree.FhirTreeDataBuilder;
import uk.nhs.fhir.render.tree.FhirTreeNode;
import uk.nhs.fhir.render.tree.FhirTreeNodeBuilder;
import uk.nhs.fhir.util.FhirVersion;

public class FhirTreeDataFactory {

	private static final FhirTreeNodeBuilder treeNodeBuilder = new FhirTreeNodeBuilder();
	
	public static FhirTreeData getSnapshotTree(WrappedStructureDefinition structureDefinition) {
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

	private static FhirTreeData dstu2Snapshot(WrappedStructureDefinition structureDefinition) {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		
		Snapshot snapshot = ((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		for (ElementDefinitionDt element : snapshot.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	private static FhirTreeData stu3Snapshot(WrappedStructureDefinition structureDefinition) {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		
		StructureDefinitionSnapshotComponent snapshot = ((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource()).getSnapshot();
		
		for (ElementDefinition element : snapshot.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}
		
		return fhirTreeDataBuilder.getTree();
	}
	
	public static FhirTreeData getDifferentialTree(WrappedStructureDefinition structureDefinition) {

		FhirVersion fhirVersion = structureDefinition.getImplicitFhirVersion();
		switch (fhirVersion) {
			case DSTU2:
				return dstu2Differential(structureDefinition);
			case STU3:
				return stu3Differential(structureDefinition);
			default:
				throw new IllegalStateException("Unexpected FHIR verison " + fhirVersion.toString());
		}
	}

	private static FhirTreeData dstu2Differential(WrappedStructureDefinition structureDefinition) {
		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		fhirTreeDataBuilder.permitDummyNodes();
		
		Differential differential = ((ca.uhn.fhir.model.dstu2.resource.StructureDefinition)structureDefinition.getWrappedResource()).getDifferential();
		for (ElementDefinitionDt element : differential.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
		
	}

	private static FhirTreeData stu3Differential(WrappedStructureDefinition structureDefinition) {

		FhirTreeDataBuilder fhirTreeDataBuilder = new FhirTreeDataBuilder();
		fhirTreeDataBuilder.permitDummyNodes();

		StructureDefinitionDifferentialComponent differential = ((org.hl7.fhir.dstu3.model.StructureDefinition)structureDefinition.getWrappedResource()).getDifferential();
		
		for (ElementDefinition element : differential.getElement()) {
			FhirTreeNode node = treeNodeBuilder.fromElementDefinition(WrappedElementDefinition.fromDefinition(element));
			fhirTreeDataBuilder.addFhirTreeNode(node);
		}

		return fhirTreeDataBuilder.getTree();
	}
}
