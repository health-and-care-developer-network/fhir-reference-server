package uk.nhs.fhir.resourcehandlers;

import java.io.File;

import org.hl7.fhir.instance.model.api.IBaseResource;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;

public interface IResourceHelper {
	public IBaseResource removeTextSection(IBaseResource resource);
	public String getTextSection(IBaseResource resource);
	public ResourceMetadata getMetadataFromResource(File thisFile);
}
