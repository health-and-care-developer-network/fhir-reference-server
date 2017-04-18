package uk.nhs.fhir.makehtml;

import java.util.List;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.makehtml.data.FhirTreeBuilder;
import uk.nhs.fhir.makehtml.data.FhirTreeData;

public class StructureDefinitionTreeDataProvider {
	
	private final StructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(StructureDefinition source) {
		this.source = source;
	}
	
	public FhirTreeData getSnapshotTreeData() {

		FhirTreeBuilder fhirTreeBuilder = new FhirTreeBuilder();
		
		Snapshot snapshot = source.getSnapshot();
		List<ElementDefinitionDt> snapshotElements = snapshot.getElement();
		for (ElementDefinitionDt elementDefinition : snapshotElements) {
			fhirTreeBuilder.addElementDefinition(elementDefinition);
		}
		
		FhirTreeData tree = fhirTreeBuilder.getTree();
		
		return tree;
	}
	
	public FhirTreeData getDifferentialTreeData() {
		DifferentialNodeBuilder nodeBuilder = new DifferentialNodeBuilder(getSnapshotTreeData());
		FhirTreeBuilder fhirTreeBuilder = new FhirTreeBuilder(nodeBuilder);
		
		List<ElementDefinitionDt> differentialElements = source.getDifferential().getElement();
		for (ElementDefinitionDt differentialElement : differentialElements) {
			fhirTreeBuilder.addElementDefinition(differentialElement);
		}
		
		return fhirTreeBuilder.getTree();
	}
}
