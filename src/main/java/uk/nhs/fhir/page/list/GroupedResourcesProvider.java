package uk.nhs.fhir.page.list;

import java.util.HashMap;
import java.util.List;

import uk.nhs.fhir.data.metadata.ResourceMetadata;
import uk.nhs.fhir.data.metadata.ResourceType;

public interface GroupedResourcesProvider {

	HashMap<String, List<ResourceMetadata>> getAGroupedListOfResources(ResourceType resourceType);

}
