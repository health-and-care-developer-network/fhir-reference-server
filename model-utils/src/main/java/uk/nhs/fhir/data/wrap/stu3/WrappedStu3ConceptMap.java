package uk.nhs.fhir.data.wrap.stu3;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.TargetElementComponent;
import org.hl7.fhir.dstu3.model.Factory;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElementTarget;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapGroupCollection;
import uk.nhs.fhir.data.wrap.WrappedConceptMap;
import uk.nhs.fhir.util.FhirVersion;

public class WrappedStu3ConceptMap extends WrappedConceptMap {

	private final ConceptMap definition;
	
	public WrappedStu3ConceptMap(ConceptMap source) {
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
		return definition.getStatus().getDisplay();
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
		return ((Reference) definition.getSource()).getReference();
	}

	@Override
	public String getTarget() {
		return ((Reference) definition.getTarget()).getReference();
	}

	@Override
	public FhirConceptMapGroupCollection getMappingGroups() {
		FhirConceptMapGroupCollection groups = new FhirConceptMapGroupCollection();
		
		for (ConceptMapGroupComponent group : definition.getGroup()) {
			String fromCodeSystem = group.getSource();
			String toCodeSystem = group.getTarget();
			
			for (SourceElementComponent element : group.getElement()) {
				String fromCode = element.getCode();
				
				for (TargetElementComponent target : element.getTarget()) {
					FhirConceptMapElementTarget mappingTarget = 
						new FhirConceptMapElementTarget(
							target.getCode(), 
							target.getEquivalence().toCode(), 
							Optional.ofNullable(target.getComment()));
					
					groups.add(fromCodeSystem, fromCode, toCodeSystem, mappingTarget);
				}
			}
		}
		
		
		return groups;
	}
	
	public List<FhirConceptMapElement> getElements() {
		List<FhirConceptMapElement> elements = Lists.newArrayList();
		
		for (ConceptMapGroupComponent group : definition.getGroup()) {
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

	@Override
	public IBaseResource getWrappedResource() {
		return definition;
	}
	
	@Override
	public String getSourceUrl() {
		try {
			Reference sourceReference = definition.getSourceReference();
			return sourceReference.getDisplay();
		} catch (FHIRException e) {
			throw new IllegalStateException(e);
		}
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
		try {
			Narrative textElement = Factory.newNarrative(NarrativeStatus.GENERATED, textSection);
	        definition.setText(textElement);
		} catch (IOException | FHIRException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public void clearHumanReadableText() {
		definition.setText(null);
	}
}
