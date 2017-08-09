package uk.nhs.fhir.data.wrap;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.valueset.FhirConceptMapElement;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ConceptMap;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ConceptMap;
import uk.nhs.fhir.util.FhirVersion;

public abstract class WrappedConceptMap {
	
	public abstract Optional<String> getUrl();
	public abstract Optional<String> getName();
	public abstract String getStatus();
	public abstract Optional<String> getVersion();
	public abstract Boolean getExperimental();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPublisher();
	public abstract Optional<String> getCopyright();
	public abstract Optional<Date> getDate();
	public abstract String getSource();
	public abstract String getTarget();
	public abstract FhirVersion getImplicitFhirVersion();
	
	public abstract List<FhirConceptMapElement> getElements();

	public static WrappedConceptMap fromDefinition(Object definition) {
		if (definition instanceof ca.uhn.fhir.model.dstu2.resource.ConceptMap) {
			return new WrappedDstu2ConceptMap((ca.uhn.fhir.model.dstu2.resource.ConceptMap)definition);
		} else if (definition instanceof org.hl7.fhir.dstu3.model.ConceptMap) {
			return new WrappedStu3ConceptMap((org.hl7.fhir.dstu3.model.ConceptMap)definition);
		} else {
			throw new IllegalStateException("Can't wrap Concept Map definition class " + definition.getClass().getCanonicalName());
		}
	}
}
