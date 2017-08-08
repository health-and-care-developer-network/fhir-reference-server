package uk.nhs.fhir.makehtml.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.TargetElementComponent;
import org.hl7.fhir.dstu3.model.Reference;

import com.google.common.collect.Lists;

import uk.nhs.fhir.makehtml.FhirVersion;
import uk.nhs.fhir.makehtml.data.FhirConceptMapElement;
import uk.nhs.fhir.makehtml.data.FhirConceptMapElementTarget;

public class WrappedStu3ConceptMap extends WrappedConceptMap {

	private final ConceptMap source;
	
	public WrappedStu3ConceptMap(ConceptMap source) {
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
		return source.getStatus().getDisplay();
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
		return ((Reference) source.getSource()).getReference();
	}

	@Override
	public String getTarget() {
		return ((Reference) source.getTarget()).getReference();
	}

	@Override
	public List<FhirConceptMapElement> getElements() {
		List<FhirConceptMapElement> elements = Lists.newArrayList();
		
		for (ConceptMapGroupComponent group : source.getGroup()) {
			for (SourceElementComponent sourceElement : group.getElement()) {
				FhirConceptMapElement element = new FhirConceptMapElement(sourceElement.getCode());
				
				for (TargetElementComponent sourceTarget : sourceElement.getTarget()) {
					
					FhirConceptMapElementTarget target = 
						new FhirConceptMapElementTarget(
							sourceTarget.getCode(), 
							sourceTarget.getEquivalence().getDisplay(), 
							Optional.ofNullable(sourceTarget.getComment()));
					
					element.addTarget(target);
				}
				
				elements.add(element);
			}
		}
		
		return elements;
	}

	@Override
	public FhirVersion getImplicitFhirVersion() {
		return FhirVersion.STU3;
	}

}
