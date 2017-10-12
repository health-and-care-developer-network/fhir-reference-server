package uk.nhs.fhir.html;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.enums.ResourceType;

public class ResourceListTemplate extends VelocityTemplate {
	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
	private final HashMap<String, List<ResourceMetadata>> groupedResources;
	
	public ResourceListTemplate(ResourceType resourceType, String baseURL, HashMap<String, List<ResourceMetadata>> groupedResources) {
		super(Optional.of("list.vm"), Optional.empty(), Optional.of(resourceType.toString()), Optional.empty(), baseURL);
		this.groupedResources = groupedResources;
	}

	@Override
	protected void updateContext(VelocityContext context) {
    	context.put( "groupedResources", groupedResources );
    	context.put( "resourceType", resourceType );
    	context.put( "baseURL", baseURL );
	}

}
