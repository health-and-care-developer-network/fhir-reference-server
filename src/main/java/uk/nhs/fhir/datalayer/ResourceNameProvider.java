package uk.nhs.fhir.datalayer;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IIdType;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;

public class ResourceNameProvider {
	private final ResourceWebHandler myWebHandler;
	
	public ResourceNameProvider(ResourceWebHandler myWebHandler) {
		this.myWebHandler = myWebHandler;
	}
	
	public String getNameForId(IdDt resourceID) {
        ResourceMetadata resourceEntityByID = myWebHandler.getResourceEntityByID(resourceID);
		return resourceEntityByID.getResourceName();
	}

	public String getNameForId(IdType resourceID) {
        ResourceMetadata resourceEntityByID = myWebHandler.getResourceEntityByID(resourceID);
		return resourceEntityByID.getResourceName();
	}
	
	public String getNameForRequestedEntity(RequestDetails theRequestDetails) {
		IIdType resourceId = theRequestDetails.getId();
		if (resourceId instanceof IdDt) {
			return getNameForId((IdDt)resourceId);
		} else if (resourceId instanceof IdType) {
			return getNameForId((IdType)resourceId);
		} else {
			throw new IllegalStateException("Unexpected IIdType class " + resourceId.getClass().getName());
		}
	}
}
