package uk.nhs.fhir.data.wrap.dstu2;

import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.base.Preconditions;

import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap.Element;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap.ElementTarget;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapElementTarget;
import uk.nhs.fhir.data.conceptmap.FhirConceptMapGroupCollection;
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

	/* In DSTU2, the organisation of mappings was:
	 * Concept Map
	 * 	-> Element (codesystem + code)
	 * 		-> Targets (each with a codesystem + code)
	 * 
	 * In STU3 (and so potentially going forwards) the organisation is:
	 * Concept Map
	 * 	-> Group [source code system and dest code system]
	 * 		-> Source code (potentially many)
	 * 			-> Target Code (potentially many)
	 * 
	 * So for consistency we reorganise the DSTU2 codes to match the STU3 system:
	 */
	@Override
	public FhirConceptMapGroupCollection getMappingGroups() {
		FhirConceptMapGroupCollection groups = new FhirConceptMapGroupCollection();
		
		for (Element element : definition.getElement()) {
			String fromCodeSystem = element.getCodeSystem();
			String fromCode = element.getCode();
			
			for (ElementTarget target : element.getTarget()) {
				String toCodeSystem = target.getCodeSystem();
				
				FhirConceptMapElementTarget mappingTarget = 
					new FhirConceptMapElementTarget(
						target.getCode(), 
						target.getEquivalence(), 
						Optional.ofNullable(target.getComments()));
				
				groups.add(fromCodeSystem, fromCode, toCodeSystem, mappingTarget);
			}
		}
		
		return groups;
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
