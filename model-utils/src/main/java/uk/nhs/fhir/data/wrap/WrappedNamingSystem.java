package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.namingsystem.FhirNamingSystemUniqueId;


public abstract class WrappedNamingSystem extends WrappedResource<WrappedNamingSystem>{
	public abstract String getName();
	public abstract String getKind();
	public abstract String getDescription();
	public abstract String getUsage();
	public abstract String getTitle();
	public abstract String getStatus();
	public abstract String getResponsible();
	public abstract Optional<String>  getUrl();

	
	public abstract List<FhirNamingSystemUniqueId> getUniqueIds();
	
	public abstract Optional<String> getDisplay();
	public abstract Optional<String> getPublisher();
	
	public abstract String getFhirVersion();
	
	
	@Override
	public ResourceMetadata getMetadataImpl(File source) {
    	String resourceName = getName();
    	String url = getUrl().get();
    	String resourceID = getIdFromUrl().orElse(resourceName);
    	String displayGroup = "Naming System";
        VersionNumber versionNo = parseVersionNumber();
        String status = getStatus();
        
        String description = getDescription();
        String kind = getKind();
    	
        return new ResourceMetadata(resourceName, source, ResourceType.NAMINGSYSTEM,
				false, // Harded coded for Naming System as the same method id used for Extensions and Naming Systems Anand 
				Optional.empty(), displayGroup, false, 
				resourceID, versionNo, status, null, kind, null, description,
				getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.NAMINGSYSTEM;
	}
	
	@Override
	public String getCrawlerDescription() {
		return getDescription();
	}
}