package uk.nhs.fhir.makehtml.data.wrap;

import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.instance.model.api.IBaseMetaType;

import uk.nhs.fhir.makehtml.FhirVersion;

public class WrappedStu3ValueSet extends WrappedValueSet {

	private final ValueSet definition;

	public WrappedStu3ValueSet(ValueSet definition) {
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
