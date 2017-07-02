package uk.nhs.fhir.resourcehandlers;

import org.hl7.fhir.instance.model.api.IBaseResource;

public interface IResourceHelper {
	public IBaseResource getResourceWithoutTextSection(IBaseResource resource);
	public String getTextSection(IBaseResource resource);
}
