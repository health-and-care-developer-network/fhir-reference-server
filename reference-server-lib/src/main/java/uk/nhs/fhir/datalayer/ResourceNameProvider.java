package uk.nhs.fhir.datalayer;

import org.hl7.fhir.instance.model.api.IIdType;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;
import uk.nhs.fhir.util.FhirVersion;

public class ResourceNameProvider {
	private final ResourceWebHandler myWebHandler;
	
	public ResourceNameProvider(ResourceWebHandler myWebHandler) {
		this.myWebHandler = myWebHandler;
	}
	
	public String getNameForRequestedEntity(FhirVersion fhirVersion, RequestDetails theRequestDetails) {
		IIdType resourceId = theRequestDetails.getId();
		ResourceMetadata resourceEntityByID = myWebHandler.getResourceEntityByID(fhirVersion, resourceId);
		return resourceEntityByID.getResourceName();
	}
}
