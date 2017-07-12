package uk.nhs.fhir.makehtml.data.wrap;

import org.hl7.fhir.instance.model.api.IBaseMetaType;

import ca.uhn.fhir.model.dstu2.resource.OperationDefinition;
import uk.nhs.fhir.makehtml.FhirVersion;

public class WrappedDstu2OperationDefinition extends WrappedOperationDefinition {

	private final OperationDefinition definition;
	
	public WrappedDstu2OperationDefinition(OperationDefinition definition) {
		this.definition = definition;
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
	}

}
