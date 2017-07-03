package uk.nhs.fhir.resourcehandlers;

import java.io.File;

import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.datalayer.collections.ResourceEntity;

public interface IResourceHelper {
	public IBaseResource getResourceWithoutTextSection(IBaseResource resource);
	public String getTextSection(IBaseResource resource);
	public ResourceEntity getMetadataFromResource(File thisFile);
}
