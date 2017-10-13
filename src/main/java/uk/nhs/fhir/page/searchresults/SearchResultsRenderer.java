package uk.nhs.fhir.page.searchresults;

import java.util.List;
import java.util.Map;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.enums.ResourceType;
import uk.nhs.fhir.resourcehandlers.ResourceWebHandler;

public class SearchResultsRenderer {

	private final ResourceWebHandler resourceWebHandler;
	
	public SearchResultsRenderer(ResourceWebHandler resourceWebHandler) {
		this.resourceWebHandler = resourceWebHandler;
	}

	public String renderSearchResults(RequestDetails theRequestDetails, ResourceType resourceType) {
		
		Map<String, String[]> params = theRequestDetails.getParameters();
		
		// We are showing a list of matching resources for the specified name query
		List<ResourceMetadata> list;
		if (params.containsKey("name")) {
        	list = resourceWebHandler.getAllNames(resourceType, params.get("name")[0]);
        } else if (params.containsKey("name:contains")) {
        	list = resourceWebHandler.getAllNames(resourceType, params.get("name:contains")[0]);
        } else {
        	throw new IllegalStateException("Expected name or name:contains to be present in params");
        }

        String baseURL = theRequestDetails.getServerBaseForRequest();

		return new SearchResultsTemplate(resourceType, baseURL, list).getHtml();
	}
}
