package uk.nhs.fhir.page.list;

import java.util.HashMap;
import java.util.List;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;
import uk.nhs.fhir.enums.ResourceType;

public interface GroupedResourcesProvider {

	HashMap<String, List<ResourceMetadata>> getAGroupedListOfResources(ResourceType resourceType);

}
