package uk.nhs.fhir.html;

import java.util.List;

import uk.nhs.fhir.datalayer.collections.ResourceMetadata;

public interface ExtensionsListProvider {

	List<ResourceMetadata> getExtensions();

}