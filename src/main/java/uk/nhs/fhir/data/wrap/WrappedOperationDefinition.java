package uk.nhs.fhir.data.wrap;

import java.io.File;
import java.util.List;
import java.util.Optional;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.data.metadata.VersionNumber;
import uk.nhs.fhir.data.opdef.FhirOperationParameter;
import uk.nhs.fhir.data.url.LinkData;

public abstract class WrappedOperationDefinition extends WrappedResource<WrappedOperationDefinition> {

	public abstract String getName();
	public abstract LinkData getNameTypeLink();
	public abstract String getKind();
	public abstract LinkData getKindTypeLink();
	public abstract Optional<String> getDescription();
	public abstract LinkData getDescriptionTypeLink();
	public abstract String getCode();
	public abstract LinkData getCodeTypeLink();
	public abstract LinkData getSystemTypeLink();
	public abstract String getIsSystem();
	public abstract LinkData getInstanceTypeLink();
	public abstract String getIsInstance();
	public abstract String getStatus();

	public abstract List<FhirOperationParameter> getInputParameters();
	public abstract List<FhirOperationParameter> getOutputParameters();
	
	@Override
	public ResourceMetadata getMetadataImpl(File source) {
    	String resourceName = getName();
    	String url = getUrl().get();
    	String resourceID = getIdFromUrl().orElse(resourceName);
    	String displayGroup = "Operations";
        VersionNumber versionNo = parseVersionNumber();
        String status = getStatus();
    	
        return new ResourceMetadata(resourceName, source, ResourceType.OPERATIONDEFINITION,
				false, Optional.empty(), displayGroup, false,
				resourceID, versionNo, status, null, null, null, null, getImplicitFhirVersion(), url);
	}
	
	@Override
	public ResourceType getResourceType() {
		return ResourceType.OPERATIONDEFINITION;
	}
	
	@Override
	public String getCrawlerDescription() {
		return getDescription().orElse(getName());
	}
}
