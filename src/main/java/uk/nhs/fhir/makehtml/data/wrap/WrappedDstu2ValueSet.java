package uk.nhs.fhir.makehtml.data.wrap;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.ValueSet;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.makehtml.FhirVersion;

public class WrappedDstu2ValueSet extends WrappedValueSet {
	private final ValueSet definition;
	
	public WrappedDstu2ValueSet(ValueSet definition) {
		this.definition = definition;
	}

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}
	
	@Override
	public IBaseMetaType getSourceMeta() {
		return definition.getMeta();
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
	}

	@Override
	public void setUrl(String url) {
		definition.setUrl(url);
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public Optional<String> getCopyright() {
		return Optional.of(definition.getCopyright());
	}

	@Override
	public void setCopyright(String copyRight) {
		definition.setCopyright(copyRight);
	}

	@Override
	public void addHumanReadableText(String textSection) {
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        definition.setText(textElement);
	}

}
