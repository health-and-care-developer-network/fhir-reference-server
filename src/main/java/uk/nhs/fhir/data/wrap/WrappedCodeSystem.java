package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.codesystem.FhirCodeSystemConcepts;
import uk.nhs.fhir.data.codesystem.FhirCodeSystemFilter;
import uk.nhs.fhir.data.codesystem.FhirIdentifier;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.structdef.FhirContacts;

public abstract class WrappedCodeSystem extends WrappedResource<WrappedCodeSystem> {
	public abstract String getName();
	public abstract Optional<String> getTitle();
	public abstract String getStatus();

	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	public abstract Optional<Date> getLastUpdatedDate();
	public abstract Optional<String> getCopyright();
	public abstract Optional<String> getValueSet();
	
	public abstract Optional<FhirIdentifier> getIdentifier();
	
	public abstract Optional<Boolean> getExperimental();
	public abstract Optional<String> getDescription();
	public abstract Optional<String> getPurpose();
	public abstract Optional<Boolean> getCaseSensitive();
	public abstract Optional<Boolean> getCompositional();
	
	public abstract Optional<String> getContent();
	public abstract Optional<String> getHierarchyMeaning();
	public abstract List<FhirContacts> getContacts();
	
	public abstract FhirCodeSystemConcepts getCodeSystemConcepts();
	public abstract List<FhirCodeSystemFilter> getFilters();
	
	public String getUserFriendlyName() {
		if (getTitle().isPresent()) {
			return getTitle().get();
		} else {
			return getName();
		}
	}

	@Override
	public ResourceMetadata getMetadataImpl(File source) {
		String displayGroup = "Code List";
    	String name = getName();
    	String url = getUrl().get();
        String resourceID = getIdFromUrl().orElse(name);
    	VersionNumber versionNo = parseVersionNumber();
    	String status = getStatus();
    	
    	return new ResourceMetadata(name, source, ResourceType.CODESYSTEM,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.CODESYSTEM;
	}
}
