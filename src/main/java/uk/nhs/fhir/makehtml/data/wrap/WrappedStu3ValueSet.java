package uk.nhs.fhir.makehtml.data.wrap;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.FhirCodeSystem;
import uk.nhs.fhir.makehtml.data.FhirValueSetCompose;

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

	@Override
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
	}

	@Override
	public void setCopyright(String copyRight) {
		definition.setCopyright(copyRight);
	}

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
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
	public void addHumanReadableText(String textSection) {
		try {
			Narrative textElement = Factory.newNarrative(NarrativeStatus.GENERATED, textSection);
	        definition.setText(textElement);
		} catch (IOException | FHIRException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public String getStatus() {
		return definition.getStatus().getDisplay();
	}

	@Override
	public List<WrappedConceptMap> getConceptMaps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getOid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getRequirements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Date> getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasComposeIncludeFilter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FhirCodeSystem getCodeSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FhirValueSetCompose getCompose() {
		// TODO Auto-generated method stub
		return null;
	}

}
