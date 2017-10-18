package uk.nhs.fhir.page.searchresults;

import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.page.VelocityTemplate;

public class SearchResultsTemplate extends VelocityTemplate {

	private final List<ResourceMetadata> resources;
	
	public SearchResultsTemplate(ResourceType resourceType, String baseURL, List<ResourceMetadata> resources) {
		super("search-results.vm", Optional.of(resourceType.toString()), Optional.empty(), baseURL);
		this.resources = resources;
	}

	@Override
	protected void updateContext(VelocityContext context) {
		context.put( "list", resources );
      	context.put( "resourceType", resourceType.get() );
	}

}
