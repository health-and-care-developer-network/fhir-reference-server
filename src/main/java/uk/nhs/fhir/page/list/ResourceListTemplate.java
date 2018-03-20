package uk.nhs.fhir.page.list;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.page.VelocityTemplate;

public class ResourceListTemplate extends VelocityTemplate {
	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
	private final HashMap<String, List<ResourceMetadata>> groupedResources;
	
	public ResourceListTemplate(ResourceType resourceType, HashMap<String, List<ResourceMetadata>> groupedResources) {
		super("list.vm", Optional.of(resourceType), Optional.empty());
		this.groupedResources = groupedResources;
	}

	@Override
	protected void updateContext(VelocityContext context) {
    	context.put( "groupedResources", groupedResources );
    	context.put( "resourceType", resourceType.get() );
	}

}
