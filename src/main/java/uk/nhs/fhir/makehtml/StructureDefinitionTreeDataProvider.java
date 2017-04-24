package uk.nhs.fhir.makehtml;

import ca.uhn.fhir.model.dstu2.composite.ElementDefinitionDt;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Differential;
import ca.uhn.fhir.model.dstu2.resource.StructureDefinition.Snapshot;
import uk.nhs.fhir.makehtml.data.FhirTreeBuilder;
import uk.nhs.fhir.makehtml.data.FhirTreeData;

import java.util.List;

public class StructureDefinitionTreeDataProvider {
	
	private final StructureDefinition source;
	
	public StructureDefinitionTreeDataProvider(StructureDefinition source) {
		this.source = source;
	}
	
	public FhirTreeData getTreeData() {

		FhirTreeBuilder fhirTreeBuilder = new FhirTreeBuilder();
		
		Snapshot snapshot = source.getSnapshot();
		List<ElementDefinitionDt> snapshotElements = snapshot.getElement();
		for (ElementDefinitionDt elementDefinition : snapshotElements) {
			fhirTreeBuilder.addElementDefinition(elementDefinition);
		}
		
		FhirTreeData tree = fhirTreeBuilder.getTree();
		
		Differential differential = source.getDifferential();
		List<ElementDefinitionDt> differentialElements = differential.getElement();
		for (ElementDefinitionDt differentialElement : differentialElements) {
			// min/max are optional, and default back to the base if not present
		}
		
		return tree;
	}
}
