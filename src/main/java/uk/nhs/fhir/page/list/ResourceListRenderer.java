package uk.nhs.fhir.page.list;

import java.util.HashMap;
import java.util.List;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.enums.ResourceType;

public class ResourceListRenderer {
	
	private final GroupedResourcesProvider groupedResourcesProvider;

	public ResourceListRenderer(GroupedResourcesProvider groupedResourcesProvider) {
		this.groupedResourcesProvider = groupedResourcesProvider;
	}
	
	/**
     * e.g. http://host/fhir/StructureDefinition
     * @param theRequestDetails
     * @param resourceType
     * @return
     */
    public String renderResourceList(RequestDetails theRequestDetails, ResourceType resourceType) {
        String baseURL = theRequestDetails.getServerBaseForRequest();
    	
    	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
    	HashMap<String, List<ResourceMetadata>> groupedResources = groupedResourcesProvider.getAGroupedListOfResources(resourceType);
    	
    	return new ResourceListTemplate(resourceType, baseURL, groupedResources).getHtml();
	}
}
