package uk.nhs.fhir.data.wrap.dstu2;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap.Element;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap.ElementTarget;
import uk.nhs.fhir.data.valueset.FhirConceptMapElement;
import uk.nhs.fhir.data.valueset.FhirConceptMapElementTarget;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedDstu2ConceptMap extends WrappedConceptMap {

	private final ConceptMap source;
	
	public WrappedDstu2ConceptMap(ConceptMap source) {
		this.source = source;
	}
	
	@Override
	public Optional<String> getUrl() {
		return Optional.ofNullable(source.getUrl());
	}

	@Override
	public Optional<String> getName() {
		return Optional.ofNullable(source.getName());
	}

	@Override
	public String getStatus() {
		return source.getStatus();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.ofNullable(source.getVersion());
	}

	@Override
	public Boolean getExperimental() {
		return source.getExperimental();
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.ofNullable(source.getDescription());
	}

	@Override
	public Optional<String> getPublisher() {
		return Optional.ofNullable(source.getPublisher());
	}

	@Override
	public Optional<String> getCopyright() {
		return Optional.ofNullable(source.getCopyright());
	}

	@Override
	public Optional<Date> getDate() {
		return Optional.ofNullable(source.getDate());
	}

	@Override
	public String getSource() {
		return ((ResourceReferenceDt) source.getSource()).getReference().getValue();
	}

	@Override
	public String getTarget() {
		return ((ResourceReferenceDt) source.getTarget()).getReference().getValue();
	}

	@Override
	public List<FhirConceptMapElement> getElements() {
		List<FhirConceptMapElement> elements = Lists.newArrayList();
		
		for (Element sourceElement : source.getElement()) {
			
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

}
