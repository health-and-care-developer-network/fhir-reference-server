package uk.nhs.fhir.util;

import org.hl7.fhir.instance.model.api.IBaseResource;

public interface FhirTextSectionHelper {
	public String getTextSection(IBaseResource resource);
    public IBaseResource removeTextSection(IBaseResource resource);
}
