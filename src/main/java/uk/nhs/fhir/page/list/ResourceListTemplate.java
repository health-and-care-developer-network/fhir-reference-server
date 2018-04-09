package uk.nhs.fhir.page.list;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.velocity.VelocityContext;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;
import uk.nhs.fhir.page.VelocityTemplate;

public class ResourceListTemplate extends VelocityTemplate {
	// We want to show a grouped list of resources of a specific type (e.g. StructureDefinitions)
	private final SortedMap<String, List<ResourceMetadata>> sortedGroupedResources;
	
	public ResourceListTemplate(ResourceType resourceType, Map<String, List<ResourceMetadata>> groupedResources) {
		super("list.vm", Optional.of(resourceType), Optional.empty());

		for (List<ResourceMetadata> resourceList : groupedResources.values()) {
			resourceList.sort(Comparator.comparing(r -> r.getResourceName().toLowerCase()));
		}
		
    	SortedMap<String, List<ResourceMetadata>> sortedGroupedResources = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    	sortedGroupedResources.putAll(groupedResources);
		this.sortedGroupedResources = sortedGroupedResources;
	}

	@Override
	protected void updateContext(VelocityContext context) {
    	context.put( "groupedResources", sortedGroupedResources );
    	context.put( "resourceType", resourceType.get() );
	}

}
