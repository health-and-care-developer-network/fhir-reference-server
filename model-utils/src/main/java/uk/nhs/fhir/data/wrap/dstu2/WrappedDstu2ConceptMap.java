package uk.nhs.fhir.data.wrap.dstu2;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap.Element;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap.ElementTarget;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElementTarget;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedDstu2ConceptMap extends WrappedConceptMap {

	private final ConceptMap definition;
	
	public WrappedDstu2ConceptMap(ConceptMap source) {
		this.definition = source;
	}
	
	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(definition.getUrl());
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public String getStatus() {
		return definition.getStatus();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(definition.getVersion());
	}

	@Override
	public Boolean getExperimental() {
		return definition.getExperimental();
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(definition.getDescription());
	}

	@Override
	public Optional<String> getPublisher() {
		return Optional.ofNullable(definition.getPublisher());
	}

	@Override
	public Optional<String> getCopyright() {
		return Optional.ofNullable(definition.getCopyright());
	}

	@Override
	public Optional<Date> getDate() {
		return Optional.ofNullable(definition.getDate());
	}

	@Override
	public String getSource() {
		return ((ResourceReferenceDt) definition.getSource()).getReference().getValue();
	}

	@Override
	public String getTarget() {
		return ((ResourceReferenceDt) definition.getTarget()).getReference().getValue();
	}

	@Override
	public String getSourceUrl() {
		// IDatatype source = definition.getSource();
		// return source.toString();
		throw new IllegalStateException("Test with real data to ensure toString returns the URL as expected");
	}

	@Override
	public List<FhirConceptMapElement> getElements() {
		List<FhirConceptMapElement> elements = Lists.newArrayList();
		
		for (Element sourceElement : definition.getElement()) {
			
			FhirConceptMapElement element = new FhirConceptMapElement(sourceElement.getCode());
			
			for (ElementTarget sourceTarget : sourceElement.getTarget()) {
				
				FhirConceptMapElementTarget target = 
					new FhirConceptMapElementTarget(
						sourceTarget.getCode(), 
						sourceTarget.getEquivalence(), 
						Optional.ofNullable(sourceTarget.getComments()));
				
				element.addTarget(target);
			}
			
			elements.add(element);
		}
		
		return elements;
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.DSTU2;
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
	public void setUrl(String url) {
		definition.setUrl(Preconditions.checkNotNull(url));
	}

	@Override
	public void addHumanReadableText(String textSection) {
		NarrativeDt textElement = new NarrativeDt();
        textElement.setStatus(NarrativeStatusEnum.GENERATED);
        textElement.setDiv(textSection);
        definition.setText(textElement);
	}

}
