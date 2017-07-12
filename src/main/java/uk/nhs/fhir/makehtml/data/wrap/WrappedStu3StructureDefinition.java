package uk.nhs.fhir.makehtml.data.wrap;

import org.hl7.fhir.dstu3.model.StructureDefinition;

public class WrappedStu3StructureDefinition extends WrappedStructureDefinition {

	private final StructureDefinition definition;
	
	public WrappedStu3StructureDefinition(StructureDefinition definition) {
		this.definition = definition;
	}

}
