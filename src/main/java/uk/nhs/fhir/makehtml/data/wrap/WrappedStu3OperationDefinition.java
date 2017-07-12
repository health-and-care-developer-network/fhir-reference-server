package uk.nhs.fhir.makehtml.data.wrap;

import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.instance.model.api.IBaseMetaType;

import uk.nhs.fhir.makehtml.FhirVersion;

public class WrappedStu3OperationDefinition extends WrappedOperationDefinition {

private final OperationDefinition definition;
	
	public WrappedStu3OperationDefinition(OperationDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}
}
