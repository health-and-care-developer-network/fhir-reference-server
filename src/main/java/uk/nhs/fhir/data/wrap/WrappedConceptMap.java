package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.conceptmap.FhirConceptMapElement;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.wrap.dstu2.WrappedDstu2ConceptMap;
import uk.nhs.fhir.data.wrap.stu3.WrappedStu3ConceptMap;

public abstract class WrappedConceptMap extends WrappedResource<WrappedConceptMap> {
	
	public abstract String getName();
	public abstract String getStatus();
	public abstract Boolean getExperimental();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPublisher();
	public abstract Optional<String> getCopyright();
	public abstract Optional<Date> getDate();
	public abstract String getSource();
	public abstract String getTarget();
	public abstract String getSourceUrl();
	
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

	@Override
	public ResourceMetadata getMetadataImpl(File source) {
		String displayGroup = "Concept Map";
    	String name = getName();
    	String url = getUrl().get();

        String resourceID = getIdFromUrl().orElse(name);
    	VersionNumber versionNo = parseVersionNumber();
    	String status = getStatus();
    	
    	return new ResourceMetadata(name, source, ResourceType.CONCEPTMAP,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.CONCEPTMAP;
	}
	
	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse(getName());
	}
}
